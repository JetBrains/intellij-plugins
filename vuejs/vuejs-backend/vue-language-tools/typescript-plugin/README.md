# Bundled [TypeScript plugin for Vue](https://www.npmjs.com/package/@vue/typescript-plugin)

## Why the path is like this

TypeScript only seems to accept the given plugins if there is a `node_modules` folder in the path to them.
Plugins are passed to TypeScript with the flag `--pluginProbeLocations`, and TypeScript probably treats these folders as it would other
projects, resolving plugins in relation to them.
To be investigated.

## How to bundle

- Adjust version everywhere in `package.json`, `VueServices.kt`
- Build it using `rolldown` inside leaf package folder (inside `node_modules/package/name/...`)

```shell
npm i
npm run build
```