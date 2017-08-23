/**
 * see TypeScriptServiceInitialStateObject
 */
interface TypeScriptPluginState extends PluginState {
    serverFolderPath: string;

    //deprecated parameters but we need it for back-compatibility
    hasManualParams: boolean;
    outPath?: string;
    projectPath?: string;
    commandLineArguments?: string[];
    mainFilePath?: string;


    isUseSingleInferredProject?: boolean;
    pluginProbeLocations?:string[];
    globalPlugins?: string[];
}