import type ts from "typescript/lib/tsserverlibrary";
import {existsSync, mkdirSync, rmSync, writeFileSync} from 'node:fs'
import path from 'node:path'
import process from 'node:process'

function init() {
    function create(info: ts.server.PluginCreateInfo) {
        const project = info.project;
        const transpiledDir = process.env.TRANSPILED_DIR as string;

        if (existsSync(transpiledDir)) {
            rmSync(transpiledDir, {recursive: true})
        }

        info.languageService.getTypeDefinitionAtPosition = (fileName) => {
            for (const scriptFileName of project.getScriptFileNames()) {
                if (!scriptFileName.endsWith('.vue'))
                    continue;

                const scriptInfo = project.getScriptInfo(scriptFileName)!
                const sourceFile = project.getSourceFile(scriptInfo.path)!

                const transpiledFilePath = path.resolve(
                    transpiledDir,
                    path.relative(process.cwd(), scriptFileName) + ".ts",
                )

                mkdirSync(path.dirname(transpiledFilePath), { recursive: true });
                writeFileSync(transpiledFilePath, sourceFile.getFullText())
            }

            return undefined;
        }

        return info.languageService;
    }

    return {create};
}

export = init;