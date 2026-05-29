import {cpSync, existsSync, readdirSync, rmSync} from 'node:fs'
import {parse, resolve} from 'node:path'
import {cwd} from 'node:process'

const rootTempDir = resolve(cwd(), '..', '__temp__')
if (existsSync(rootTempDir)) {
  rmSync(rootTempDir, {recursive: true})
}

const rootSourceDir = resolve(cwd(), '..')
readdirSync(rootSourceDir, {withFileTypes: true})
  .filter(entry => entry.isDirectory())
  .filter(entry => entry.name !== '__generator__')
  .filter(entry => !entry.name.endsWith('__transpiled'))
  .map(entry => resolve(rootSourceDir, entry.name))
  .filter(path => existsSync(resolve(path, 'src')))
  .forEach(createTempProject)

function createTempProject(sourcePath: string) {
  const projectDir = resolve(rootTempDir, parse(sourcePath).name)

  cpSync(
    resolve(cwd(), 'default-project-data'),
    projectDir,
    {recursive: true},
  )

  cpSync(
    resolve(sourcePath),
    projectDir,
    {recursive: true},
  )

  cpSync(
    resolve(cwd(), 'node_modules'),
    resolve(projectDir, 'node_modules'),
    {recursive: true},
  )

  cpSync(
    resolve(cwd(), 'vue-transpiled-data-plugin'),
    resolve(projectDir, 'node_modules/vue-transpiled-data-plugin'),
    {recursive: true},
  )
}
