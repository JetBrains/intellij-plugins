// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import * as ts from 'typescript/lib/tsserverlibrary'

import {Parser} from "htmlparser2/lib/Parser"

export class VueScriptCache {

  private cache: Map<string, { version: string, snapshot: ts.IScriptSnapshot, kind: ts.ScriptKind }> = new Map()

  constructor(private ts_impl: typeof ts,
              private getHostScriptSnapshot: (fileName: string) => ts.IScriptSnapshot,
              private getScriptVersion: (fileName: string) => string) {
  }

  getScriptKind(fileName: string): ts.ScriptKind {
    return this.getUpToDateInfo(fileName).kind
  }

  getScriptSnapshot(fileName: string): ts.IScriptSnapshot {
    return this.getUpToDateInfo(fileName).snapshot
  }

  private getUpToDateInfo(fileName: string): { snapshot: ts.IScriptSnapshot, kind: ts.ScriptKind } {
    const fromCache = this.cache.get(fileName)
    const currentVersion = this.getScriptVersion(fileName)
    if (fromCache?.version === currentVersion) {
      return {
        kind: fromCache.kind,
        snapshot: fromCache.snapshot
      }
    }
    const snapshot = this.getHostScriptSnapshot(fileName)
    const result = this.parseVue(snapshot.getText(0, snapshot.getLength()))
    this.cache.set(fileName, {...result, version: currentVersion})
    return result
  }

  private parseVue(contents: string): { snapshot: ts.IScriptSnapshot, kind: ts.ScriptKind } {
    let lastIndex = 0;
    let level = 0;
    let result = "";
    let isScript = false;
    const ts_impl = this.ts_impl
    let scriptKind = ts_impl.ScriptKind.JS;
    let inScriptSetup = false;
    let addedScriptSetupPrefix = false;

    let hadScriptSetup = false;
    let hadScriptNormal = false;
    let scriptSetupStartLoc = -1;
    let scriptSetupEndLoc = -1;
    const parser = new Parser({
      onopentag(name: string, attribs: { [p: string]: string }) {
        if (name === "script" && level === 0) {
          isScript = true
          inScriptSetup = false
          for (let attr in attribs) {
            if (attr.toLowerCase() == "lang") {
              const extension = attribs[attr].toLowerCase()
              switch (extension) {
                case "jsx":
                  if (scriptKind == ts_impl.ScriptKind.JS) {
                    scriptKind = ts_impl.ScriptKind.JSX;
                  } else {
                    scriptKind = ts_impl.ScriptKind.TSX;
                  }
                  break;
                case "ts":
                  if (scriptKind == ts_impl.ScriptKind.JS) {
                    scriptKind = ts_impl.ScriptKind.TS;
                  } else if (scriptKind == ts_impl.ScriptKind.JSX) {
                    scriptKind = ts_impl.ScriptKind.TSX;
                  }
                  break;
                case "tsx":
                  scriptKind = ts_impl.ScriptKind.TSX;
                  break;
              }
            }
            if (attr.toLowerCase() == "setup") {
              inScriptSetup = true
              addedScriptSetupPrefix = false
              hadScriptSetup = true
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
            result += ";(()=>{"
            charsCount -= 7
          }
          result += " ".repeat(charsCount) + "\n".repeat(lineCount) + data
          lastIndex = parser.endIndex + 1
        }
      },
      onclosetag(name: string) {
        if (inScriptSetup) {
          scriptSetupEndLoc = result.length
          result += "})();"
          inScriptSetup = false
          lastIndex += 5
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
      result = "import componentDefinition from '*.vue'; export default componentDefinition;"
      scriptKind = ts_impl.ScriptKind.TS;
    }
    // Support <script setup> syntax
    else if (hadScriptSetup && !hadScriptNormal) {
      result = result + "; import __componentDefinition from '*.vue'; export default __componentDefinition;"

      // Remove wrapper for imports to work properly
      if (scriptSetupStartLoc >= 0 ) {
        result = result.substring(0, scriptSetupStartLoc) + " ".repeat(7) + result.substring(scriptSetupStartLoc + 7)
      }
      if (scriptSetupEndLoc >= 0 ) {
        result = result.substring(0, scriptSetupEndLoc) + " ".repeat(5) + result.substring(scriptSetupEndLoc + 5)
      }
    }

    const snapshot = ts_impl.ScriptSnapshot.fromString(result);
    // Allow retrieving script kind from snapshot
    (<any>snapshot).scriptKind = scriptKind
    return {
      snapshot,
      kind: scriptKind
    }
  }

}

