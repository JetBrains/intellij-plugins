/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hcl.psi.impl

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.SmartList
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.Icons
import org.intellij.terraform.hcl.psi.*
import javax.swing.Icon

val CharSequence.indentation: Int
  get() {
    var x = 0
    forEach {
      if (it.isWhitespace()) x++
      else return x
    }
    return x
  }

object HCLPsiImplUtils {
  private val LOG = Logger.getInstance(HCLPsiImplUtils::class.java)

  fun getName(property: HCLProperty): String = when (val identifier = property.nameElement) {
    is HCLStringLiteral -> identifier.value
    is HCLIdentifier -> identifier.id
    else -> StringUtil.unescapeStringCharacters(identifier.text)
  }

  fun getName(block: HCLBlock): String = when (val identifier = block.nameIdentifier) {
    null -> getFullName(block)
    is HCLStringLiteral -> identifier.value
    is HCLIdentifier -> identifier.id
    else -> identifier.text
  }

  fun getFullName(block: HCLBlock): String {
    val elements = block.nameElements
    val sb = StringBuilder()
    for (element in elements) {
      sb.append(StringUtil.unescapeStringCharacters(HCLPsiUtil.stripQuotes(element.text))).append(' ')
    }
    return sb.toString().trim()
  }

  fun getName(marker: HCLHeredocMarker): String {
    return marker.firstChild.text
  }

  fun getNameElement(property: HCLProperty): HCLExpression {
    val firstChild = property.firstChild
    assert(firstChild is HCLExpression) { "Excepted expression, got ${firstChild.javaClass.name}" }
    return firstChild as HCLExpression
  }

  fun getNameElements(block: HCLBlock): Array<HCLElement> {
    var result: MutableList<HCLElement>? = null
    var child: PsiElement? = block.firstChild
    while (child != null) {
      if (child is HCLIdentifier || child is HCLStringLiteral) {
        if (result == null) result = SmartList<HCLElement>()
        //noinspection unchecked
        result.add(child as HCLElement)
      }
      child = child.nextSibling
    }
    return result?.toTypedArray() ?: emptyArray()
  }

  fun getValue(property: HCLProperty): HCLExpression? {
    return PsiTreeUtil.getNextSiblingOfType<HCLExpression>(getNameElement(property), HCLExpression::class.java)
  }

  fun getObject(block: HCLBlock): HCLObject? {
    return PsiTreeUtil.getNextSiblingOfType<HCLObject>(block.firstChild, HCLObject::class.java)
  }

  fun isQuotedString(literal: HCLLiteral): Boolean {
    return literal.node.findChildByType(HCLTokenTypes.STRING_LITERALS) != null
  }

  open class AbstractItemPresentation(val element: HCLElement) : ItemPresentation {
    override fun getPresentableText(): String? {
      if (element is PsiNamedElement) {
        return (element as PsiNamedElement).name
      }
      return null
    }

    override fun getLocationString(): String? {
      // TODO: Implement
      if (true) return null
      if (element.parent is HCLFile) {
        return element.containingFile.containingDirectory.name
      } else if (element.parent is HCLObject) {
        val pp = element.parent.parent
        if (pp is HCLBlock) {
          return pp.fullName
        } else if (pp is PsiNamedElement) {
          return pp.name
        }
      }
      return null
    }

    override fun getIcon(unused: Boolean): Icon? {
      return null
    }
  }

  fun getPresentation(property: HCLProperty): ItemPresentation {
    return object : AbstractItemPresentation(property) {
      override fun getIcon(unused: Boolean): Icon {
        if (property.value is HCLArray) {
          return AllIcons.Json.Array
        }
        if (property.value is HCLObject) {
          return AllIcons.Json.Object
        }
        return Icons.Property
      }
    }
  }

  fun getPresentation(block: HCLBlock): ItemPresentation {
    return object : AbstractItemPresentation(block) {
      override fun getPresentableText(): String {
        return block.fullName
      }

      override fun getIcon(unused: Boolean): Icon {
        if (block.`object` is HCLArray) {
          return AllIcons.Json.Array
        }
        if (block.`object` is HCLObject) {
          return AllIcons.Json.Object
        }
        return Icons.Property
      }
    }
  }

  fun getPresentation(array: HCLArray): ItemPresentation {
    return object : AbstractItemPresentation(array) {
      override fun getPresentableText(): String {
        return ("hcl.array")
      }

      override fun getIcon(unused: Boolean): Icon {
        return Icons.Array
      }
    }
  }

  fun getPresentation(o: HCLObject): ItemPresentation {
    return object : AbstractItemPresentation(o) {
      override fun getPresentableText(): String {
        return ("hcl.object")
      }

      override fun getIcon(unused: Boolean): Icon {
        return Icons.Object
      }
    }
  }

  //  public static void delete(@NotNull HCLProperty property) {
  //    final ASTNode myNode = property.getNode();
  //    HCLPsiChangeUtils.removeCommaSeparatedFromList(myNode, myNode.getTreeParent());
  //  }

  fun findProperty(`object`: HCLObject, name: String): HCLProperty? {
    for (property in `object`.propertyList) {
      if (property.name == name) {
        return property
      }
    }
    return null
  }

  fun getValue(literal: HCLStringLiteral): String {
    val stripQuotes = HCLPsiUtil.stripQuotes(literal.text)
    val interpolations = literal.isInHCLFileWithInterpolations()
    val out = StringBuilder(stripQuotes.length)
    val decode = HCLStringLiteralTextEscaper.parseStringCharacters(stripQuotes, out, null, interpolations)
    if (decode) return out.toString()
    else return HCLQuoter.unquote(literal.text, interpolations, true) // TODO: Could be replaced with #getTextFragments?
  }

  fun getQuoteSymbol(literal: HCLStringLiteral): Char {
    return literal.text[0]
  }

  fun getValue(literal: HCLHeredocLiteral): String {
    return getValue(literal.content, literal.indentation ?: 0)
  }

  fun isIndented(literal: HCLHeredocLiteral): Boolean {
    return literal.markerStart.text.startsWith('-')
  }

  fun getIndentation(literal: HCLHeredocLiteral): Int? {
    if (!isIndented(literal)) return null
    val markerEnd = literal.markerEnd ?: return null
    val indentation = markerEnd.text.indentation
    val contentMinIndentation = literal.content.minimalIndentation ?: return indentation
    if (contentMinIndentation < indentation) return 0
    return indentation
  }

  fun getMinimalIndentation(content: HCLHeredocContent): Int? {
    val children = content.node.getChildren(null)
    var prev: ASTNode? = null
    var indentation: Int = Int.MAX_VALUE
    for (child in children) {
      if (child.elementType == HCLElementTypes.HD_EOL) {
        if (prev?.elementType == HCLElementTypes.HD_EOL) {
          return 0
        }
      } else {
        indentation = Math.min(indentation, child.chars.indentation)
      }
      prev = child
    }
    if (indentation == Int.MAX_VALUE) return null
    else return indentation
  }

  fun getValue(content: HCLHeredocContent, trimFirst: Int = 0): String {
    val builder = StringBuilder()
    val children = content.node.getChildren(null)
    children
        .forEach {
          if (it.elementType == HCLElementTypes.HD_EOL) {
            builder.append(it.chars) // TODO: maybe just simple '\n' ?
          } else {
            builder.append(it.chars.substring(trimFirst))
          }
        }
    return builder.toString()
  }

  fun getLines(content: HCLHeredocContent): List<String> {
    val parent = content.parent
    val indentation: Int =
        if (parent is HCLHeredocLiteral) parent.indentation ?: 0
        else 0
    return getLinesInternal(content, indentation, false)
  }

  fun getLinesWithEOL(content: HCLHeredocContent): List<String> {
    val parent = content.parent
    val indentation: Int =
        if (parent is HCLHeredocLiteral) parent.indentation ?: 0
        else 0
    return getLinesInternal(content, indentation, true)
  }

  fun getLinesRaw(content: HCLHeredocContent): List<CharSequence> {
    return getLinesInternal(content, eols = false)
  }

  fun getLinesInternal(content: HCLHeredocContent, indentation: Int = 0, eols: Boolean = true): List<String> {
    val children = content.node.getChildren(null)
    val result = ArrayList<String>((children.size + 1) / 2)
    var prev: ASTNode? = null
    for (child in children) {
      if (child.elementType == HCLElementTypes.HD_EOL) {
        if (prev != null) {
          result.add(prev.text.substring(indentation) + if (eols) child.text else "")
          prev = null
        } else {
          result.add(if (eols) child.text else "")
        }
      } else {
        assert(prev == null) { "Unexpected heredoc content:\n" + DebugUtil.psiToString(content, true) }
        prev = child
      }
    }
    if (prev != null) {
      result.add(prev.text.substring(indentation))
    }
    return result
  }

  fun getLinesCount(content: HCLHeredocContent): Int {
    val node = content.node
    var cn: ASTNode? = node.firstChildNode
    var counter = 0
    while (cn != null) {
      if (cn.elementType == HCLElementTypes.HD_EOL) counter++
      cn = cn.treeNext
    }
    return counter
  }

  fun getValue(literal: HCLBooleanLiteral): Boolean {
    return literal.textMatches("true")
  }

  fun getValue(literal: HCLNumberLiteral): Number {
    val text = literal.text
    return getNumericValue(text)
  }

  /**
   * Returns either Double, Integer or Long
   */
  // TODO: Check support of hex values and other formats
  private fun getNumericValue(text: String): Number {
    val base: String
    val suffix: Long
    if (text.last().toUpperCase() in "KMGB") {
      val index: Int
      if (text[text.lastIndex - 1].toUpperCase() in "KMGB") {
        index = text.lastIndex - 1
      } else {
        index = text.lastIndex
      }
      base = text.substring(0, index)
      suffix = getSuffixValue(text.substring(index))
    } else {
      base = text
      suffix = 1
    }
    base.toIntOrNull()?.let {
      val l = it * suffix
      if (l > Int.MAX_VALUE) return l
      return l.toInt()
    }
    base.toDoubleOrNull()?.let {
      return it * suffix
    }
    throw NumberFormatException("Non-number value '$base'")
  }

  private fun getSuffixValue(suffix: String): Long {
    val base: Int
    when(suffix.length) {
      1 -> {
        base = 1000
      }
      2 -> {
        LOG.assertTrue(suffix[1].toLowerCase() == 'b', "Second suffix char must be 'b' or 'B': $suffix")
        base = 1024
      }
      else -> throw IllegalArgumentException("Unsupported suffix '$suffix'")
    }
    when (suffix[0].toLowerCase()) {
      'k' -> return pow(base, 1)
      'm' -> return pow(base, 2)
      'g' -> return pow(base, 3)
      else -> throw IllegalArgumentException("Unsupported suffix '$suffix'")
    }
  }

  private fun pow(a: Int, b: Int): Long {
    if (b == 0) return 1
    var result = a.toLong()
    for (i in 1.rangeTo(b - 1)) {
      result *= a
    }
    return result
  }


  fun getId(identifier: HCLIdentifier): String {
    return identifier.firstChild.text
  }
}
