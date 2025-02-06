// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import * as prettier from "prettier";
import {Options} from "prettier"

type PrettierApi = typeof prettier & {
    prettierPath: string;
    packageJsonPath?: string;
};

interface FormatResponse {
    ignored?: boolean;
    unsupported?: boolean;
    error?: string;
    formatted?: string;
}

interface FormatArguments {
    start?: number;
    end?: number;
    path: string;
    prettierPath: string;
    packageJsonPath?: string;
    ignoreFilePath?: string;
    content: string;
    flushConfigCache: boolean;
}

interface ResolveConfigArguments {
    path: string;
    prettierPath: string;
    packageJsonPath?: string;
    flushConfigCache: boolean;
}

interface ResolveConfigResponse {
    config?: Options | null;
    error?: string;
}

export class PrettierPlugin implements LanguagePlugin {
    private _prettierApi?: PrettierApi;

    async onMessage(p: string, writer: MessageWriter) {
        let r: ServiceRequest<FormatArguments | ResolveConfigArguments> =
            JSON.parse(p);
        let response: any;

        try {
            if (r.command == "reformat") {
                response = await this.handleReformatCommand(
                    <FormatArguments>r.arguments,
                );
            } else if (r.command == "resolveConfig") {
                response = await this.handleResolveConfigCommand(
                    <ResolveConfigArguments>r.arguments,
                );
            } else {
                response = { error: "Unknown command: " + r.command };
            }
        } catch (e) {
            let msg =
                e instanceof String
                    ? e
                    : e.stack && e.stack.length > 0
                      ? e.stack
                      : e.message || e;
            response = { error: `${msg}` };
        }
        response.request_seq = r.seq;
        writer.write(JSON.stringify(response));
    }

    private async handleReformatCommand(
        args: FormatArguments,
    ): Promise<FormatResponse> {
        let prettierApi = this.requirePrettierApi(
            args.prettierPath,
            args.packageJsonPath,
        );

        let config = await this.resolveConfig(prettierApi, args);
        let options = {
            ignorePath: args.ignoreFilePath,
            withNodeModules: true,
            plugins: config.plugins,
            resolveConfig: true,
        };

        if (prettierApi.getFileInfo) {
            let fileInfo = await prettierApi.getFileInfo(args.path, options);
            if (fileInfo.ignored) {
                return { ignored: true };
            }
            if (fileInfo.inferredParser == null) {
                return { unsupported: true };
            }
        }
        return performFormat(prettierApi, config, args);
    }

    private async handleResolveConfigCommand(
        args: ResolveConfigArguments,
    ): Promise<ResolveConfigResponse> {
        let prettierApi = this.requirePrettierApi(
            args.prettierPath,
            args.packageJsonPath,
        );
        if (args.flushConfigCache) {
          await prettierApi.clearConfigCache();
        }
        const config = await prettierApi.resolveConfig(args.path, {
          useCache: true,
          editorconfig: true,
        })

        return { config };
    }

    private async resolveConfig(
        prettierApi: PrettierApi,
        args: FormatArguments,
    ) {
        let config = await prettierApi.resolveConfig(args.path, {
            useCache: true,
            editorconfig: true,
        });
        if (config == null) {
            config = { filepath: args.path };
        }
        if (config.filepath == null) {
            config.filepath = args.path;
        }

        config.rangeStart = args.start;
        config.rangeEnd = args.end;
        return config;
    }

    private requirePrettierApi(
        prettierPath: string,
        packageJsonPath?: string,
    ): PrettierApi {
        if (
            this._prettierApi != null &&
            this._prettierApi.prettierPath == prettierPath &&
            this._prettierApi.packageJsonPath == packageJsonPath
        ) {
            return this._prettierApi;
        }
        const prettier = <PrettierApi>(
            requireInContext(prettierPath, packageJsonPath)
        );
        prettier.prettierPath = prettierPath;
        prettier.packageJsonPath = packageJsonPath;
        this._prettierApi = prettier;
        return prettier;
    }
}

async function performFormat(
    api: PrettierApi,
    config: any,
    args: FormatArguments,
): Promise<FormatResponse> {
    if (args.flushConfigCache) {
      await api.clearConfigCache();
    }
    return { formatted: await api.format(args.content, config) };
}

function requireInContext(
    modulePathToRequire: string,
    contextPath?: string,
): any {
    const contextRequire = getContextRequire(modulePathToRequire, contextPath);
    return contextRequire(modulePathToRequire);
}

function getContextRequire(
    modulePathToRequire: string,
    contextPath?: string,
): NodeRequire {
    if (contextPath != null) {
        const m = require("module");
        if (typeof m.createRequire === "function") {
            // https://nodejs.org/api/modules.html#modules_module_createrequire_filename
            // Also, implemented for Yarn Pnp: https://next.yarnpkg.com/advanced/pnpapi/#requiremodule
            return m.createRequire(contextPath);
        }
    }
    return require;
}
