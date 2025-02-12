// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.Type
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLLiteral
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.hil.psi.ILLiteralExpression
import java.util.Locale

class TfElementGenerator(val project: Project) : HCLElementGenerator(project) {

  private fun createDummyFile(content: String, original: PsiFile?): PsiFile {
    val psiFileFactory = PsiFileFactory.getInstance(project)
    val psiFile = if (original != null) {
      psiFileFactory.createFileFromText("dummy.${original.fileType.defaultExtension}", original.fileType, content)
        .also { it.putUserData(PsiFileFactory.ORIGINAL_FILE, original) } //The same as com.intellij.psi.impl.PsiFileFactoryImpl.createFileFromText(java.lang.CharSequence, com.intellij.psi.PsiFile)
    } else {
      psiFileFactory.createFileFromText("dummy.${TerraformFileType.defaultExtension}", TerraformFileType, content)
    }
    if (PsiTreeUtil.hasErrorElements(psiFile as PsiElement)) {
      throw IllegalStateException("PsiFile contains PsiErrorElement: " + DebugUtil.psiToString(psiFile, false, true))
    }
    return psiFile
  }

  override fun createDummyFile(content: String): PsiFile {
    return createDummyFile(content, null)
  }

  fun createBlock(name: String, properties: Map<String, String> = emptyMap(), vararg namedElements: String, original: PsiFile? = null): HCLBlock {
    val nameString = if (!isIdentifier(name)) '"' + name + '"' else name
    val typeString = namedElements.joinToString(" ") { str ->
      if (StringUtil.isQuotedString(str)) str
      else {
        val builder = StringBuilder(str)
        StringUtil.quote(builder, '"')
        builder.toString()
      }
    }
    val propertiesString = properties.map { (name, value) -> createPropertyString(name, "\"${StringUtil.unquoteString(value)}\"") }.joinToString("\n", "\t")
    val content = """
      $nameString ${typeString} {
      ${propertiesString}
      }
     """.trimIndent()
    val file = createDummyFile(content, original)
    return file.firstChild as HCLBlock
  }

  fun createObjectProperty(name: String, value: String): HCLProperty {
    val s = """
      $name = $value
    """.trimIndent()
    val file = createDummyFile(s)
    return file.childrenOfType<HCLProperty>().first()
  }


  fun createVariable(name: String, type: Type?, initializer: ILExpression): HCLBlock {
    // TODO: Improve
    val value = when (initializer) {
      is ILLiteralExpression -> initializer.text
      else -> "\"\${${initializer.text}}\""
    }
    return createVariable(name, type, value)
  }

  fun createVariable(name: String, type: Type?, value: String): HCLBlock {
    val content = buildString {
      append("variable \"").append(name).append("\" {")
      val typeName = when (type) {
        null -> null
        else -> type.presentableText.lowercase(Locale.getDefault())
      }
      if (typeName != null) {
        append("\n  type=").append(typeName)
      }
      append("\n  default=").append(value).append("\n}")
    }
    val file = createDummyFile(content)
    return file.firstChild as HCLBlock
  }

  fun createVariable(name: String, type: Type?, initializer: HCLElement): PsiElement {
    // TODO: Improve
    val value = when (initializer) {
      is HCLLiteral -> initializer.text
      else -> initializer.text
    }
    return createVariable(name, type, value)
  }
}
