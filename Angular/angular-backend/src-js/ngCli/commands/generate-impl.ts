import {BaseSchematicSchema} from '@angular/cli/models/schematic-command';
import {DryRunSink, workflow} from '@angular-devkit/schematics';
import {GenerateCommand} from "@angular/cli/commands/generate-impl";
import {Observable, of} from 'rxjs';

export class PatchedGenerateCommand extends GenerateCommand {
  protected async createWorkflow(options: BaseSchematicSchema): Promise<workflow.BaseWorkflow> {
    const workflow = await super.createWorkflow(options);

    (workflow as any)._createSinks = function () {
      return [new JsonSink((workflow as any)._host, options.force)]
    }

    return workflow;
  }
}

interface AngularCliGenerateCommand {
  kind: 'WRITE' | 'DELETE' | 'RENAME',
  path: string,
  to?: string,
  content?: string,
}

class JsonSink extends DryRunSink {
  _done(): Observable<void> {
    if (this._fileAlreadyExistExceptionSet.size > 0 || this._fileDoesNotExistExceptionSet.size > 0) {
      // do not output commands
      return of<void>(undefined);
    }

    const commands: AngularCliGenerateCommand[] = [];

    for (let path of this._filesToDelete.values()) {
      commands.push({
        kind: 'DELETE',
        path: path,
      })
    }

    for (let [path, to] of this._filesToRename.values()) {
      commands.push({
        kind: "RENAME",
        path: path,
        to: to,
      })
    }

    for (let [path, buffer] of [...this._filesToCreate.entries(), ...this._filesToUpdate.entries()]) {
      commands.push({
        kind: 'WRITE',
        path: path,
        content: buffer.generate().toString("utf-8"),
      })
    }

    console.log(JSON.stringify(commands, null, 2));

    return of<void>(undefined);
  }
}
