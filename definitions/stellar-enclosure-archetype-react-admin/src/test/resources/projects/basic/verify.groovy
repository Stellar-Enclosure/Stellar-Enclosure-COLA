import groovy.json.JsonSlurper
import java.security.MessageDigest

def root = new File(basedir, 'project/example-admin')
assert root.isDirectory()
def json = { path -> new JsonSlurper().parse(new File(root, path)) }
def pkg = json('package.json')
def lock = json('package-lock.json')
def manifest = json('permission-manifest.config.json')
assert pkg.name == 'example-admin' && pkg.version == '0.1.0-SNAPSHOT'
assert lock.name == pkg.name && lock.version == pkg.version
assert lock.packages[''].name == pkg.name && lock.packages[''].version == pkg.version
['stellar-enclosure-web-shared', 'stellar-enclosure-tianquan-jianshen-react-sdk'].each { module ->
    def name = '@stellar-enclosure/' + module
    assert pkg.dependencies[name] == '0.1.0'
    assert lock.packages[''].dependencies[name] == '0.1.0'
    assert lock.packages['node_modules/' + name].version == '0.1.0'
    assert lock.packages['node_modules/' + name].integrity.startsWith('sha512-')
}
def authSource = new File(root, 'src/app/api.ts').getText('UTF-8')
def publicEnv = new File(root, '.env.example').getText('UTF-8')
assert authSource.contains('createPublicOAuthClient')
assert authSource.contains('oauth/callback')
assert authSource.contains('oauth/popup-callback')
assert !authSource.contains('createGatewayAuthClient')
assert publicEnv.contains('VITE_OAUTH_API_BASE_URL=https://')
assert !publicEnv.contains('VITE_OAUTH_APP_ALIAS')
assert new File(root, 'src/app/api.test.ts').isFile()
assert manifest.application.applicationCode == pkg.name && manifest.producerId == 'example-admin-web'
assert new File(root, 'src/permissions/resources.ts').text.contains("applicationCode: 'example-admin'")
assert new File(root, '.gitignore').isFile()
assert json('permission-toolchain.lock.json').mode == 'PINNED_COMMIT'
assert new File(root, '.github/workflows/permission-delivery.yml').getText('UTF-8').contains('${{ inputs.phase }}')
def registry = new File(root, 'src/generated/permissions.ts')
def hash = MessageDigest.getInstance('SHA-256').digest(registry.bytes).collect { String.format('%02x', it & 0xff) }.join()
assert json('.egon/codegen/state.json').artifacts[0].previousGeneratedHash == hash
