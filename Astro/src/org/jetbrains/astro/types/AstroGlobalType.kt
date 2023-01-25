// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.types

import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import org.jetbrains.astro.codeInsight.ASTRO_GLOBAL_INTERFACE
import org.jetbrains.astro.codeInsight.ASTRO_PKG
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.propsInterface

class AstroGlobalType(source: JSTypeSource,
                      private val file: PsiFile)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType {

  constructor(file: PsiFile) : this(JSTypeSource(file.frontmatterScript() ?: file, JSTypeSource.SourceLanguage.TS, true), file)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    AstroGlobalType(source, file)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is AstroGlobalType && type.file == file

  override fun hashCodeImpl(): Int = file.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append(ASTRO_GLOBAL_INTERFACE).append("(").append(file.containingFile.name).append(")")
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType {
    val astroGlobal = WebJSResolveUtil.resolveSymbolFromNodeModule(file, ASTRO_PKG, ASTRO_GLOBAL_INTERFACE,
                                                                   TypeScriptInterface::class.java)
                      ?: return JSAnyType.get(source)
    val astroType = astroGlobal.jsType
    val propsType = file.frontmatterScript()?.propsInterface()?.jsType ?: JSAnyType.get(source)
    return JSGenericTypeImpl(source, astroType, propsType).asRecordType()
  }

}