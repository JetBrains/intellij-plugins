import type ts from "typescript/lib/tsserverlibrary";
import {existsSync, mkdirSync, rmSync, writeFileSync} from 'node:fs'
import {dirname, relative, resolve} from 'node:path'
import {cwd, env} from 'node:process'

function init() {
  function create(info: ts.server.PluginCreateInfo) {
    const project = info.project;
    const transpiledDir = env.TRANSPILED_DIR as string;

    if (existsSync(transpiledDir)) {
      rmSync(transpiledDir, {recursive: true})
    }

    info.languageService.getTypeDefinitionAtPosition = () => {
      for (const scriptFileName of project.getScriptFileNames()) {
        if (!scriptFileName.endsWith('.vue'))
          continue;

        const scriptInfo = project.getScriptInfo(scriptFileName)!
        const sourceFile = project.getSourceFile(scriptInfo.path)!

        const transpiledFilePath = resolve(
          transpiledDir,
          relative(cwd(), scriptFileName) + ".ts",
        )

        mkdirSync(dirname(transpiledFilePath), {recursive: true});
        writeFileSync(transpiledFilePath, sourceFile.getFullText())
      }

      return undefined;
    }

    return info.languageService;
  }

  return {create};
}

export = init;