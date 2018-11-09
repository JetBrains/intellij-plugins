// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import * as tslintModule from "tslint";
import {IOptions} from "tslint";
import {getVersion} from "./utils";

type TslintModuleType = typeof tslintModule;

const modulePath = process.argv[2];
const configFilePath = process.argv[3];
const tslint: TslintModuleType = require(modulePath);
const version = getVersion(tslint);
let configFile = version.major && version.major >= 4
    ? tslint.Configuration.loadConfigurationFromPath(configFilePath)
    : (<any>tslint).loadConfigurationFromPath(configFilePath);
let configObject = version.major && version.major >= 5
    ? {
        rules: mapToObject(configFile.rules, mapOptions),
        jsRules: mapToObject(configFile.jsRules, mapOptions)
    } : configFile;
console.log(JSON.stringify(configObject))

function mapToObject(map: any, mapper: (p: any) => any) {
    const rules: any = {};
    for (let [key, value] of map) {
        rules[key] = mapper(value)
    }
    return rules;
}

function mapOptions(options: IOptions) {
    return {
        severity: options.ruleSeverity.toString(),
        options: options.ruleArguments
    }
}