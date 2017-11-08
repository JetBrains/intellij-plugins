package org.angularjs.cli

import com.intellij.openapi.util.text.StringUtil

/**
 * @author Dennis.Ushakov
 */
class BlueprintParser {
  fun parse(output: String): Collection<Blueprint> {
    if (output.isEmpty()) return emptyList()
    val result:MutableList<Blueprint> = mutableListOf()
    val converted = StringUtil.convertLineSeparators(output)
    var name:String? = null
    var description:String? = null
    var arguments:MutableList<String> = mutableListOf()
    for (line in converted.split('\n')) {
      if (line.startsWith("ng generate")) {
        if (name != null) result.add(Blueprint(name, description, arguments))
        name = null
        break
      }
      if (line.startsWith("    ")) {
        var text = line.substring(4)
        if (text.isBlank()) continue

        val nameCandidate = firstWord(text)
        if (nameCandidate.isNotBlank()) {
          if (name != null) {
            result.add(Blueprint(name, description, arguments))
            description = null
            arguments = mutableListOf()
          }

          name = nameCandidate
          continue
        }
        if (!text.startsWith("  ")) continue
        text = text.substring(2)

        if (text.startsWith("-")) arguments.add(firstWord(text))
        else if (!text.isBlank() && !text[0].isWhitespace()) description = text
      }
    }
    if (name != null) result.add(Blueprint(name, description, arguments))
    return result
  }

  private fun firstWord(text: String) = text.takeWhile { !it.isWhitespace() }
}

class Blueprint(val name:String, val description:String?, val args:List<String>) {
  override fun toString(): String{
    return name
  }
}