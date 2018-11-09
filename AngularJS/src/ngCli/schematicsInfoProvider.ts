// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
/* Initialize access to schematics registry */
let provider: SchematicsProvider;

//find appropriate support
for (let version of ["60", "62", "70"]) {
    try {
        provider = require("./schematicsProvider" + version);
        break;
    } catch (e) {
        //ignore
    }
}
if (!provider) {
    console.info("No schematics")
    process.exit(0)
}
/**/

import {SchematicsProvider} from "./schematicsProvider";
import {Option} from "@angular/cli/models/interface";
import * as path from "path";
import * as fs from "fs";

interface SchematicsInfo {
    description?: string;
    name: string;
    error?: string;
    arguments?: Option[];
    options?: Option[];
    hidden?: boolean;
}

const engineHost = provider.getEngineHost();

const includeHidden = process.argv[2] === "--includeHidden";

const defaultCollectionName = provider.getDefaultSchematicCollection();

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
    let schematicNames: string[];
    let collection: any;
    try {
        collection = provider.getCollection(collectionName);
        schematicNames = includeHidden
            ? listAllSchematics(collection)
            : engineHost.listSchematics(collection);
    } catch (e) {
        return [{
            name: collectionName,
            error: "" + e.message
        }]
    }
    try {
        const schematicInfos: any[] = schematicNames
            .map(name => provider.getSchematic(collection, name).description)
            //`ng-add` schematics should be executed only with `ng add`
            .filter(info => (info.name !== "ng-add" || includeHidden) && info.schemaJson !== undefined);

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
                hidden: info.hidden,
                options: filterProps(info.schemaJson,
                    (key, prop) => newFormat ? prop.$default === undefined : required.indexOf(key) < 0)
                    .concat(coreOptions()),
                arguments: filterProps(info.schemaJson,
                    (key, prop) => newFormat ? prop.$default !== undefined && prop.$default.$source === "argv" : required.indexOf(key) >= 0)
            }
        })
    } catch (e) {
        console.error(e.stack || e);
        return [];
    }
}

function listAllSchematics(collection: any) {
    collection = collection.description;
    const schematics = [];
    for (const key of Object.keys(collection.schematics)) {
        const schematic = collection.schematics[key];
        if (schematic.private) {
            continue
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