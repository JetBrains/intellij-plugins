import * as prettierModule from "prettier";

type PrettierApi = typeof prettierModule;
const modulePath = process.argv[2];
const configFilePath = process.argv[3];
const prettier: PrettierApi = require(modulePath)

async function main() {
  let config = await prettier.resolveConfig(configFilePath, {config: configFilePath, editorconfig: false})
  console.log(JSON.stringify(config))
}

main()
