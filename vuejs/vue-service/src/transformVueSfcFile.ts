// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type {ScriptKind} from 'typescript/lib/tsserverlibrary'
import {Parser} from "htmlparser2/lib/Parser"

type TS = typeof import("typescript/lib/tsserverlibrary");

const scriptSetupPrefix = ";(async ()=>{";
const scriptSetupSuffix = "})();";
const prefixLength = scriptSetupPrefix.length;
const suffixLength = scriptSetupSuffix.length;
const componentShim = "import componentDefinition from '*.vue'; export default componentDefinition;";

export function transformVueSfcFile(ts: TS, contents: string): { result: string, scriptKind: ScriptKind } {
  let result = "";

  let lastIndex = 0;
  let level = 0;
  let isScript = false;
  let scriptKind = ts.ScriptKind.JS;
  let inScriptSetup = false;
  let addedScriptSetupPrefix = false;
  let scriptSetupGeneric = ""

  let hadScriptSetup = false;
  let hadScriptNormal = false;
  let scriptSetupStartLoc = -1;
  let scriptSetupEndLoc = -1;

  const parser = new Parser({
    onopentag(name: string, attrs: { [p: string]: string }) {
      if (name === "script" && level === 0) {
        isScript = true
        inScriptSetup = false
        for (let attr in attrs) {
          if (attr.toLowerCase() == "lang") {
            const attrValue = attrs[attr]!.toLowerCase()
            scriptKind = getUpdatedScriptKind(ts, scriptKind, attrValue)
          }
          if (attr.toLowerCase() == "setup") {
            inScriptSetup = true
            addedScriptSetupPrefix = false
            hadScriptSetup = true
          }
          if (attr.toLowerCase() == "generic") {
            scriptSetupGeneric = attrs[attr]
          }
        }
        hadScriptNormal = hadScriptNormal || !inScriptSetup
      }
      level++;
    },
    ontext(data: string) {
      if (isScript) {
        const lineCount = contents.substring(lastIndex, parser.startIndex).split("\n").length - 1
        let charsCount = parser.startIndex - lastIndex - lineCount
        if (inScriptSetup && !addedScriptSetupPrefix) {
          addedScriptSetupPrefix = true
          scriptSetupStartLoc = result.length
          if (scriptSetupGeneric) {
            const genericPrefix = `;(async<${scriptSetupGeneric}>()=>{`
            result += genericPrefix
            charsCount -= genericPrefix.length
          } else {
            result += scriptSetupPrefix
            charsCount -= prefixLength
          }
        }
        result += " ".repeat(charsCount) + "\n".repeat(lineCount) + data
        lastIndex = parser.endIndex! + 1 // TODO handle null assertion
      }
    },
    onclosetag(name: string) {
      if (inScriptSetup) {
        scriptSetupEndLoc = result.length
        result += scriptSetupSuffix
        inScriptSetup = false
        lastIndex += suffixLength
      }
      isScript = false;
      level--
    }
  }, {
    recognizeSelfClosing: true
  })

  parser.write(contents)
  parser.end()

  // Support empty <script> tag
  if (result.trim() === "") {
    result = componentShim;
    scriptKind = ts.ScriptKind.TS;
  }
  // Support <script setup> syntax
  else if (hadScriptSetup) {
    if (!hadScriptNormal) {
      result = `${result}; ${componentShim}`
    }

    // Imports handling is confusing:
    // * Vue Compiler hoists imports out of script setup, to module scope
    // * WS needs those imports preserved as is inside script setup, in order to report errors in them (it's a fixable limitation)
    // * TS compiler will raise e.g. TS2307: Cannot find module 'vue' or its corresponding type declarations.
    //   for imports inside functions, but only if there's no equivalent import in module scope.
    // * script setup imports are visible from normal setup, which is also hoisted to module scope.
    // Therefore, WS duplicates the imports.
    // This can be considered a hack, import statements inside functions make no sense, but we skip TS1232 in VueTypeScriptService,
    // and our transformation does not need to be correct at runtime.
    // We also skip errors in stuff added below because VueTypeScriptService filters ranges.
    result += "\n;"
    const r = /import[^'"]*['"]([^'"]*)['"]/g;
    const fragmentToMatch = result.substring(scriptSetupStartLoc, scriptSetupEndLoc)
    let match: RegExpMatchArray | null
    while ((match = r.exec(fragmentToMatch)) !== null) {
      result += `import "${match[1]}";\n`
    }
  }

  return {
    result,
    scriptKind
  }
}

function getUpdatedScriptKind(ts: TS, scriptKind: ScriptKind, value: string): ScriptKind {
  switch (value) {
    case "jsx":
      if (scriptKind == ts.ScriptKind.JS) {
        scriptKind = ts.ScriptKind.JSX;
      }
      else {
        scriptKind = ts.ScriptKind.TSX;
      }
      break;
    case "ts":
      if (scriptKind == ts.ScriptKind.JS) {
        scriptKind = ts.ScriptKind.TS;
      }
      else if (scriptKind == ts.ScriptKind.JSX) {
        scriptKind = ts.ScriptKind.TSX;
      }
      break;
    case "tsx":
      scriptKind = ts.ScriptKind.TSX;
      break;
  }

  return scriptKind
}
