"use strict";
const project_1 = require("@angular/cli/src/utilities/project");
const config_1 = require("@angular/cli/src/utilities/config");
const schematics_command_module_1 = require("@angular/cli/src/command-builder/schematics-command-module");
async function getWorkspace() {
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
    let command = new schematics_command_module_1.SchematicsCommandModule({
        workspace: workspace,
        currentDirectory: process.cwd(),
        root: (_a = workspace === null || workspace === void 0 ? void 0 : workspace.basePath) !== null && _a !== void 0 ? _a : process.cwd()
    });
    return {
        getCollection(collectionName) {
            const workflow = command.getOrCreateWorkflowForBuilder(collectionName);
            return workflow.engine.createCollection(collectionName);
        },
        listSchematics(collection) {
            return collection.listSchematicNames(false);
        },
        getSchematic(collection, schematicName, allowPrivate) {
            return collection.createSchematic(schematicName, allowPrivate);
        },
        getDefaultSchematicCollection() {
            return schematics_command_module_1.DEFAULT_SCHEMATICS_COLLECTION;
        }
    };
})();
module.exports = schematicsProvider;
