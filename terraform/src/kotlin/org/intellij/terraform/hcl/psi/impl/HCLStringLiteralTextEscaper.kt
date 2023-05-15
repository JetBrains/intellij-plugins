// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import org.intellij.terraform.hcl.psi.isInHCLFileWithInterpolations

class HCLStringLiteralTextEscaper(host: HCLStringLiteralMixin,
                                  val interpolations: Boolean = host.isInHCLFileWithInterpolations()) : LiteralTextEscaper<HCLStringLiteralMixin>(host) {
  private var outSourceOffsets: IntArray? = null

  override fun isOneLine(): Boolean = false

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    val subText:String = rangeInsideHost.substring(myHost.text)
    val array = IntArray(subText.length + 1)
    val success = parseStringCharacters(subText, outChars, array, interpolations)
    outSourceOffsets = array
    return success
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
    val offsets = outSourceOffsets ?: throw IllegalStateException("#decode was not called")
    val result = if (offsetInDecoded < offsets.size) offsets[offsetInDecoded] else -1
    if (result == -1) return -1
    return result.coerceAtMost(rangeInsideHost.length) + rangeInsideHost.startOffset
  }

  override fun getRelevantTextRange(): TextRange {
    if (myHost.textLength <= 1) return TextRange.EMPTY_RANGE
    return TextRange.create(1, myHost.textLength - 1)
  }

  companion object {
    fun parseStringCharacters(chars: String, outChars: StringBuilder, sourceOffsets: IntArray?, interpolations: Boolean): Boolean {
      assert(sourceOffsets == null || sourceOffsets.size == chars.length + 1)

      if (chars.indexOf('\\') < 0) {
        outChars.append(chars)
        if (sourceOffsets != null) for (i in sourceOffsets.indices) {
          sourceOffsets[i] = i
        }
        return true
      }

      var index = 0
      val outOffset = outChars.length
      var braces = 0
      while (index < chars.length) {
        var c = chars[index++]
        if (sourceOffsets != null) {
          sourceOffsets[outChars.length - outOffset] = index - 1
          sourceOffsets[outChars.length + 1 - outOffset] = index
        }

        if (interpolations && braces == 0 && c == '$' && index < chars.length && chars[index] == '{') {
          outChars.append(c)
          c = chars[index++]
          if (sourceOffsets != null) {
            sourceOffsets[outChars.length - outOffset] = index - 1
            sourceOffsets[outChars.length + 1 - outOffset] = index
          }
          outChars.append(c)
          braces++
          continue
        }
        if (braces > 0) {
          if (c == '{') braces++
          else if (c == '}') braces--
          outChars.append(c)
          continue
        }

        if (c != '\\') {
          outChars.append(c)
          continue
        }
        if (index == chars.length) return false
        c = chars[index++]
        when (c) {
          'a' -> outChars.append(0x07.toChar())
          'b' -> outChars.append('\b')
          'f' -> outChars.append(0x0c.toChar())
          'n' -> outChars.append('\n')
          't' -> outChars.append('\t')
          'r' -> outChars.append('\r')
          'v' -> outChars.append(0x0b.toChar())

          '\\' -> outChars.append('\\')
          '"' -> outChars.append('"')

          in '0'..'7' -> {
            // TODO: Consider simplify to just three digits
            val startC = c
            var v = c - '0'
            if (index < chars.length) {
              c = chars[index++]
              if (c in '0'..'7') {
                v = v shl 3
                v += c - '0'
                if (startC <= '3' && index < chars.length) {
                  c = chars[index++]
                  if (c in '0'..'7') {
                    v = v shl 3
                    v += c - '0'
                  } else {
                    index--
                  }
                }
              } else {
                index--
              }
            }
            outChars.append(v.toChar())
          }

          'X' -> {
            if (index + 2 <= chars.length) {
              try {
                val code = Integer.parseInt(chars.substring(index, index + 2), 16)
                //line separators are invalid here
                if (code == 0x000a || code == 0x000d) return false // WTF?
                c = chars[index]
                if (c == '+' || c == '-') return false // WTF?
                outChars.append(code.toChar())
                index += 2
              } catch (e: Exception) {
                return false
              }
            } else {
              return false
            }
          }
          'u' -> {
            if (index + 4 <= chars.length) {
              try {
                val code = Integer.parseInt(chars.substring(index, index + 4), 16)
                //line separators are invalid here
                if (code == 0x000a || code == 0x000d) return false // WTF?
                c = chars[index]
                if (c == '+' || c == '-') return false // WTF?
                outChars.append(code.toChar())
                index += 4
              } catch (e: Exception) {
                return false
              }
            } else {
              return false
            }
          }
          'U' -> {
            if (index + 8 <= chars.length) {
              try {
                val code = java.lang.Long.parseLong(chars.substring(index, index + 8), 16)
                //line separators are invalid here
                if (code == 0x000aL || code == 0x000dL) return false // WTF?
                c = chars[index]
                if (c == '+' || c == '-') return false // WTF?
                outChars.append(code.toChar())
                index += 8
              } catch (e: Exception) {
                return false
              }
            } else {
              return false
            }
          }

          else -> return false
        }
        if (sourceOffsets != null) {
          sourceOffsets[outChars.length - outOffset] = index
        }
      }
      return true
    }
  }
}
