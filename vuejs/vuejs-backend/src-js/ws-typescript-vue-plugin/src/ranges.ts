// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type {Language, Mapper} from "@volar/language-core"
import {forEachEmbeddedCode} from "@volar/language-core"

export function toGeneratedRange(
  language: Language<string>,
  fileName: string,
  startOffset: number,
  endOffset: number,
): [startOffset: number, endOffset: number] | undefined {
  const sourceScript = language.scripts.get(fileName)
  if (!sourceScript)
    return undefined

  const virtualCode = sourceScript.generated?.root
  if (!virtualCode)
    return undefined

  for (const code of forEachEmbeddedCode(virtualCode)) {
    if (!code.id.startsWith('script_'))
      continue

    const mapper = language.maps.get(code, sourceScript)

    const generatedStartOffset = toGeneratedOffset(mapper, startOffset)
    if (generatedStartOffset === undefined) continue

    const generatedEndOffset = toGeneratedOffset(mapper, endOffset)
    if (generatedEndOffset === undefined) continue

    const tsShift = sourceScript.snapshot.getLength()
    return [
      generatedStartOffset + tsShift,
      generatedEndOffset + tsShift,
    ]
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
