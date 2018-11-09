// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.openapi.util.text.StringUtil

/**
 * @author Dennis.Ushakov
 */
class BlueprintParser {
  fun parse(output: String): Collection<Schematic> {
    if (output.isEmpty()) return emptyList()
    val result: MutableList<Schematic> = mutableListOf()
    val converted = StringUtil.convertLineSeparators(output)
    var name: String? = null
    var description: String? = null
    var options: MutableList<Option> = mutableListOf()
    var arguments: MutableList<Option> = mutableListOf()
    for (line in converted.split('\n')) {
      if (line.startsWith("ng generate")) {
        if (name != null) result.add(Schematic(name, description, options, arguments))
        name = null
        break
      }
      if (line.startsWith("    ")) {
        var text = line.substring(4)
        if (text.isBlank()) continue

        val nameCandidate = firstWord(text)
        if (nameCandidate.isNotBlank()) {
          if (name != null) {
            result.add(Schematic(name, description, options, arguments))
            description = null
            options = mutableListOf()
            arguments = mutableListOf()
          }
          name = nameCandidate
          arguments.addAll(
            text.split(" ").filter { it.startsWith("<") && it != "<options...>" }.map {
              Option(it.substring(1 until it.length - 1))
            })
          continue
        }
        if (!text.startsWith("  ")) continue
        text = text.substring(2)

        if (text.startsWith("--")) options.add(Option(firstWord(text).substring(2)))
        else if (!text.isBlank() && !text[0].isWhitespace()) description = text
      }
    }
    if (name != null) result.add(Schematic(name, description, options, arguments))
    return result
  }

  private fun firstWord(text: String) = text.takeWhile { !it.isWhitespace() }
}