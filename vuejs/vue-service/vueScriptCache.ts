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
    const parser = new Parser({
      onopentag(name: string, attribs: { [p: string]: string }) {
        if (name === "script" && level === 0) {
          isScript = true
          for (let attr in attribs) {
            if (attr.toLowerCase() == "lang") {
              const extension = attribs[attr].toLowerCase()
              switch (extension) {
                case "js":
                  scriptKind = ts_impl.ScriptKind.JS;
                  break;
                case "jsx":
                  scriptKind = ts_impl.ScriptKind.JSX;
                  break;
                case "ts":
                  scriptKind = ts_impl.ScriptKind.TS;
                  break;
                case "tsx":
                  scriptKind = ts_impl.ScriptKind.TSX;
                  break;
              }
            }
          }
        }
        level++;
      },
      ontext(data: string) {
        if (isScript) {
          result += " ".repeat(parser.startIndex - lastIndex) + data
          lastIndex = parser.endIndex + 1
        }
      },
      onclosetag(name: string) {
        isScript = false;
        level--
      }
    }, {
      recognizeSelfClosing: true
    })

    parser.write(contents)
    parser.end()

    // Allow for empty <script> tag
    if (result.trim() === "") {
      result = "import Vue from 'vue'; export default Vue;"
      scriptKind = ts_impl.ScriptKind.TS;
    }

    const snapshot = ts_impl.ScriptSnapshot.fromString(result);
    // Allow to retrieve script kind from snapshot
    (<any>snapshot).scriptKind = scriptKind
    return {
      snapshot,
      kind: scriptKind
    }
  }

}

