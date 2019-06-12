import * as prettier from "prettier";

type PrettierApi = typeof prettier & { path: string }

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
        let prettierApi = this.requirePrettierApi(args.prettierPath);

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

    private requirePrettierApi(path: string): PrettierApi {
        if (this._prettierApi != null && this._prettierApi.path == path) {
            return this._prettierApi;
        }
        let prettier = (<PrettierApi>require(path));
        prettier.path = path;
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
