"use strict";
// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
Object.defineProperty(exports, "__esModule", { value: true });
const project_1 = require("@angular/cli/utilities/project");
const command_runner_1 = require("@angular/cli/models/command-runner");
const config_1 = require("@angular/cli/utilities/config");
const core_1 = require("@angular-devkit/core");
async function getWorkspace() {
    const getWorkspaceDetails = require("@angular/cli/utilities/project").getWorkspaceDetails;
    if (getWorkspaceDetails) {
        // Angular 7-10
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
const patchedCommands = {
    'generate': __dirname + '/commands/generate.json',
};
async function generateVirtual() {
    const workspace = await getWorkspace();
    await command_runner_1.runCommand(['generate', ...process.argv.slice(2)], new core_1.logging.NullLogger(), workspace, patchedCommands);
    process.exit(0);
}
generateVirtual();
