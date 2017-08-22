export declare function reload15(session: ts.server.Session, ts_impl: any): any;
export declare function close15(session: ts.server.Session, request: any): void;
export declare function onMessage15(session: ts.server.Session, message: string): {
    response?: any;
    responseRequired?: boolean;
};
export declare function openClientFileConfig15(service: ts.server.ProjectService, fileName: string, fileContent: string, ts_impl: any): {
    configFileName?: string;
    configFileErrors?: ts.Diagnostic[];
};
export declare function openProjectByConfig(service: ts.server.ProjectService, fileName: string, ts_impl: any): ts.server.Project;
export declare function setGetFileNames(Project: typeof ts.server.Project): void;
export declare function findConfiguredProjectByConfigFile15(service: ts.server.ProjectService, configFileName: string): ts.server.Project;
export declare function updateConfiguredProject15(service: ts.server.ProjectService, project: ts.server.Project, ts_impl: any): void;
