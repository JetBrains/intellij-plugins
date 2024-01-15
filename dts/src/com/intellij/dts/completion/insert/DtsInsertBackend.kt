package com.intellij.dts.completion.insert

interface DtsInsertBackend {
  /**
   * Heuristic whether a character should be inserted or not. Returns:
   * - 0 if the character should be inserted
   * - 1 if the current line is not empty
   * - 2 if the character appeared after a new line
   * - 3 if the character should not be inserted
   */
  fun shouldWrite(char: Char): Int

  fun write(text: String)

  fun moveCaret(offset: Int)

  fun openAutocomplete()
}

