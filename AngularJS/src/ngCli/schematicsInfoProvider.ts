try {
    require('@angular/cli/utilities/schematics');
} catch (e) {
    console.info("No schematics")
    process.exit(0)
}

import {getCollection, getEngineHost, getSchematic} from '@angular/cli/utilities/schematics';
import {Option} from "@angular/cli/models/command";
import * as path from "path";
import * as fs from "fs";

interface SchematicsInfo {
    description: string;
    name: string;
    arguments: Option[];
    options: Option[];
}

const engineHost = getEngineHost();
let defaultCollectionName;
try {
    defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
} catch (e) {
    defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}

const collections = getAvailableSchematicCollections();
if (collections.indexOf(defaultCollectionName) < 0) {
    collections.push(defaultCollectionName);
}

const allSchematics = collections
// Update schematics should be executed only with `ng update`
    .filter(c => c !== "@schematics/update")
    .map(getCollectionSchematics)
    .reduce((a, b) => a.concat(...b));

console.info(JSON.stringify(allSchematics, null, 2))

function getAvailableSchematicCollections() {
    let result: string[] = [];
    let packages: string[] = [];
    fs.readdirSync(path.resolve(process.cwd(), "node_modules")).forEach(dir => {
        if (dir.startsWith("@")) {
            fs.readdirSync(path.resolve(process.cwd(), `node_modules/${dir}`)).forEach(subDir => {
                packages.push(`${dir}/${subDir}`)
            })
        } else {
            packages.push(dir)
        }
    })

    for (const pkgName of packages) {
        const pkgPath = path.resolve(process.cwd(), `node_modules/${pkgName}/package.json`)
        if (fs.existsSync(pkgPath)) {
            const subInfo = require(pkgPath)
            if (subInfo !== undefined && subInfo.schematics !== undefined) {
                result.push(pkgName);
            }
        }
    }
    return result;
}

function getCollectionSchematics(collectionName: string): SchematicsInfo[] {
    const collection = getCollection(collectionName);
    const schematicNames: string[] = engineHost.listSchematics(collection);

    const schematicInfos: any[] = schematicNames
        .map(name => getSchematic(collection, name).description)
        //`ng-add` schematics should be executed only with `ng add`
        .filter(info => info.name !== "ng-add");

    const newFormat = schematicInfos
        .map(info => info.schemaJson.properties)
        .map(prop => Object.keys(prop).map(k => prop[k]))
        .reduce((a, b) => a.concat(b), [])
        .find(prop => prop.$default)

    return schematicInfos.map(info => {
        const required = info.schemaJson.required || [];
        return {
            description: info.description,
            name: (collectionName === defaultCollectionName ? "" : collectionName + ":") + info.name,
            options: filterProps(info.schemaJson,
                (key, prop) => newFormat ? prop.$default === undefined : required.indexOf(key) < 0)
                .concat(coreOptions()),
            arguments: filterProps(info.schemaJson,
                (key, prop) => newFormat ? prop.$default !== undefined && prop.$default.$source === "argv" : required.indexOf(key) >= 0)
        }
    })
}

function filterProps(schemaJson: any, filter: (k: string, prop: any) => boolean): any[] {
    const required = schemaJson.required || [];
    const props = schemaJson.properties;
    return Object.keys(props).filter(
        key => filter(key, props[key])
    ).map(k => Object.assign({name: k, required: required.indexOf(k) >= 0}, props[k]))
}

function coreOptions(): any[] {
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
    ]
}