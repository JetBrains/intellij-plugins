// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.types.JSModuleTypeImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.typescript.modules.TypeScriptNodeReference
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.util.castSafelyTo
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.VueTypedComponentFilesIndex
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.source.VueEntityDescriptor

object VueTypedEntitiesProvider {

  fun isComponentDefinition(variable: TypeScriptVariable): Boolean {
    val qualifiedTypeName = variable.typeElement?.castSafelyTo<TypeScriptSingleType>()
      ?.qualifiedTypeName
    return variable.name != null
           && (qualifiedTypeName == "DefineComponent"
               || qualifiedTypeName == "import(\"vue\").DefineComponent"
               || qualifiedTypeName == "import('vue').DefineComponent")
  }

  fun getComponentDescriptor(element: PsiElement?): VueEntityDescriptor? {
    if (element == null) return null
    val source = if (element is JSPsiNamedElementBase) {
      element.resolveIfImportSpecifier()
    }
    else element
    val variable = resolveElementTo(element, TypeScriptVariable::class)
    return variable?.takeIf { isComponentDefinition(it) }?.let {
      VueTypedComponentDescriptor(source, (source as? PsiNamedElement)?.name ?: variable.name!!)
    }
  }

  fun getComponent(descriptor: VueEntityDescriptor?): VueTypedComponent? =
    (descriptor as? VueTypedComponentDescriptor)?.let {
      VueTypedComponent(it.source, it.name)
    }

  fun calculateDtsComponents(moduleDir: PsiDirectory): Map<String, VueComponent> {
    val componentsFromDts = mutableMapOf<String, VueComponent>()
    val psiManager = PsiManager.getInstance(moduleDir.project)
    val componentDefs = mutableSetOf<TypeScriptVariable>()
    FileBasedIndex.getInstance().getFilesWithKey(
      VueTypedComponentFilesIndex.VUE_TYPED_COMPONENTS_INDEX, setOf(true),
      { file ->
        psiManager.findFile(file)?.castSafelyTo<JSFile>()?.let { psiFile ->
          JSStubBasedPsiTreeUtil.processDeclarationsInScope(psiFile, { element, _ ->
            (element as? TypeScriptVariable)
              ?.takeIf { VueTypedEntitiesProvider.isComponentDefinition(it) }
              ?.let {
                componentDefs.add(it)
              }
            true
          }, false)
        }
        true
      }, GlobalSearchScopesCore.directoryScope(moduleDir, true))
    if (componentDefs.isEmpty()) return emptyMap()

    val searchProcessor = TypeScriptNodeReference.TypeScriptNodeModuleDirectorySearchProcessor()
    val mainFile = JSExactFileReference.resolveForNpmPackages(moduleDir.virtualFile, searchProcessor)
    val mainPsiFile = moduleDir.manager.findFile(mainFile) as? JSFile
    if (mainPsiFile != null) {
      JSModuleTypeImpl(mainPsiFile, false)
        .asRecordType()
        .properties
        .forEach { export ->
          export.memberSource
            .allSourceElements
            .asSequence()
            .mapNotNull {
              val variable = if (it !is TypeScriptVariable) resolveElementTo(it, TypeScriptVariable::class) else it
              if (variable != null && componentDefs.contains(variable)) {
                VueTypedComponent(it, export.memberName)
              }
              else null
            }
            .firstOrNull()?.let {
              componentsFromDts[it.defaultName] = it
            }
        }
    }
    else {
      componentDefs.forEach { variable ->
        val name = variable.name
        if (variable.isExported && name != null) {
          componentsFromDts[name] = VueTypedComponent(variable, name)
        }
      }
    }
    return componentsFromDts
  }

  class VueTypedComponentDescriptor(override val source: PsiElement,
                                    val name: String) : VueEntityDescriptor

}