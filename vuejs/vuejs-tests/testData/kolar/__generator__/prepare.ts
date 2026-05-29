import {cpSync, existsSync, readdirSync, rmSync, writeFileSync} from 'node:fs'
import {parse, resolve} from 'node:path'
import {cwd} from 'node:process'

const rootTempDir = resolve(cwd(), '..', '__temp__')
if (existsSync(rootTempDir)) {
  rmSync(rootTempDir, {recursive: true})
}

const rootSourceDir = resolve(cwd(), '..')

readdirSync(rootSourceDir, {withFileTypes: true})
  .filter(entry => entry.isDirectory())
  .filter(entry => entry.name.endsWith('__transpiled'))
  .map(entry => resolve(rootSourceDir, entry.name))
  .forEach(path => rmSync(path, {recursive: true}))

const projectDirs = readdirSync(rootSourceDir, {withFileTypes: true})
  .filter(entry => entry.isDirectory())
  .filter(entry => entry.name !== '__generator__')
  .map(entry => resolve(rootSourceDir, entry.name))
  .filter(path => existsSync(resolve(path, 'src')))
  .map(createTempProject)

const scriptContent = projectDirs
  .map(path => parse(path).name)
  .flatMap(dirName => [
    `cd ${dirName}/`,
    'node generate.ts',
    'cd ../',
    '',
  ])
  .toSpliced(0, 0,
    '#!/bin/bash',
    'set -e',
    '',
  )
  .join('\n');

writeFileSync(resolve(rootTempDir, 'generate-all.sh'), scriptContent)

function createTempProject(sourcePath: string): string {
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

  return projectDir
}
