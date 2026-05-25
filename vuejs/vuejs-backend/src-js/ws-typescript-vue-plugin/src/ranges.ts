// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import type {Language} from "@volar/language-core"
import type {ReverseMapper} from "tsc-ide-plugin/ide-get-element-type"
import type {Position, Range} from "tsc-ide-plugin/protocol"
import {SimpleRange} from "./script-mapper"
import {toGeneratedRangeTransform, toSourceRangeTransform} from "./range-transform"
import {firstNotNull} from "./generators"
import {getNodeAtRange, isVolarTypeAliasDeclaration} from "./volar"
import {isVueFile} from "./vue"

type TypeScript = typeof ts

export function createReverseMapper(
  ts: TypeScript,
  language: Language<string>,
): ReverseMapper {
  return (sourceFile, generatedRange) => {
    const fileName = sourceFile.fileName
    if (!isVueFile(fileName))
      return undefined

    const sourceRange = toSourceRange(ts, language, sourceFile, generatedRange)

    if (!sourceRange)
      return "no-mapping"

    return {
      sourceRange,
      fileName,
    }
  }
}

function toSourceRange(
  ts: TypeScript,
  language: Language<string>,
  sourceFile: ts.SourceFile,
  generatedRange: Range,
): Range | undefined {
  const toSourceRange = toSourceRangeTransform(language, sourceFile.fileName)

  const sourceRange = firstNotNull(
    getGeneratedRanges(ts, sourceFile, generatedRange),
    ([startOffset, endOffset]) => toSourceRange(startOffset, endOffset),
  )

  if (sourceRange === undefined)
    return undefined

  return {
    start: sourceFile.getLineAndCharacterOfPosition(sourceRange[0]),
    end: sourceFile.getLineAndCharacterOfPosition(sourceRange[1]),
  }
}

function* getGeneratedRanges(
  ts: TypeScript,
  sourceFile: ts.SourceFile,
  generatedRange: Range,
): Generator<SimpleRange, void, undefined> {
  const startOffset = getOffset(sourceFile, generatedRange.start)
  const endOffset = getOffset(sourceFile, generatedRange.end)

  yield [startOffset, endOffset]

  const node = getNodeAtRange(ts, sourceFile, startOffset, endOffset)
  if (isVolarTypeAliasDeclaration(ts, node)) {
    const type = node.type
    yield [type.getStart(sourceFile), type.getEnd()]
  }
}

function getOffset(
  sourceFile: ts.SourceFile,
  position: Position,
): number {
  return sourceFile.getPositionOfLineAndCharacter(position.line, position.character)
}

export function toGeneratedRange(
  language: Language<string>,
  fileName: string,
  startOffset: number,
  endOffset: number,
): SimpleRange | undefined {
  const toGeneratedRange = toGeneratedRangeTransform(language, fileName)
  return toGeneratedRange(startOffset, endOffset)
}
