// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {findWorkspaceFile} from "@angular/cli/utilities/project";
import {runCommand} from "@angular/cli/models/command-runner";
import {AngularWorkspace, getWorkspaceRaw} from "@angular/cli/utilities/config";
import {logging} from "@angular-devkit/core";

async function getWorkspace(): Promise<AngularWorkspace> {
  const getWorkspaceDetails = require("@angular/cli/utilities/project").getWorkspaceDetails

  if (getWorkspaceDetails) {
    // Angular 7-10
    return await getWorkspaceDetails()
  }
  // Angular 11+
  let workspace;
  const workspaceFile = findWorkspaceFile();
  if (workspaceFile === null) {
    const [, localPath] = getWorkspaceRaw('local');
    if (localPath !== null) {
      throw new Error(
        `An invalid configuration file was found ['${localPath}'].` +
        ' Please delete the file before running the command.',
      );
    }
  }
  else {
    try {
      return await AngularWorkspace.load(workspaceFile);
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

  await runCommand([ 'generate', ...process.argv.slice(2)], new logging.NullLogger() as any, workspace, patchedCommands);
  process.exit(0);
}

generateVirtual();