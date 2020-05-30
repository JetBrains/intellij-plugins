import * as prettier from "prettier";

type PrettierApi = typeof prettier & { prettierPath: string, packageJsonPath?: string }

interface FormatResponse {
    ignored?: boolean,
    unsupported?: boolean,
    error?: string,
    formatted?: string
}

type FormatArguments = {
    start?: number,
    end?: number,
    path: string,
    prettierPath: string,
    packageJsonPath?: string,
    ignoreFilePath?: string,
    content: string,
    flushConfigCache: boolean
}

export class PrettierPlugin implements LanguagePlugin {
    private _prettierApi?: PrettierApi;

    onMessage(p: string, writer: MessageWriter): void {
        let r: ServiceRequest<FormatArguments> = JSON.parse(p);
        let response: any;

        try {
            if (r.command == "reformat") {
                response = this.handleReformatCommand((<FormatArguments>r.arguments));
            }
            else {
                response = {error: "Unknown command: " + r.command};
            }
        }
        catch (e) {
            let msg = e instanceof String ? e : (e.stack && e.stack.length > 0 ? e.stack : e.message || e)
            response = {error: `${msg}`};
        }
        response.request_seq = r.seq
        writer.write(JSON.stringify(response))
    }

    private handleReformatCommand(args: FormatArguments): FormatResponse {
        let prettierApi = this.requirePrettierApi(args.prettierPath, args.packageJsonPath);

        let options = {ignorePath: args.ignoreFilePath, withNodeModules: true};
        if (prettierApi.getFileInfo) {
            let fileInfo = prettierApi.getFileInfo.sync(args.path, options)
            if (fileInfo.ignored) {
                return {ignored: true}
            }
            if (fileInfo.inferredParser == null) {
                return {unsupported: true}
            }
        }
        return performFormat(prettierApi, args)
    }

    private requirePrettierApi(prettierPath: string, packageJsonPath?: string): PrettierApi {
        if (this._prettierApi != null
            && this._prettierApi.prettierPath == prettierPath
            && this._prettierApi.packageJsonPath == packageJsonPath) {
            return this._prettierApi;
        }
        const prettier = (<PrettierApi>requireInContext(prettierPath, packageJsonPath));
        prettier.prettierPath = prettierPath;
        prettier.packageJsonPath = packageJsonPath;
        this._prettierApi = prettier;
        return prettier;
    }
}

function performFormat(api: PrettierApi, args: FormatArguments): { formatted: string} {
    if (args.flushConfigCache) {
        api.clearConfigCache()
    }
    let config = api.resolveConfig.sync(args.path, {useCache: true, editorconfig: true});
    if (config == null) {
        config = {filepath: args.path};
    }
    if (config.filepath == null) {
        config.filepath = args.path
    }

    config.rangeStart = args.start;
    config.rangeEnd = args.end;
    return {formatted: api.format(args.content, config)};
}

function requireInContext(modulePathToRequire: string, contextPath?: string): any {
    const contextRequire = getContextRequire(modulePathToRequire, contextPath);
    return contextRequire(modulePathToRequire);
}

function getContextRequire(modulePathToRequire: string, contextPath?: string): NodeRequire {
    if (contextPath != null) {
        const m = require('module')
        if (typeof m.createRequire === 'function') {
            // https://nodejs.org/api/modules.html#modules_module_createrequire_filename
            // Also, implemented for Yarn Pnp: https://next.yarnpkg.com/advanced/pnpapi/#requiremodule
            return m.createRequire(contextPath);
        }
    }
    return require;
}
