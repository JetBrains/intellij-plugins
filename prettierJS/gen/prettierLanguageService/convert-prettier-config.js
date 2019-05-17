"use strict";
exports.__esModule = true;
var modulePath = process.argv[2];
var configFilePath = process.argv[3];
var prettier = require(modulePath);
var config = prettier.resolveConfig.sync(configFilePath, { config: configFilePath, editorconfig: false });
console.log(JSON.stringify(config));
