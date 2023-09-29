package com.intellij.protobuf.lang.refactoring.json

import com.google.protobuf.Struct
import com.intellij.openapi.util.text.StringUtil

internal class PbJsonStructTransformer(existingNames: Collection<String>) {
  private val knownNames = existingNames.toMutableSet()
  private val structToShortNameMappings = mutableMapOf<Struct, String>()

  fun rememberUniqueStructOrNull(structInJson: PbStructInJson): Struct? {
    val existingStructMapping = structToShortNameMappings[structInJson.struct]
    if (existingStructMapping != null) {
      return null
    }

    val shortenedName = StringUtil.capitalize(structInJson.jsonNodeName)
    val uniqueName =
      if (knownNames.add(shortenedName)) {
        shortenedName
      }
      else {
        generateSequence(1, Int::inc)
          .map { index -> "$shortenedName$index" }
          .first(knownNames::add)
      }

    structToShortNameMappings[structInJson.struct] = uniqueName
    return structInJson.struct
  }

  fun getUniqueName(struct: Struct): String {
    return structToShortNameMappings[struct] ?: "Unknown"
  }
}
