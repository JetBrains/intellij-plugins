"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
/* Initialize access to schematics registry */
let providerPromise;
//find appropriate support
for (let version of ["60", "62", "70", "80", "90"]) {
    try {
        providerPromise = require("./schematicsProvider" + version);
        break;
    }
    catch (e) {
        //ignore
    }
}
if (!providerPromise) {
    console.info("No schematics");
    process.exit(0);
}
const path = require("path");
const fs = require("fs");
const includeHidden = process.argv[2] === "--includeHidden";
(async function () {
    let provider = await providerPromise;
    const defaultCollectionName = provider.getDefaultSchematicCollection();
    const collections = getAvailableSchematicCollections(provider);
    if (collections.indexOf(defaultCollectionName) < 0) {
        collections.push(defaultCollectionName);
    }
    const allSchematics = collections
        // Update schematics should be executed only with `ng update`
        .filter(c => c !== "@schematics/update")
        .map(c => getCollectionSchematics(c, provider, defaultCollectionName))
        .reduce((a, b) => a.concat(...b));
    console.info(JSON.stringify(allSchematics, null, 2));
})().catch(err => console.error(err.stack || err));
function getAvailableSchematicCollections(provider) {
    let result = [];
    let packages = [];
    fs.readdirSync(path.resolve(process.cwd(), "node_modules")).forEach(dir => {
        if (dir.startsWith("@")) {
            fs.readdirSync(path.resolve(process.cwd(), `node_modules/${dir}`)).forEach(subDir => {
                packages.push(`${dir}/${subDir}`);
            });
        }
        else {
            packages.push(dir);
        }
    });
    for (const pkgName of packages) {
        const pkgPath = path.resolve(process.cwd(), `node_modules/${pkgName}/package.json`);
        if (fs.existsSync(pkgPath)) {
            const subInfo = require(pkgPath);
            if (subInfo !== undefined && subInfo.schematics !== undefined) {
                result.push(pkgName);
            }
        }
    }
    return result;
}
function getCollectionSchematics(collectionName, provider, defaultCollectionName) {
    let schematicNames;
    let collection;
    try {
        collection = provider.getCollection(collectionName);
        schematicNames = includeHidden
            ? listAllSchematics(collection)
            : provider.listSchematics(collection);
    }
    catch (e) {
        return [{
                name: collectionName,
                error: "" + e.message
            }];
    }
    try {
        const schematicInfos = schematicNames
            .map(name => {
            try {
                return provider.getSchematic(collection, name).description;
            }
            catch (e) {
                return {
                    name,
                    error: "" + e.message
                };
            }
        })
            //`ng-add` schematics should be executed only with `ng add`
            .filter(info => (info.name !== "ng-add" || includeHidden) && (info.schemaJson !== undefined || info.error));
        const newFormat = schematicInfos
            .map(info => info.schemaJson ? info.schemaJson.properties : {})
            .map(prop => Object.keys(prop).map(k => prop[k]))
            .reduce((a, b) => a.concat(b), [])
            .find(prop => prop.$default);
        return schematicInfos.map(info => {
            const required = (info.schemaJson && info.schemaJson.required) || [];
            return {
                description: info.description,
                name: (collectionName === defaultCollectionName ? "" : collectionName + ":") + info.name,
                hidden: info.hidden,
                error: info.error,
                options: info.schemaJson
                    ? filterProps(info.schemaJson, (key, prop) => newFormat ? prop.$default === undefined : required.indexOf(key) < 0)
                        .concat(coreOptions())
                    : undefined,
                arguments: info.schemaJson
                    ? filterProps(info.schemaJson, (key, prop) => newFormat ? prop.$default !== undefined && prop.$default.$source === "argv" : required.indexOf(key) >= 0)
                    : undefined
            };
        });
    }
    catch (e) {
        console.error(e.stack || e);
        return [];
    }
}
function listAllSchematics(collection) {
    collection = collection.description;
    const schematics = [];
    for (const key of Object.keys(collection.schematics)) {
        const schematic = collection.schematics[key];
        if (schematic.private) {
            continue;
        }
        // If extends is present without a factory it is an alias, do not return it
        //   unless it is from another collection.
        if (!schematic.extends || schematic.factory) {
            schematics.push(key);
        }
        else if (schematic.extends && schematic.extends.indexOf(':') !== -1) {
            schematics.push(key);
        }
    }
    return schematics;
}
function filterProps(schemaJson, filter) {
    const required = schemaJson.required || [];
    const props = schemaJson.properties;
    return Object.keys(props).filter(key => filter(key, props[key])).map(k => Object.assign({ name: k, required: required.indexOf(k) >= 0 }, props[k]));
}
function coreOptions() {
    return [
        {
            name: 'dryRun',
            type: "Boolean",
            default: false,
            alias: 'd',
            description: 'Run through without making any changes.',
        },
        {
            name: 'force',
            type: "Boolean",
            default: false,
            alias: 'f',
            description: 'Forces overwriting of files.',
        }
    ];
}
