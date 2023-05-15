// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.text.CharArrayUtil
import org.intellij.terraform.hcl.HCLElementTypes.*
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLPsiUtil

class HCLBlock(val parent: HCLBlock?,
               node: ASTNode,
               wrap: Wrap?,
               alignment: Alignment?,
               private val spacingBuilder: SpacingBuilder,
               private val _indent: Indent?,
               val settings: HCLCodeStyleSettings,
               private val valueAlignment: Alignment? = null) : AbstractBlock(node, wrap, alignment) {
  private val myChildWrap: Wrap?

  init {
    myChildWrap = when (node.elementType) {
      BLOCK_OBJECT -> Wrap.createWrap(settings.OBJECT_WRAPPING, true)
      OBJECT -> Wrap.createWrap(settings.OBJECT_WRAPPING, true)
      ARRAY, FOR_ARRAY_EXPRESSION, FOR_OBJECT_EXPRESSION -> Wrap.createWrap(settings.ARRAY_WRAPPING, true)
      else -> null
    }
  }

  companion object {
    private val OPEN_BRACES: TokenSet = TokenSet.create(L_CURLY, L_BRACKET)
    private val CLOSE_BRACES: TokenSet = TokenSet.create(R_CURLY, R_BRACKET)
    private val ALL_BRACES: TokenSet = TokenSet.orSet(OPEN_BRACES, CLOSE_BRACES)
    private val PARENS: TokenSet = TokenSet.create(L_PAREN, R_PAREN)
  }

  override fun buildChildren(): List<Block> {
    val children = myNode.getChildren(null)
    if (children.isEmpty()) return EMPTY
    var propertyValueAlignment: Alignment? =
      if (settings.PROPERTY_ALIGNMENT == HCLCodeStyleSettings.DO_NOT_ALIGN_PROPERTY) null
      else if (node.isElementType(OBJECT, BLOCK_OBJECT) || node.isFile()) Alignment.createAlignment(true)
      else null


    if (propertyValueAlignment == null) {
      return children.mapNotNull {
        if (it.isWhitespaceOrEmpty()) null
        else makeSubBlock(it, null)
      }.toMutableList()
    }

    return children.mapNotNull {
      val ret =
        if (it.isWhitespaceOrEmpty()) null
        else makeSubBlock(it, propertyValueAlignment)

      // readjust PVA if needed
      if (it.textContains('\n')) {
        if (it.elementType == TokenType.WHITE_SPACE) {
          if (it.isHasTwoOrMoreLineSeparators()) {
            propertyValueAlignment = Alignment.createAlignment(true)
          }
        }
        else if (it.isElementType(PROPERTY) && it.lastChildNode.isElementType(HEREDOC_LITERAL)) {
          // That's fine, heredocs are ignored in such context
        }
        else {
          propertyValueAlignment = Alignment.createAlignment(true)
        }
      }

      ret
    }.toMutableList()
  }

  private fun makeSubBlock(childNode: ASTNode, propertyValueAlignment: Alignment?): HCLBlock {
    var indent = Indent.getNoneIndent()
    var alignment: Alignment? = null
    var wrap: Wrap? = null

    if (myNode.isElementType(HCLTokenTypes.HCL_CONTAINERS)) {
      assert(myChildWrap != null) { "myChildWrap should not be null for container, ${myNode.elementType}" }

      if (childNode.isElementType(COMMA)) {
        wrap = Wrap.createWrap(WrapType.NONE, true)
      }
      else if (!childNode.isElementType(ALL_BRACES)) {
        wrap = myChildWrap!!
        indent = Indent.getNormalIndent()
      }
      else if (childNode.isElementType(OPEN_BRACES)) {
        if (HCLPsiUtil.isPropertyValue(myNode.psi) && settings.PROPERTY_ALIGNMENT == HCLCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE) {
          // WEB-13587 Align compound values on opening brace/bracket, not the whole block
          assert(valueAlignment != null)
          alignment = valueAlignment
        }
      }
    }
    else if (myNode.isElementType(PROPERTY)) {
      // Handle properties alignment
      val pva = valueAlignment
      if (childNode.isElementType(EQUALS) && settings.PROPERTY_ALIGNMENT == HCLCodeStyleSettings.ALIGN_PROPERTY_ON_EQUALS) {
        assert(pva != null) { "Expected not null PVA, node ${node.elementType}, parent ${parent?.node?.elementType}" }
        alignment = pva
      }
      else if (HCLPsiUtil.isPropertyValue(childNode.psi) && settings.PROPERTY_ALIGNMENT == HCLCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE) {
        assert(pva != null) { "Expected not null PVA, node ${node.elementType}, parent ${parent?.node?.elementType}" }
        if (!childNode.isElementType(HCLTokenTypes.HCL_CONTAINERS)) {
          // WEB-13587 Align compound values on opening brace/bracket, not the whole block
          alignment = pva
        }
      }
    }
    else if (myNode.isElementType(PARAMETER_LIST)) {
      if (!childNode.isElementType(PARENS)) {
        indent = Indent.getNormalIndent()
      }
    }
    else if (myNode.isElementType(HEREDOC_LITERAL)) {
      if (this.textRange == myNode.textRange) {
        if (childNode.isElementType(HEREDOC_CONTENT, HEREDOC_MARKER, HD_LINE, HD_MARKER)) {
          wrap = Wrap.createWrap(WrapType.NONE, false)
          indent = Indent.getAbsoluteNoneIndent()
        }
        else if (childNode.isElementType(HD_START)) {
          wrap = Wrap.createWrap(WrapType.NONE, false)
          indent = Indent.getNoneIndent()
        }
      }
    }
    return HCLBlock(this, childNode, wrap, alignment, spacingBuilder, indent, settings, propertyValueAlignment ?: valueAlignment)
  }

  override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
    return ChildAttributes(childIndent, getFirstChildAlignment(newChildIndex))
  }

  override fun getChildIndent(): Indent? {
    if (myNode.isElementType(HEREDOC_LITERAL, HEREDOC_MARKER, HEREDOC_CONTENT, HD_MARKER, HD_LINE, HD_START)) {
      return Indent.getAbsoluteNoneIndent()
    }
    if (myNode.isElementType(OBJECT, BLOCK_OBJECT)) {
      return Indent.getNormalIndent()
    }
    if (myNode.isElementType(ARRAY)) {
      return Indent.getNormalIndent()
    }
    if (myNode.isElementType(PARAMETER_LIST)) {
      return Indent.getNormalIndent()
    }
    if (myNode.isFile()) {
      return Indent.getNoneIndent()
    }
    return null
  }

  private fun getFirstChildAlignment(newChildIndex: Int): Alignment? {
    if (myNode.isElementType(OBJECT) || myNode.isFile()) {
      return null
    }
    if (myNode.isElementType(PROPERTY)) {
      if (newChildIndex == 1 && settings.PROPERTY_ALIGNMENT == HCLCodeStyleSettings.ALIGN_PROPERTY_ON_EQUALS) {
        // equals
        return valueAlignment
      }
      if (newChildIndex == 2 && settings.PROPERTY_ALIGNMENT == HCLCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE) {
        // Property value
        return valueAlignment
      }
    }
    return null
  }

  override fun getIndent(): Indent? {
    return _indent
  }

  override fun isLeaf(): Boolean {
    if (myNode.isElementType(HEREDOC_CONTENT, HEREDOC_MARKER, HD_LINE, HD_MARKER)) return true
    return myNode.firstChildNode == null
  }

  override fun getSpacing(child1: Block?, child2: Block): Spacing? {
    return spacingBuilder.getSpacing(this, child1, child2)
  }
}

private fun ASTNode.isHasTwoOrMoreLineSeparators(): Boolean {
  val first = indexOf(chars, '\n')
  return first != -1 && indexOf(chars, '\n', first + 1) != -1
}

private fun indexOf(charSequence: CharSequence, c: Char, start: Int = 0): Int {
  val len = charSequence.length
  if (len > 5) {
    val chars = CharArrayUtil.fromSequenceWithoutCopying(charSequence)
    if (chars != null) {
      for (i in start until chars.size) {
        if (chars[i] == c) return i
      }
      return -1
    }
  }
  for (i in start until len) {
    if (c == charSequence[i]) return i
  }
  return -1
}

private fun ASTNode?.isElementType(set: TokenSet): Boolean {
  if (this == null) return false
  return set.contains(this.elementType)
}

private fun ASTNode?.isElementType(vararg types: IElementType): Boolean {
  if (this == null) return false
  return types.contains(this.elementType)
}

private fun ASTNode.isFile(): Boolean {
  return this.elementType is IFileElementType
}

private fun ASTNode.isWhitespaceOrEmpty(): Boolean {
  return this.elementType == TokenType.WHITE_SPACE || this.textLength == 0
}