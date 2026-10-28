import {resolve} from 'node:path'
import {pathToFileURL} from 'node:url'

if (!process.env.PAC_SDK_DIR) throw new Error('PAC_SDK_DIR_REQUIRED')
const tool = await import(pathToFileURL(resolve(process.env.PAC_SDK_DIR, 'scripts/permissions/build-consumer.mjs')).href)
const result = await tool.buildConsumer()
process.stdout.write(JSON.stringify({status: result.status, manifestDigest: result.manifestDigest, artifactDigest: result.artifactDigest}) + '\n')
