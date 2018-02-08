import {format, resolveConfig, clearConfigCache} from "prettier";

declare var require: any;
type PrettierApi = {
    path: string,
    resolveConfigFn: typeof resolveConfig.sync,
    formatFn: typeof format,
    clearCache: typeof clearConfigCache
}

interface FormatResponse extends ServiceResponse {
    error?: string,
    formatted?: string,
}

type FormatArguments = {
    start?: number,
    end?: number,
    path: string,
    prettierPath: string,
    content: string
}

export class PrettierPlugin implements LanguagePlugin {
    private _prettierApi?: PrettierApi;

    onMessage(p: string, writer: MessageWriter): void {
        let r: ServiceRequest<FormatArguments> = JSON.parse(p);
        let response: FormatResponse = {request_seq: r.seq};
        if (r.command != "reformat") {
            response.error = "Unknown command: " + r.command;
            writer.write(JSON.stringify(response));
            return;
        }

        try {
            let args = (<FormatArguments>r.arguments);
            let prettierApi = this.requirePrettierApi(args.prettierPath);

            try {
                response.formatted = performFormat(prettierApi, args.content, args.path,
                    args.start,
                    args.end)
            }
            catch (e) {
                let msg = `${args.path}: ${e.message}`;
                if (e.stack && e.stack.length > 0 && e.stack != e.message) {
                    msg = msg + `at\n${e.stack}`;
                }
                response.error = msg;
            }
        }
        catch (e) {
            response.error = `${e.message} ${e.stack}`
        }
        writer.write(JSON.stringify(response))
    }

    private requirePrettierApi(path: string): PrettierApi {
        if (this._prettierApi != null && this._prettierApi.path == path) {
            return this._prettierApi;
        }
        let prettier = require(path);
        return this._prettierApi = {
            path: path, formatFn:
            prettier.format,
            resolveConfigFn: prettier.resolveConfig.sync,
            clearCache: prettier.clearConfigCache
        }
    }
}

function performFormat(api: PrettierApi, text: string, path: string, rangeStart?: number, rangeEnd?: number) {
    let config = api.resolveConfigFn(path, {useCache:false, editorconfig: true});
    if (config == null) {
        config = {filepath: path};
    }
    config.rangeStart = rangeStart;
    config.rangeEnd = rangeEnd;
    return api.formatFn(text, config);
}
