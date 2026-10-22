import groovy.xml.XmlSlurper

// Called by the generated-project verifier after packaging; no application startup.
def verifyOpenDependencyBoundary = { File projectDir ->
    def pom = new XmlSlurper(false, false).parse(new File(projectDir, 'pom.xml'))
    assert pom.parent.groupId.text() == 'top.egon'
    assert pom.parent.artifactId.text() == 'stellar-enclosure-archetypes-parent'
    assert pom.parent.version.text() == pom.properties.'stellar-enclosure.version'.text()
    assert pom.parent.relativePath.size() == 1 && !pom.parent.relativePath.text()
    def dependencies = pom.dependencies.dependency.findAll { !(it.scope.text() in ['test', 'provided']) }
    def ids = dependencies.artifactId*.text() as Set
    ['stellar-enclosure-component-common-core', 'stellar-enclosure-component-common-id-starter',
     'stellar-enclosure-component-common-mybatis-plus-sharding-jdbc-ext-spring-boot-starter',
     'stellar-enclosure-component-dynamic-thread-pool-starter', 'spring-cloud-starter-bootstrap',
     'spring-cloud-starter-alibaba-nacos-config', 'spring-cloud-starter-alibaba-nacos-discovery',
     'springdoc-openapi-starter-webmvc-ui'].each { required ->
        assert ids.contains(required): "Missing Light Open dependency ${required}"
    }
    def forbidden = ['stellar-enclosure-component-rpc-', 'stellar-enclosure-tianshu', 'dubbo-']
    assert !ids.any { id -> forbidden.any { id.startsWith(it) } }
    def packaged = new File(projectDir, 'target').listFiles()?.find {
        it.name.endsWith('.jar') && !it.name.endsWith('-sources.jar') && !it.name.endsWith('-javadoc.jar')
    }
    assert packaged: 'Expected packaged Light Open runtime'
    def libraries = [] as Set
    new java.util.jar.JarFile(packaged).withCloseable { archive ->
        archive.entries().each { entry ->
            if (entry.name.startsWith('BOOT-INF/lib/')) libraries << entry.name.substring('BOOT-INF/lib/'.length())
        }
    }
    assert libraries.any { it.startsWith('stellar-enclosure-component-common-core-') }
    assert libraries.any { it.startsWith('nacos-client-') }
    assert libraries.any { it.startsWith('springdoc-openapi-starter-webmvc-ui-') }
    assert !libraries.any { library -> forbidden.any { library.startsWith(it) } }
    true
}
verifyOpenDependencyBoundary
