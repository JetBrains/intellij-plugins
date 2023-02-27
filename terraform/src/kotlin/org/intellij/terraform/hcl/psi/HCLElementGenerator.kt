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
package org.intellij.terraform.hcl.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.HCLFileType

/**
 * @author Mikhail Golubev
 * @author Vladislav Rassokhin
 */
open class HCLElementGenerator(private val project: Project) {

  /**
   * Create lightweight in-memory [org.intellij.terraform.hcl.psi.HCLFile] filled with `content`.

   * @param content content of the file to be created
   * *
   * @return created file
   */
  open fun createDummyFile(content: String): PsiFile {
    val psiFileFactory = PsiFileFactory.getInstance(project)
    val psiFile = psiFileFactory.createFileFromText("dummy." + HCLFileType.defaultExtension, HCLFileType, content)
    if (PsiTreeUtil.hasErrorElements(psiFile)) {
      throw IllegalStateException("PsiFile contains PsiErrorElement: " + DebugUtil.psiToString(psiFile, true, true))
    }
    return psiFile
  }

  /**
   * Create HCL value from supplied content.

   * @param content properly escaped text of HCL value, e.g. Java literal `&quot;\&quot;new\\nline\&quot;&quot;` if you want to create string literal
   * *
   * @param T type of the HCL value desired
   * *
   * @return element created from given text
   * *
   * *
   * @see .createStringLiteral
   */
  fun <T : HCLExpression> createValue(content: String): T {
    val property = createProperty("foo", content)
    @Suppress("UNCHECKED_CAST")
    return property.value as T
  }

  fun createObject(content: String): HCLObject {
    val file = createDummyFile("foo {$content}")
    val block = file.firstChild as HCLBlock
    return block.`object`!!
  }

  /**
   * Create HCL string literal from supplied *unescaped* content.

   * @param unescapedContent unescaped content of string literal, e.g. Java literal `&quot;new\nline&quot;` (compare with [.createValue]).
   * *
   * @return HCL string literal created from given text
   */
  fun createStringLiteral(unescapedContent: String, quoteSymbol: Char? = '"'): HCLStringLiteral {
    return createValue(buildString {
      if (quoteSymbol == null) {
        if (unescapedContent.length < 2) throw IllegalArgumentException()
        append(unescapedContent.first())
        append(StringUtil.escapeStringCharacters(unescapedContent.substring(1..unescapedContent.lastIndex - 1)))
        append(unescapedContent.last())
      } else {
        append(quoteSymbol)
        append(StringUtil.escapeStringCharacters(unescapedContent))
        append(quoteSymbol)
      }
    })
  }

  fun createProperty(name: String, value: String): HCLProperty {
    val s: String
    if (isIdentifier(name)) {
      s = "$name = $value"
    } else {
      s = "\"$name\" = $value"
    }
    val file = createDummyFile(s)
    return file.firstChild as HCLProperty
  }

  private fun isIdentifier(name: String): Boolean {
    return name.matches("\\w+".toRegex()) && !name[0].isDigit()
  }

  fun createBlock(name: String): HCLBlock {
    val start = if (!isIdentifier(name)) '"' + name + '"' else name
    val file = createDummyFile("$start {}")
    return file.firstChild as HCLBlock
  }

  fun createComma(): PsiElement {
    val array = createValue<HCLArray>("[1, 2]")
    return array.elements[0].nextSibling
  }

  fun createIdentifier(name: String): HCLIdentifier {
    val file = createDummyFile("$name=true")
    val property = file.firstChild as HCLProperty
    return property.nameElement as HCLIdentifier
  }

  fun createPropertyKey(name: String): HCLExpression {
    val file = createDummyFile("x={$name=true}")
    val obj = (file.firstChild as HCLProperty).value as HCLObject
    val property = obj.firstChild.nextSibling as HCLProperty
    return property.nameElement
  }

  fun createHeredocContent(lines: List<String>, appendNewlines: Boolean = true, indented: Boolean = false, endIndent: Int = 0): HCLHeredocContent {
    val text = buildString {
      append("x=<<")
      if (indented) append('-')
      append("___EOF___\n")
      for (l in lines) {
        append(l)
        if (appendNewlines && !l.endsWith('\n')) {
          append('\n')
        }
      }
      append(" ".repeat(endIndent))
      append("___EOF___")
    }
    val file = createDummyFile(text)
    val property = file.firstChild as HCLProperty
    return (property.value as HCLHeredocLiteral).content
  }
}
