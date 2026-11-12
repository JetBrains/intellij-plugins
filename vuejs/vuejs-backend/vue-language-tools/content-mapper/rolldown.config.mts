// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import * as fs from 'node:fs'
import * as path from 'node:path'
import {defineConfig} from 'rolldown'

const resolve = (...paths: string[]) => path.resolve(import.meta.dirname, ...paths)
const readJson = (file: string) => JSON.parse(fs.readFileSync(file, 'utf8'))

const nodeModulesPath = resolve('node_modules')
const ownPackageJson = readJson(resolve('package.json'))
const mapperPath = path.join(nodeModulesPath, ownPackageJson.name)
const mapperPackageJson = readJson(path.join(mapperPath, 'package.json'))

if (ownPackageJson.version !== mapperPackageJson.version) {
  throw new Error(`Make sure that the version in package.json matches the version of official mapper distribution (${mapperPackageJson.version})`)
}

const typeScriptResolutionFailure = "throw new Error(`Cannot find TS 7.1 from ${directory}: no \\`typescript\\` package with \\`typescript/ast\\` resolves from it.`);"
const typeScriptResolutionFallback = `
        const fallbackDirectory = process.env.JETBRAINS_TYPESCRIPT_PACKAGE_DIR;
        if (fallbackDirectory) {
            return path.join(fallbackDirectory, 'package.json');
        }
        ${typeScriptResolutionFailure}`

export default defineConfig({
  input: path.join(mapperPath, 'server.js'),
  output: {
    file: resolve('lib/server.js'),
    format: 'cjs',
  },
  platform: 'node',
  transform: {
    define: {
      'process.env.NODE_ENV': '"production"',
    },
  },
  external: [/^typescript(?:\/.*)?$/],
  plugins: [
    {
      name: 'typescript-fallback-and-assets',
      transform: {
        filter: {
          id: /[\\/]@vue[\\/]language-core[\\/]lib[\\/]typescript[\\/]loadTypeScript\.js$/,
        },
        handler(code) {
          if (!code.includes(typeScriptResolutionFailure)) {
            throw new Error('Cannot patch loadTypeScript.js: the code has changed')
          }
          return code.replace(typeScriptResolutionFailure, typeScriptResolutionFallback)
        },
      },
      writeBundle() {
        const typesPath = resolve('types')
        fs.rmSync(typesPath, {recursive: true, force: true})
        fs.cpSync(path.join(nodeModulesPath, '@vue/language-core/types'), typesPath, {recursive: true})
        fs.rmSync(nodeModulesPath, {recursive: true})
      },
    },
  ],
})
