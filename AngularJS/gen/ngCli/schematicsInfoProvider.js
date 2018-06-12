"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
try {
    require('@angular/cli/utilities/schematics');
}
catch (e) {
    console.info("No schematics");
    process.exit(0);
}
var schematics_1 = require("@angular/cli/utilities/schematics");
var path = require("path");
var fs = require("fs");
var engineHost = schematics_1.getEngineHost();
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
    var collection = schematics_1.getCollection(collectionName);
    var schematicNames = engineHost.listSchematics(collection);
    var schematicInfos = schematicNames
        .map(function (name) { return schematics_1.getSchematic(collection, name).description; })
        //`ng-add` schematics should be executed only with `ng add`
        .filter(function (info) { return info.name !== "ng-add"; });
    var newFormat = schematicInfos
        .map(function (info) { return info.schemaJson.properties; })
        .map(function (prop) { return Object.keys(prop).map(function (k) { return prop[k]; }); })
        .reduce(function (a, b) { return a.concat(b); }, [])
        .find(function (prop) { return prop.$default; });
    return schematicInfos.map(function (info) {
        var required = info.schemaJson.required || [];
        return {
            description: info.description,
            name: (collectionName === defaultCollectionName ? "" : collectionName + ":") + info.name,
            options: filterProps(info.schemaJson, function (key, prop) { return newFormat ? prop.$default === undefined : required.indexOf(key) < 0; })
                .concat(coreOptions()),
            arguments: filterProps(info.schemaJson, function (key, prop) { return newFormat ? prop.$default !== undefined && prop.$default.$source === "argv" : required.indexOf(key) >= 0; })
        };
    });
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
