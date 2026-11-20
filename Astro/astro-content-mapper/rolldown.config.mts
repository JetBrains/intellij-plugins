// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import * as fs from 'node:fs'
import * as path from 'node:path'
import {defineConfig} from 'rolldown'

const resolve = (...paths: string[]) => path.resolve(import.meta.dirname, ...paths)
const readJson = (file: string) => JSON.parse(fs.readFileSync(file, 'utf8'))

const nodeModulesPath = resolve('node_modules')
const ownPackageJson = readJson(resolve('package.json'))
const packageName = ownPackageJson.name
const theirPackageJson = readJson(path.resolve(nodeModulesPath, packageName, 'package.json'))

if (ownPackageJson.version !== theirPackageJson.version) {
  throw new Error(`Make sure that the version in package.json matches the version of official mapper distribution (${theirPackageJson.version})`)
}

const wasmFileName = 'astro2tsx.wasm32-wasi.wasm'
const outputFile = resolve('lib/server.mjs')

export default defineConfig({
  input: path.resolve(nodeModulesPath, packageName, 'dist/server.js'),
  output: {
    file: outputFile,
    format: 'esm',
  },
  platform: 'node',
  transform: {
    define: {
      'process.env.NODE_ENV': '"production"',
    },
  },
  plugins: [
    {
      name: 'astro2tsx-wasm',
      writeBundle() {
        if (!fs.readFileSync(outputFile, 'utf8').includes(`new URL("./${wasmFileName}", import.meta.url)`)) {
          throw new Error(`The bundle does not load ./${wasmFileName} next to it: the code of @astrojs/astro2tsx has changed`)
        }
        fs.copyFileSync(path.resolve(nodeModulesPath, '@astrojs/astro2tsx', wasmFileName), resolve('lib', wasmFileName))
        fs.rmSync(nodeModulesPath, {recursive: true})
      },
    },
  ],
})