declare const require: any;


declare interface AngularTypeScriptPluginState extends TypeScriptPluginState {
    typescriptPluginPath: string;
    ngServicePath: string;
}


declare namespace ts.server.CommandNames {
    const IDEGetHtmlErrors: string;
    const IDENgCompletions: string;
    const IDEGetProjectHtmlErr: string;
}