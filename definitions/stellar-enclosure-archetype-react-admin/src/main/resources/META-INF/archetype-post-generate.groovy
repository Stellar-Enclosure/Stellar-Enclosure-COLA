import java.security.MessageDigest

def name = request.artifactId
if (!(name ==~ /[a-z][a-z0-9-]{0,63}/)) {
    throw new IllegalArgumentException('React artifactId must be a lowercase npm name')
}
if (request.version.size() > 128 || !(request.version ==~ /(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)(?:-[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*)?(?:\+[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*)?/)) {
    throw new IllegalArgumentException('React project version must be an npm semantic version')
}
def root = new File(request.outputDirectory, name)
def sentinel = 'stellar-enclosure-source-react-admin'
def replaceName = { String path ->
    def file = new File(root, path)
    def text = file.getText('UTF-8')
    if (!text.contains(sentinel)) throw new IllegalStateException('React project sentinel is missing: ' + path)
    file.setText(text.replace(sentinel, name), 'UTF-8')
}
['package.json', 'package-lock.json', 'permission-manifest.config.json', 'src/permissions/resources.ts'].each(replaceName)

def pkg = new File(root, 'package.json')
pkg.setText(pkg.getText('UTF-8').replaceFirst(/"version": "0\.0\.1"/, '"version": "' + request.version + '"'), 'UTF-8')
def lock = new File(root, 'package-lock.json')
def lockText = lock.getText('UTF-8').replaceFirst(/"version": "0\.0\.1"/, '"version": "' + request.version + '"')
def rootPackage = lockText.indexOf('    "": {')
if (rootPackage < 0) throw new IllegalStateException('npm lock root is missing')
def prefix = lockText.substring(0, rootPackage)
def suffix = lockText.substring(rootPackage).replaceFirst(/"version": "0\.0\.1"/, '"version": "' + request.version + '"')
lock.setText(prefix + suffix, 'UTF-8')
def manifest = new File(root, 'permission-manifest.config.json')
manifest.setText(manifest.getText('UTF-8').replaceFirst(/"version": "0\.0\.1"/, '"version": "' + request.version + '"'), 'UTF-8')

def resources = new File(root, 'src/permissions/resources.ts')
def original = resources.getText('UTF-8')
if (!original.contains("applicationCode: '" + name + "'")) throw new IllegalStateException('React application resource is invalid')

def registry = new File(root, 'src/generated/permissions.ts')
def hash = MessageDigest.getInstance('SHA-256').digest(registry.bytes).collect { String.format('%02x', it & 0xff) }.join()
def stateDirectory = new File(root, '.egon/codegen')
if (!stateDirectory.mkdirs() && !stateDirectory.isDirectory()) throw new IOException('Cannot create codegen state directory')
new File(stateDirectory, 'state.json').setText('{"formatVersion":1,"latestObservedSchema":null,"versionChecksumPrefix":null,' +
    '"artifacts":[{"path":"src/generated/permissions.ts","artifact":"frontend-registry","logicalTable":null,' +
    '"lastGeneratedInput":"react-archetype","previousGeneratedHash":"' + hash + '",' +
    '"previousGeneratedHashPresent":true,"selected":false}],"journal":null}\n', 'UTF-8')
