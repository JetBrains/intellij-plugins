// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.util.text.StringUtil
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hil.ILLanguageInjector

object HCLQuoter {
  /**
   * Returns null if failed to unquote
   */
  fun unquote(text: String, supportInterpolations: Boolean = true, safe: Boolean = false): String {
    val stripQuotes = HCLPsiUtil.stripQuotes(text)
    // FIXME: Fix unescaping for single quoted string
    return unescapeStringCharacters(stripQuotes, supportInterpolations, safe)
  }

  fun escape(text: String, supportInterpolations: Boolean = true): String {
    return escapeStringCharacters(text, supportInterpolations)
  }

  private fun unescapeStringCharacters(s: String, supportInterpolations: Boolean, safe: Boolean): String {
    val buffer = StringBuilder(s.length)
    unescapeStringCharacters(s.length, s, buffer, supportInterpolations, safe)
    return buffer.toString()
  }

  private fun escapeStringCharacters(s: String, supportInterpolations: Boolean): String {
    val length = s.length
    val buffer = StringBuilder(length)
    if (supportInterpolations && (s.contains("\${") || s.contains("%{"))) {
      val ranges = ILLanguageInjector.getILRangesInText(s)
      var last = 0
      for (range in ranges) {
        if (last < range.startOffset) {
          val sub = s.substring(last, range.startOffset)
          StringUtil.escapeStringCharacters(sub.length, sub, JavaUtil.ourEscapedSymbols, true, buffer)
        }
        escapeInterpolation(range.substring(s), buffer)
        last = range.endOffset
      }
      if (last < length) {
        val sub = s.substring(last, length)
        StringUtil.escapeStringCharacters(sub.length, sub, JavaUtil.ourEscapedSymbols, true, buffer)
      }
    } else {
      StringUtil.escapeStringCharacters(length, s, JavaUtil.ourEscapedSymbols, true, buffer)
    }
    return buffer.toString()
  }

  private fun escapeInterpolation(s: String, buffer: StringBuilder) {
    if (!s.contains('\\')) {
      buffer.append(s)
      return
    }
    for (c in s) {
      if (c == '\\') {
        buffer.append('\\')
      }
      buffer.append(c)
    }
  }

  private val OCTAL_REGEX = "[0-7]{3}".toRegex()

  private fun unescapeStringCharacters(length: Int, s: String, buffer: StringBuilder, supportInterpolations: Boolean, safe: Boolean) {
    var escaped = false
    var idx = 0
    while (idx < length) {
      val ch = s[idx]

      if (!escaped && supportInterpolations && (ch == '$' || ch == '%') && idx + 1 < length && s[idx + 1] == '{' && (idx == 0 || s[idx - 1] != ch)) {
        // Interpolation
        idx += 2
        buffer.append(ch).append('{')
        var braces = 1
        while (idx < length && braces > 0) {
          val ch2 = s[idx]
          if (ch2 == '\\') {
            if (idx + 1 < length && s[idx + 1] == '\\') {
              idx++
            }
          } else if (ch2 == '{') {
            braces++
          } else if (ch2 == '}') {
            braces--
          }
          buffer.append(ch2)
          idx++
        }
        continue
      }
      if (ch == '\n') if (safe) buffer.append(ch) else throw Exception("Illegal character: \\n")

      if (!escaped) {
        if (ch == '\\') {
          escaped = true
        } else {
          buffer.append(ch)
        }
      } else {
        when (ch) {
          'a' -> buffer.append(0x07.toChar())
          'b' -> buffer.append('\b')
          'f' -> buffer.append(0x0c.toChar())
          'n' -> buffer.append('\n')
          't' -> buffer.append('\t')
          'r' -> buffer.append('\r')
          'v' -> buffer.append(0x0b.toChar())

          '\\' -> buffer.append('\\')
          '"' -> buffer.append('"')
//          '\'' -> buffer.append('\'')

          'u' -> if (idx + 4 < length) {
            val code: Int
            try {
              code = Integer.parseInt(s.substring(idx + 1, idx + 5), 16)
              idx += 4
              buffer.append(code.toChar())
            } catch(e: NumberFormatException) {
              if (safe) buffer.append('\\').append(ch)
              else throw e
            }
          } else if (safe) buffer.append('\\').append(ch) else throw Exception("Illegal character: unfinished \\u")

          'U' -> if (idx + 8 < length) {
            try {
              val code = java.lang.Long.parseLong(s.substring(idx + 1, idx + 9), 16)
              idx += 8
              buffer.append(code.toChar())
            } catch(e: NumberFormatException) {
              if (safe) buffer.append('\\').append(ch)
              else throw e
            }
          } else if (safe) buffer.append('\\').append(ch) else throw Exception("Illegal character: unfinished \\U")

          'X' -> if (idx + 2 < length) {
            try {
              val code = Integer.parseInt(s.substring(idx + 1, idx + 3), 16)
              idx += 2
              buffer.append(code.toChar())
            } catch(e: NumberFormatException) {
              if (safe) buffer.append('\\').append(ch)
              else throw e
            }
          } else if (safe) buffer.append('\\').append(ch) else throw Exception("Illegal character: unfinished \\U")

          in '0'..'7' -> if (idx + 2 < length) {
            val sub = s.substring(idx, idx + 3)
            if (sub.matches(OCTAL_REGEX)) {
              try {
                val code = Integer.parseInt(sub, 8)
                idx += 2
                buffer.append(code.toChar())
              } catch(e: NumberFormatException) {
                if (safe) buffer.append('\\').append(ch)
                else throw e
              }
            } else if (safe) buffer.append('\\').append(ch) else throw Exception("Illegal character: incorrect octal: $sub")
          } else if (safe) buffer.append('\\').append(ch) else throw Exception("Illegal character: unfinished octal")

          else -> if (safe) buffer.append('\\').append(ch) else throw Exception("Illegal character: ${ch.code}")
        }
        escaped = false
      }
      idx++
    }

    if (escaped) if (safe) buffer.append('\\') else throw Exception("Illegal character: unfinished escaping")
  }
}