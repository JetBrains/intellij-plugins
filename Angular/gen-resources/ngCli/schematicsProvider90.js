"use strict";
const schematic_command_1 = require("@angular/cli/models/schematic-command");
const project_1 = require("@angular/cli/utilities/project");
const config_1 = require("@angular/cli/utilities/config");
const getWorkspaceDetails = require("@angular/cli/utilities/project").getWorkspaceDetails;
async function getWorkspace() {
    if (getWorkspaceDetails) {
        // Angular 9-10
        return await getWorkspaceDetails();
    }
    // Angular 11+
    let workspace;
    const workspaceFile = project_1.findWorkspaceFile();
    if (workspaceFile === null) {
        const [, localPath] = config_1.getWorkspaceRaw('local');
        if (localPath !== null) {
            throw new Error(`An invalid configuration file was found ['${localPath}'].` +
                ' Please delete the file before running the command.');
        }
    }
    else {
        try {
            return await config_1.AngularWorkspace.load(workspaceFile);
        }
        catch (e) {
            throw new Error(`Unable to read workspace file '${workspaceFile}': ${e.message}`);
        }
    }
}
const schematicsProvider = (async function () {
    var _a;
    let workspace = await getWorkspace();
    let command = new schematic_command_1.SchematicCommand({
        workspace: workspace,
        currentDirectory: process.cwd(),
        root: (_a = workspace === null || workspace === void 0 ? void 0 : workspace.basePath) !== null && _a !== void 0 ? _a : process.cwd()
    }, null, null);
    let { listSchematicNames } = (await command.createWorkflow({ interactive: false })).engineHost;
    let defaultSchematicCollection = await command.getDefaultSchematicCollection();
    return {
        getCollection(collectionName) {
            return command.getCollection(collectionName);
        },
        listSchematics(collection) {
            return listSchematicNames(collection.description);
        },
        getSchematic(collection, schematicName, allowPrivate) {
            return command.getSchematic(collection, schematicName, allowPrivate);
        },
        getDefaultSchematicCollection() {
            return defaultSchematicCollection;
        }
    };
})();
module.exports = schematicsProvider;
