"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
/* Initialize access to schematics registry */
var provider;
try {
    //first try to use schematics utils approach
    provider = require("./schematicsProvider60");
}
catch (e) {
    try {
        //if not working than try to load SchematicCommand
        provider = require("./schematicsProvider62");
    }
    catch (e) {
        console.info("No schematics");
        process.exit(0);
    }
}
var path = require("path");
var fs = require("fs");
var engineHost = provider.getEngineHost();
var includeHidden = process.argv[2] === "--includeHidden";
var defaultCollectionName;
try {
    defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
}
catch (e) {
    defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}
var collections = getAvailableSchematicCollections();
if (collections.indexOf(defaultCollectionName) < 0) {
    collections.push(defaultCollectionName);
}
var allSchematics = collections
    // Update schematics should be executed only with `ng update`
    .filter(function (c) { return c !== "@schematics/update"; })
    .map(getCollectionSchematics)
    .reduce(function (a, b) { return a.concat.apply(a, b); });
console.info(JSON.stringify(allSchematics, null, 2));
function getAvailableSchematicCollections() {
    var result = [];
    var packages = [];
    fs.readdirSync(path.resolve(process.cwd(), "node_modules")).forEach(function (dir) {
        if (dir.startsWith("@")) {
            fs.readdirSync(path.resolve(process.cwd(), "node_modules/" + dir)).forEach(function (subDir) {
                packages.push(dir + "/" + subDir);
            });
        }
        else {
            packages.push(dir);
        }
    });
    for (var _i = 0, packages_1 = packages; _i < packages_1.length; _i++) {
        var pkgName = packages_1[_i];
        var pkgPath = path.resolve(process.cwd(), "node_modules/" + pkgName + "/package.json");
        if (fs.existsSync(pkgPath)) {
            var subInfo = require(pkgPath);
            if (subInfo !== undefined && subInfo.schematics !== undefined) {
                result.push(pkgName);
            }
        }
    }
    return result;
}
function getCollectionSchematics(collectionName) {
    var schematicNames;
    var collection;
    try {
        collection = provider.getCollection(collectionName);
        schematicNames = includeHidden
            ? listAllSchematics(collection)
            : engineHost.listSchematics(collection);
    }
    catch (e) {
        return [{
                name: collectionName,
                error: "" + e.message
            }];
    }
    try {
        var schematicInfos = schematicNames
            .map(function (name) { return provider.getSchematic(collection, name).description; })
            //`ng-add` schematics should be executed only with `ng add`
            .filter(function (info) { return (info.name !== "ng-add" || includeHidden) && info.schemaJson !== undefined; });
        var newFormat_1 = schematicInfos
            .map(function (info) { return info.schemaJson.properties; })
            .map(function (prop) { return Object.keys(prop).map(function (k) { return prop[k]; }); })
            .reduce(function (a, b) { return a.concat(b); }, [])
            .find(function (prop) { return prop.$default; });
        return schematicInfos.map(function (info) {
            var required = info.schemaJson.required || [];
            return {
                description: info.description,
                name: (collectionName === defaultCollectionName ? "" : collectionName + ":") + info.name,
                hidden: info.hidden,
                options: filterProps(info.schemaJson, function (key, prop) { return newFormat_1 ? prop.$default === undefined : required.indexOf(key) < 0; })
                    .concat(coreOptions()),
                arguments: filterProps(info.schemaJson, function (key, prop) { return newFormat_1 ? prop.$default !== undefined && prop.$default.$source === "argv" : required.indexOf(key) >= 0; })
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
    var schematics = [];
    for (var _i = 0, _a = Object.keys(collection.schematics); _i < _a.length; _i++) {
        var key = _a[_i];
        var schematic = collection.schematics[key];
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
    var required = schemaJson.required || [];
    var props = schemaJson.properties;
    return Object.keys(props).filter(function (key) { return filter(key, props[key]); }).map(function (k) { return Object.assign({ name: k, required: required.indexOf(k) >= 0 }, props[k]); });
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
