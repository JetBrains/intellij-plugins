import {SessionClass} from "./typings/ts-session-provider";
import {IDEGetHtmlErrors, IDEGetProjectHtmlErr, IDENgCompletions} from "./ngutil";
import * as ts from './typings/tsserverlibrary';

export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: SessionClass): SessionClass {


    class AngularSessionLatest extends sessionClass {
        executeCommand(request: ts.server.protocol.Request): { response?: any; responseRequired?: boolean } {
            let command = request.command;
            if (command == IDEGetHtmlErrors) {
                request.command = ts_impl.server.CommandNames.SemanticDiagnosticsSync;
                
                return super.executeCommand(request)
            }
            if (command == IDENgCompletions) {

                request.command = ts_impl.server.CommandNames.Completions;
                return super.executeCommand(request)
            }

            if (command == IDEGetProjectHtmlErr) {
                request.command = ts_impl.server.CommandNames.GeterrForProject;
                return super.executeCommand(request)
            }

            return super.executeCommand(request);
        }

    }


    return AngularSessionLatest;
}