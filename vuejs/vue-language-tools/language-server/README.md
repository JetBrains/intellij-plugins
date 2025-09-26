# Bundled [LSP server for Vue](https://www.npmjs.com/package/@vue/language-server)

## How to bundle

- Adjust version everywhere in `package.json`, `VueServices.kt`
- Build it using bundler inside package folder

> For v2 `esbuild` was used, since v3 it's `rolldown`

```shell
npm i
npm run build
```