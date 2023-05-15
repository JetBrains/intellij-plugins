// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.util.SmartList
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.findDecorator
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import org.angular2.index.Angular2IndexingHandler
import org.angular2.index.Angular2IndexingHandler.Companion.resolveComponentsFromIndex
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlLanguage
import java.util.Collections.emptyList
import java.util.function.BiPredicate

object Angular2ComponentLocator {

  @JvmStatic
  fun findComponentClass(templateContext: PsiElement): TypeScriptClass? {
    return if (ApplicationManager.getApplication().let { it.isDispatchThread && !it.isUnitTestMode })
      WebJSResolveUtil.disableIndexUpToDateCheckIn(templateContext) {
        findComponentClasses(templateContext).firstOrNull()
      }
    else findComponentClasses(templateContext).firstOrNull()
  }

  @JvmStatic
  fun findComponentClasses(templateContext: PsiElement): List<TypeScriptClass> {
    val file = templateContext.containingFile
    if (file == null || !(file.language.isKindOf(Angular2HtmlLanguage.INSTANCE)
                          || file.language.`is`(Angular2Language.INSTANCE)
                          || isStylesheet(file))) {
      return emptyList()
    }
    val hostFile = getHostFile(templateContext) ?: return emptyList()
    if (file.originalFile != hostFile && DialectDetector.isTypeScript(hostFile)) {
      // inline content
      return listOfNotNull(getClassForDecoratorElement(
        InjectedLanguageManager.getInstance(templateContext.project).getInjectionHost(file.originalFile))
      )
    }
    // external content
    val result = SmartList(Angular2FrameworkHandler.EP_NAME.extensionList.flatMap { h -> h.findAdditionalComponentClasses(hostFile) })
    if (result.isEmpty() || isStylesheet(file)) {
      result.addAll(resolveComponentsFromSimilarFile(hostFile))
    }
    if (result.isEmpty() || isStylesheet(file)) {
      result.addAll(resolveComponentsFromIndex(hostFile) { dec -> hasFileReference(dec, hostFile) })
    }
    return result
  }

  // External usages
  @Suppress("unused")
  @JvmStatic
  fun findComponentClassesInFile(file: PsiFile, filter: BiPredicate<TypeScriptClass, ES6Decorator>?): List<TypeScriptClass> {
    return JSStubBasedPsiTreeUtil.findDescendants<PsiElement>(file, Angular2IndexingHandler.TS_CLASS_TOKENS)
      .filterIsInstance<TypeScriptClass>()
      .filter {
        val dec = findDecorator(it, COMPONENT_DEC)
        dec != null && (filter == null || filter.test(it, dec))
      }
  }

  private fun resolveComponentsFromSimilarFile(file: PsiFile): List<TypeScriptClass> {
    val name = file.viewProvider.virtualFile.nameWithoutExtension
    val dir = file.parent ?: return emptyList()
    for (ext in TypeScriptUtil.TYPESCRIPT_EXTENSIONS_WITHOUT_DECLARATIONS) {
      val directiveFile = dir.findFile(name + ext)

      if (directiveFile != null) {
        return findComponentClassesInFile(directiveFile) { _, dec -> hasFileReference(dec, file) }
      }
    }

    return emptyList()
  }

  private fun hasFileReference(componentDecorator: ES6Decorator?, file: PsiFile): Boolean {
    val component = Angular2EntitiesProvider.getComponent(componentDecorator)
    return if (component != null) {
      if (isStylesheet(file)) component.cssFiles.contains(file) else file == component.templateFile
    }
    else false
  }

  @JvmStatic
  fun isStylesheet(file: PsiFile): Boolean {
    return file is StylesheetFile
  }

  private fun getHostFile(context: PsiElement): PsiFile? {
    val original = CompletionUtil.getOriginalOrSelf(context)
    val hostFile = FileContextUtil.getContextFile(if (original !== context) original else context.containingFile.originalFile)
    return hostFile?.originalFile
  }
}
