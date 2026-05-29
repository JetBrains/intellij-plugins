import {spawn} from 'node:child_process'
import { parse, resolve } from 'node:path'
import {cwd, execPath, exit} from 'node:process'

const currentDirName = parse(cwd()).name
const transpiledDir = resolve(cwd(), '../..', currentDirName + "__transpiled")

const tsserver = spawn(
    execPath,
    [
        "./node_modules/typescript/lib/tsserver.js",
        '--globalPlugins',
        '@vue/typescript-plugin,vue-transpiled-data-plugin',
        '--pluginProbeLocations',
        "./node_modules",
    ],
    {
        stdio: ['pipe', 'pipe', 'inherit'],
        env: { TRANSPILED_DIR: transpiledDir },
    }
)

function processResponse(
    content: string,
) {
    const response = JSON.parse(content)

    if (response.request_seq === undefined) {
        return
    }

    if (response.success !== true) {
        console.error("INVALID RESPONSE:", content)
        tsserver.kill()
        exit(1)
    }

    if (response.request_seq === 36) {
        sendRequest(38, 'typeDefinition', {
            file: cwd() + '/src/force-vue-transpiler-data-generation.ts',
            line: 1,
            offset: 0,
        })
        return
    }

    if (response.request_seq === 38) {
        console.log("Processing finished! SUCCESS!")
        tsserver.kill()
        exit()
    }
}

tsserver.stdout.on('data', (data) => {
    const dataString = data.toString().trim()
    if (!dataString.startsWith('Content-Length: '))
        return

    console.log("---------------")
    console.log(dataString)
    console.log("---------------")

    dataString.split('\r\n\r\n')
        .slice(1)
        .map(s => s.split('\nContent-Length: ')[0])
        .forEach(s => processResponse(s))
})

function sendRequest(
  seq: number, 
  command: string, 
  args: Record<string, unknown>,
) {
    const payload = {
        seq,
        type: 'request',
        command,
        arguments: args,
    }

    // tsserver expects valid JSON stringified on a single line
    const jsonStr = JSON.stringify(payload) + '\n'
    tsserver.stdin.write(jsonStr)
}

sendRequest(36, 'open', {
    file: cwd() + '/src/force-vue-transpiler-data-generation.ts',
})
