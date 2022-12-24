package com.jetbrains.cidr.cpp.embedded.platformio.project

enum class SourceTemplate(val fileName: String, val content: String) {
  NONE("", ""),

  GENERIC(
    "main.c",
    """
      int main() {
      // write your code here
      }
      
      """.trimIndent()),
  ARDUINO(
    "main.cpp",
    """
      #include <Arduino.h>
      void setup() {
      // write your initialization code here
      }

      void loop() {
      // write your code here
      }
      """.trimIndent());

  companion object {
    fun byFrameworkName(platformName: String): SourceTemplate {
      return if ("arduino".equals(platformName, ignoreCase = true)) ARDUINO else GENERIC
    }

    fun byFrameworkName(platformsNames: Collection<String?>): SourceTemplate {
      return if (platformsNames.contains("arduino")) ARDUINO else GENERIC
    }

    val EMPTY_BOARD_INFO = BoardInfo(GENERIC, emptyList())
  }
}

data class BoardInfo(val template: SourceTemplate, val parameters: List<String>)