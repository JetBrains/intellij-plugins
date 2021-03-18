"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.PatchedGenerateCommand = void 0;
const schematics_1 = require("@angular-devkit/schematics");
const generate_impl_1 = require("@angular/cli/commands/generate-impl");
const rxjs_1 = require("rxjs");
class PatchedGenerateCommand extends generate_impl_1.GenerateCommand {
    async createWorkflow(options) {
        const workflow = await super.createWorkflow(options);
        workflow._createSinks = function () {
            return [new JsonSink(workflow._host, options.force)];
        };
        return workflow;
    }
}
exports.PatchedGenerateCommand = PatchedGenerateCommand;
class JsonSink extends schematics_1.DryRunSink {
    _done() {
        if (this._fileAlreadyExistExceptionSet.size > 0 || this._fileDoesNotExistExceptionSet.size > 0) {
            // do not output commands
            return rxjs_1.of(undefined);
        }
        const commands = [];
        for (let path of this._filesToDelete.values()) {
            commands.push({
                kind: 'DELETE',
                path: path,
            });
        }
        for (let [path, to] of this._filesToRename.values()) {
            commands.push({
                kind: "RENAME",
                path: path,
                to: to,
            });
        }
        for (let [path, buffer] of [...this._filesToCreate.entries(), ...this._filesToUpdate.entries()]) {
            commands.push({
                kind: 'WRITE',
                path: path,
                content: buffer.generate().toString("utf-8"),
            });
        }
        console.log(JSON.stringify(commands, null, 2));
        return rxjs_1.of(undefined);
    }
}
