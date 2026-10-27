import groovy.io.FileType
import groovy.xml.XmlSlurper

def artifact = 'student-management-evaluation-standalone'
def project = [new File(basedir, "project/${artifact}"), new File(basedir, artifact)].find { it.isDirectory() }
assert project != null
// Native code-gen domain POJOs use the existing managed compile-only Lombok dependency.
def codegenDomainModules = project.listFiles().findAll { it.isDirectory() && it.name.endsWith('-domain') }
assert codegenDomainModules.size() == 1: 'Expected exactly one generated Domain module'
def codegenDomainPom = new XmlSlurper(false, false).parse(new File(codegenDomainModules[0], 'pom.xml'))
def codegenDomainLombok = codegenDomainPom.dependencies.dependency.find {
    it.groupId.text() == 'org.projectlombok' && it.artifactId.text() == 'lombok'
}
assert codegenDomainLombok && codegenDomainLombok.scope.text() == 'provided': 'Domain code-gen requires compile-only Lombok'
assert !codegenDomainLombok.version.text(): 'Domain Lombok version must remain parent-managed'

// Source layout is checked before build-output assertions; a clean hash or javac pass is insufficient.
def contractJavaSources = []
project.eachFileRecurse(FileType.FILES) { source ->
    def relative = project.toPath().relativize(source.toPath()).toString().replace('\\', '/')
    if (!relative.tokenize('/').any { it in ['target', '.egon', '.git'] } &&
            source.name.endsWith('.java') && relative.contains('src/main/java/')) {
        contractJavaSources << [file: source, relative: relative.split('src/main/java/', 2)[1]]
    }
}
assert contractJavaSources: 'Expected main Java sources for package/path contract'
contractJavaSources.each { entry ->
    if (entry.relative != 'module-info.java') {
        def declaration = entry.file.getText('UTF-8') =~ /\A(?:\s|\/\/[^\r\n]*|\/\*[\s\S]*?\*\/)*package\s+([\w.]+)\s*;/
        assert declaration.find(): "Missing Java package: ${entry.file}"
        def slash = entry.relative.lastIndexOf('/')
        def expected = slash < 0 ? '' : entry.relative.substring(0, slash).replace('/', '.')
        assert declaration.group(1) == expected: "Java package/path mismatch: ${entry.file}"
        def carrier = entry.file.getText('UTF-8') =~ /(?m)^\s*(?:(?:public|protected|private|static|abstract|final|sealed|non-sealed)\s+)*(?:class|interface|record)\s+\w*(?:Request|Response)\b/
        assert !carrier.find(): "Business carrier must use DTO/Command/Query or Result/VO: ${entry.file}"
    }
}

def root = new XmlSlurper(false, false).parse(new File(project, 'pom.xml'))
assert root.modules.module*.text().size() == 7
assert !root.properties.'organization-facade.group-id'.text()
assert !root.properties.'organization-facade.artifact-id'.text()
assert !root.properties.'organization-facade.version'.text()
assert !root.properties.'organization-facade.package'.text()
def infra = new File(project, "${artifact}-infrastructure")
assert !new File(infra, 'pom.xml').text.contains('organization-facade')
assert !new File(project, 'pom.xml').text.contains('__NO_PEER_FACADE__')
def peerSources = []
new File(infra, 'src/main/java').eachFileRecurse { file ->
    if (file.isFile() && (file.path.contains('/client/organization/') || file.name == 'OrganizationDirectoryServiceImpl.java')) peerSources << file
}
assert peerSources.isEmpty()
new File(project, "${artifact}-domain/src/main/java").eachFileRecurse { file ->
    if (file.isFile()) assert !file.name.startsWith('Organization')
}
['application.yml', 'application-dev.yml', 'application-test.yml', 'application-prod.yml'].each { name ->
    assert !new File(project, "${artifact}-starter/src/main/resources/${name}").text.contains('ORGANIZATION_FACADE_')
}
['env', 'compose'].each { directory ->
    new File(project, "deploy/${directory}").eachFileRecurse { file ->
        if (file.isFile()) assert !file.text.contains('ORGANIZATION_FACADE_')
    }
}

// Every application input has an invoked executor in the same business domain.
def cqersSources = []
project.eachFileRecurse(FileType.FILES) { source ->
    def relative = project.toPath().relativize(source.toPath()).toString().replace('\\', '/')
    if (!relative.tokenize('/').any { it in ['target', '.egon', '.git'] } &&
            source.name.endsWith('.java') && relative.contains('src/main/java/')) {
        cqersSources << [file: source, relative: relative.split('src/main/java/', 2)[1]]
    }
}
def cqersCallerText = cqersSources.collect { it.file.getText('UTF-8') }.join('\n')
def cqersExpected = [] as Set
cqersSources.each { entry ->
    def input = entry.relative =~ /^(.*\/application\/[^\/]+)\/pojo\/(command|query|event)\/([A-Za-z0-9_]+)\.java$/
    if (input.matches()) {
        def domain = input.group(1)
        def kind = input.group(2)
        def type = input.group(3)
        def suffix = [command: 'Command', query: 'Query', event: 'Event'][kind]
        assert type.endsWith(suffix): "Invalid CQERS input ${entry.relative}"
        def executorName = kind == 'command' ? type.substring(0, type.length() - suffix.length()) + 'CmdExe' : type + 'Exe'
        def expected = "${domain}/executor/${kind}/${executorName}.java".toString()
        cqersExpected << expected
        def executor = cqersSources.find { it.relative == expected }
        assert executor: "Missing CQERS executor ${expected}"
        def execution = executor.file.getText('UTF-8') =~ /(?s)\bexecute\s*\([^)]*\b${type}\b/
        assert execution.find(): "${expected} does not accept ${type}"
        def bean = executorName.substring(0, 1).toLowerCase() + executorName.substring(1)
        assert cqersCallerText.contains(bean + '.execute('): "${expected} is never invoked"
    }
}
assert !cqersExpected.empty: 'No CQERS application inputs found'
cqersSources.findAll { it.relative.contains('/application/') && it.relative.contains('/executor/') && it.file.name.endsWith('Exe.java') }.each {
    assert cqersExpected.contains(it.relative): "Executor without matching input ${it.relative}"
}
assert new File(project, 'README.md').getText('UTF-8').contains('CQERS Architecture')
assert new File(project, 'README.zh-CN.md').getText('UTF-8').contains('CQERS 架构')

return true
