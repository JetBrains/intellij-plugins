// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import * as ts from 'typescript/lib/tsserverlibrary'
import {transformVueSfcFile} from "./transformVueSfcFile"

export class VueScriptCache {

  private cache: Map<string, { version: string, snapshot: ts.IScriptSnapshot, kind: ts.ScriptKind }> = new Map()

  constructor(private tsImpl: typeof ts,
              private getHostScriptSnapshot: (fileName: string) => ts.IScriptSnapshot | undefined,
              private getScriptVersion: (fileName: string) => string) {
  }

  getScriptKind(fileName: string): ts.ScriptKind | undefined {
    return this.getUpToDateInfo(fileName)?.kind
  }

  getScriptSnapshot(fileName: string): ts.IScriptSnapshot | undefined {
    return this.getUpToDateInfo(fileName)?.snapshot
  }

  private getUpToDateInfo(fileName: string): { snapshot: ts.IScriptSnapshot, kind: ts.ScriptKind } | undefined {
    const fromCache = this.cache.get(fileName)
    const currentVersion = this.getScriptVersion(fileName)
    if (fromCache?.version === currentVersion) {
      return {
        kind: fromCache.kind,
        snapshot: fromCache.snapshot
      }
    }
    const snapshot = this.getHostScriptSnapshot(fileName)
    if (snapshot === undefined || snapshot === null) return undefined
    const result = this.handleVueFile(snapshot.getText(0, snapshot.getLength()))
    this.cache.set(fileName, {...result, version: currentVersion})
    return result
  }

  private handleVueFile(contents: string): { snapshot: ts.IScriptSnapshot, kind: ts.ScriptKind } {
    const {result, scriptKind} = transformVueSfcFile(this.tsImpl, contents);

    const snapshot = this.tsImpl.ScriptSnapshot.fromString(result);
    // Allow retrieving script kind from snapshot
    (snapshot as any).scriptKind = scriptKind
    return {
      snapshot,
      kind: scriptKind
    }
  }

}

