// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import type {Language, Mapper} from "@volar/language-core"
import {forEachEmbeddedCode} from "@volar/language-core"
import type {ReverseMapper} from "tsc-ide-plugin/ide-get-element-type"
import type {Position, Range} from "tsc-ide-plugin/protocol"

export function createReverseMapper(
  language: Language<string>,
): ReverseMapper {
  return (sourceFile, generatedRange) => {
    const sourceRange = toSourceRange(language, sourceFile, generatedRange)

    if (!sourceRange)
      return undefined

    return {
      sourceRange,
      fileName: sourceFile.fileName,
    }
  }
}

function toSourceRange(
  language: Language<string>,
  sourceFile: ts.SourceFile,
  generatedRange: Range,
): Range | undefined {
  for (const {toSourceOffset} of scriptMappers(language, sourceFile.fileName)) {
    const sourceStartOffset = toSourceOffset(getOffset(sourceFile, generatedRange.start))
    if (sourceStartOffset === undefined) continue

    const sourceEndOffset = toSourceOffset(getOffset(sourceFile, generatedRange.end))
    if (sourceEndOffset === undefined) continue

    return {
      start: sourceFile.getLineAndCharacterOfPosition(sourceStartOffset),
      end: sourceFile.getLineAndCharacterOfPosition(sourceEndOffset),
    }
  }

  return undefined
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
): [startOffset: number, endOffset: number] | undefined {
  for (const {toGeneratedOffset} of scriptMappers(language, fileName)) {
    const generatedStartOffset = toGeneratedOffset(startOffset)
    if (generatedStartOffset === undefined) continue

    const generatedEndOffset = toGeneratedOffset(endOffset)
    if (generatedEndOffset === undefined) continue

    return [
      generatedStartOffset,
      generatedEndOffset,
    ]
  }

  return undefined
}

function* scriptMappers(
  language: Language<string>,
  fileName: string,
): Generator<ScriptMapper, void, undefined> {
  const sourceScript = language.scripts.get(fileName)
  if (!sourceScript)
    return

  const virtualCode = sourceScript.generated?.root
  if (!virtualCode)
    return

  for (const code of forEachEmbeddedCode(virtualCode)) {
    if (!code.id.startsWith('script_'))
      continue

    yield new ScriptMapper(
      language.maps.get(code, sourceScript),
      sourceScript.snapshot.getLength(),
    )
  }
}

class ScriptMapper {
  constructor(
    readonly mapper: Mapper,
    readonly tsShift: number,
  ) {
  }

  toSourceOffset = (offset: number): number | undefined => {
    for (const [sourceOffset] of this.mapper.toSourceLocation(offset - this.tsShift)) {
      return sourceOffset
    }

    return undefined
  }

  toGeneratedOffset = (offset: number): number | undefined => {
    for (const [generatedOffset] of this.mapper.toGeneratedLocation(offset + this.tsShift)) {
      return generatedOffset
    }

    return undefined
  }
}
