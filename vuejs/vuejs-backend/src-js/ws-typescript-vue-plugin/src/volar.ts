// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"

type TypeScript = typeof ts

export function getNodeAtRange(
  ts: TypeScript,
  sourceFile: ts.SourceFile,
  startOffset: number,
  endOffset: number,
): ts.Node {
  let node = ts.getTokenAtPosition(sourceFile, startOffset)

  do {
    const next: ts.Node | undefined = node.parent
    if (next && startOffset <= next.getStart(sourceFile) && next.getEnd() <= endOffset) {
      node = next
    }
    else {
      break
    }
  } while (node !== sourceFile)

  return node
}

export function isVolarTypeAliasDeclaration(
  ts: TypeScript,
  node: ts.Node,
): node is ts.TypeAliasDeclaration {
  return ts.isTypeAliasDeclaration(node)
    && node.name.text.startsWith("__VLS_")
}
