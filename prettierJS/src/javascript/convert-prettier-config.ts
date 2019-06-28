import * as prettierModule from "prettier";

type PrettierApi = typeof prettierModule;
const modulePath = process.argv[2];
const configFilePath = process.argv[3];
const prettier: PrettierApi = require(modulePath)

let config = prettier.resolveConfig.sync(configFilePath, {config: configFilePath, editorconfig: false})

console.log(JSON.stringify(config))