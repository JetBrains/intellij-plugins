import * as prettier from "prettier";

declare var require: any;
type PrettierApi = typeof prettier & { path: string }

interface FormatResponse {
    error?: string,
    formatted?: string,
}

type FormatArguments = {
    start?: number,
    end?: number,
    path: string,
    prettierPath: string,
    content: string,
    flushConfigCache: boolean
}

type SupportedFilesArguments = {
    prettierPath: string,
}

interface SupportedFilesResponse {
    fileNames: string[],
    extensions: string[]
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
            else if (r.command == "getSupportedFiles") {
                response = this.getSupportedFiles((<SupportedFilesArguments>r.arguments).prettierPath);
            }
            else {
                response = {error: "Unknown command: " + r.command};
            }
        }
        catch (e) {
            response = {error: `${e.message} ${e.stack}`};
        }
        response.request_seq = r.seq
        writer.write(JSON.stringify(response))
    }

    private handleReformatCommand(args: FormatArguments): FormatResponse {
        let prettierApi = this.requirePrettierApi(args.prettierPath);

        try {
            return {formatted: performFormat(prettierApi, args)}
        }
        catch (e) {
            return {error: `${args.path}: ${e.stack && e.stack.length > 0 ? e.stack : e.message}`};
        }
    }

    getSupportedFiles(path: string): SupportedFilesResponse {
        let prettierApi = this.requirePrettierApi(path)
        let info = prettierApi.getSupportInfo();
        let extensions = flatten(info.languages.map(l => l.extensions)).map(e => withoutPrefix(e, "."));
        let fileNames = flatten(info.languages.map(l => l.filenames != null ? l.filenames : []));
        return {
            fileNames: fileNames,
            extensions: extensions
        }
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

function withoutPrefix(e: string, prefix: string): string {
    if (e == null || e.length == 0) {
        return e;
    }
    let index = e.indexOf(prefix);
    return index == 0 ? e.substr(prefix.length) : e;
}

function flatten<T>(arr: T[][]): T[] {
    return arr.reduce((previousValue, currentValue) => previousValue.concat(currentValue))
}

function performFormat(api: PrettierApi, args: FormatArguments) {
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
    return api.format(args.content, config);
}
