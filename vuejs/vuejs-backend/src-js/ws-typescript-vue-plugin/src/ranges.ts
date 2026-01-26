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
  for (const [mapper, tsShift] of scriptMappers(language, sourceFile.fileName)) {
    const sourceStartOffset = toSourceOffset(mapper, getOffset(sourceFile, generatedRange.start) - tsShift)
    if (sourceStartOffset === undefined) continue

    const sourceEndOffset = toSourceOffset(mapper, getOffset(sourceFile, generatedRange.end) - tsShift)
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
  for (const [mapper, tsShift] of scriptMappers(language, fileName)) {
    const generatedStartOffset = toGeneratedOffset(mapper, startOffset)
    if (generatedStartOffset === undefined) continue

    const generatedEndOffset = toGeneratedOffset(mapper, endOffset)
    if (generatedEndOffset === undefined) continue

    return [
      generatedStartOffset + tsShift,
      generatedEndOffset + tsShift,
    ]
  }

  return undefined
}

function* scriptMappers(
  language: Language<string>,
  fileName: string,
): Generator<[mapper: Mapper, tsShift: number], void, undefined> {
  const sourceScript = language.scripts.get(fileName)
  if (!sourceScript)
    return

  const virtualCode = sourceScript.generated?.root
  if (!virtualCode)
    return

  const tsShift = sourceScript.snapshot.getLength()

  for (const code of forEachEmbeddedCode(virtualCode)) {
    if (!code.id.startsWith('script_'))
      continue

    yield [language.maps.get(code, sourceScript), tsShift]
  }
}

function toSourceOffset(
  mapper: Mapper,
  offset: number,
): number | undefined {
  for (const [sourceOffset] of mapper.toSourceLocation(offset)) {
    return sourceOffset
  }

  return undefined
}

function toGeneratedOffset(
  mapper: Mapper,
  offset: number,
): number | undefined {
  for (const [generatedOffset] of mapper.toGeneratedLocation(offset)) {
    return generatedOffset
  }

  return undefined
}
