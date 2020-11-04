// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.typescript.modules.TypeScriptNodeReference
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.webtypes.json.Source

class WebTypesSourceSymbolResolver(private val context: PsiFile, private val pluginName: String) {

  fun resolve(source: Source): Result<PsiElement?> {
    val properties: Map<String, *> = source.additionalProperties
    val symbolName = properties["symbol"]
    val moduleName = properties["module"]
    val file = properties["file"]
    val offset = properties["offset"]
    if (symbolName is String && moduleName is String?) {
      val modules = if (moduleName != null && moduleName.startsWith("./")) {
        context.parent
          ?.virtualFile
          ?.findFileByRelativePath(moduleName)
          ?.let { context.manager.findFile(it) }
          ?.let { listOf(it) }
      }
      else
        TypeScriptNodeReference(context, moduleName ?: pluginName, 0)
          .resolve()
          ?.castSafelyTo<JSElement>()
          ?.let { listOf(it) }

      if (modules != null) {
        for (module in modules) {
          val jsModule = when (module) {
                           is JSElement -> module
                           else -> findModule(module)
                         } ?: continue
          val exportName: String
          val names = symbolName.split('.').let {
            exportName = it[0]
            it.subList(1, it.size)
          }

          ES6PsiUtil.resolveSymbolInModule(exportName, context, jsModule).asSequence()
            .filter { it.isValidResult }
            .mapNotNull { it.element }
            .flatMap {
              var elements: List<PsiElement> = listOf(it)
              for (name in names) {
                elements = resolveQualifiedAccess(elements, name)
              }
              elements
            }
            // TODO resolve to multiple symbols
            .firstOrNull()
            ?.let { return Result.create(it, context, module, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS) }
        }
      }
      return Result.create(null, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }
    else if (file is String && offset is Int) {
      return context.parent
               ?.virtualFile
               ?.findFileByRelativePath(file)
               ?.let { context.manager.findFile(it) }
               ?.findElementAt(offset)
               ?.let {
                 PsiTreeUtil.getParentOfType(it, JSClass::class.java, JSObjectLiteralExpression::class.java,
                                             JSExportAssignment::class.java, JSNamedElement::class.java)
               }
               ?.let { Result.create(it as PsiElement, context, it) }
             ?: Result.create(null as PsiElement?, context, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }
    return Result.create(null, ModificationTracker.NEVER_CHANGED)
  }

  private fun resolveQualifiedAccess(elements: List<PsiElement>, name: String): List<PsiElement> {
    val result = mutableListOf<PsiElement>()
    for (element in elements) {
      result.addAll(ES6PsiUtil.createResolver(element)
                      .resolveQualifiedName(name))
    }
    return result
  }

}
