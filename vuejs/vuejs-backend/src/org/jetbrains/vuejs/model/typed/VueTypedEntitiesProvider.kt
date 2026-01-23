// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.stubs.JSFrameworkMarkersIndex
import com.intellij.lang.javascript.psi.stubs.TypeScriptSingleTypeStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptTypeArgumentListStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptUnionOrIntersectionTypeStub
import com.intellij.lang.javascript.psi.types.JSModuleTypeImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.typescript.modules.TypeScriptNodeSearchProcessor
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.stubs.Stub
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.VueLocallyDefinedComponent
import org.jetbrains.vuejs.model.VueNamedComponent

object VueTypedEntitiesProvider {

  private val vueComponentTypenameRegex = Regex(
    """(import\s*\(\s*['"]vue['"]\s*\)\s*\.\s*|vue\s*\.\s*)?(DefineComponent|ComponentOptionsBase|ComponentOptionsMixin|ComponentCustomProps|__VLS_WithTemplateSlots|__VLS_WithSlots(\$[0-9]+)?)""")

  fun isComponentDefinition(definition: JSQualifiedNamedElement): Boolean {
    if (definition.name == null || definition is JSField) return false
    when (definition) {
      is TypeScriptVariable -> {
        val typeElement = definition.typeElement ?: return false
        if (checkType(typeElement)) {
          return true
        }
        if (typeElement is TypeScriptSingleType) {
          val aliasedType = typeElement.qualifiedTypeName
            ?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, typeElement) }
            ?.asSafely<TypeScriptTypeAlias>()
            ?.typeDeclaration

          if (aliasedType != null && checkType(aliasedType)) {
            return true
          }
        }
        return false
      }
      is TypeScriptClass -> return TypeScriptUtil.isDefinitionFile(definition.containingFile)
      else -> return false
    }
  }

  private fun checkType(typeElement: TypeScriptType): Boolean {
    var result = false
    val typeStub = (typeElement as? StubBasedPsiElement<*>)?.stub

    if (typeStub != null) {
      fun visit(stub: Stub) {
        if (stub is TypeScriptSingleTypeStub
            && checkTypeName(stub.qualifiedTypeName)) {
          result = true
        }
        else if (stub is TypeScriptTypeArgumentListStub
                 || stub is TypeScriptUnionOrIntersectionTypeStub
                 || stub is TypeScriptSingleTypeStub) {
          stub.childrenStubs.forEach { visit(it) }
        }
      }
      visit(typeStub)
    }
    else {
      typeElement.accept(object : JSElementVisitor(), PsiRecursiveVisitor {
        override fun visitJSElement(node: JSElement) {
          if (node is TypeScriptSingleType
              && checkTypeName(node.qualifiedTypeName)) {
            result = true
          }
          else if (node is TypeScriptTypeArgumentList
                   || node is TypeScriptUnionOrIntersectionType
                   || node is TypeScriptSingleType) {
            node.acceptChildren(this)
          }
        }
      })
    }
    return result
  }

  private fun checkTypeName(typeName: String?) =
    typeName != null && typeName.matches(vueComponentTypenameRegex)

  fun getComponent(element: PsiElement?): VueTypedComponent? =
    resolveElementTo(element, TypeScriptVariable::class, TypeScriptInterfaceClass::class)
      ?.takeIf { isComponentDefinition(it) }
      ?.let { VueTypedComponent.create(it) }

  fun calculateDtsComponents(moduleDir: PsiDirectory): Map<String, VueNamedComponent> {
    val componentsFromDts = mutableMapOf<String, VueNamedComponent>()
    val componentDefs = JSFrameworkMarkersIndex.getElements(
      VueFrameworkHandler.TYPED_COMPONENT_MARKER, TypeScriptVariable::class.java, moduleDir.project,
      GlobalSearchScopesCore.directoryScope(moduleDir, true)
    ).toSet()
    if (componentDefs.isEmpty()) return emptyMap()

    val searchProcessor = TypeScriptNodeSearchProcessor()
    val mainFile = JSExactFileReference.resolveForNpmPackages(moduleDir.virtualFile, searchProcessor)
    val mainPsiFile = mainFile?.let { moduleDir.manager.findFile(it) } as? JSFile
    if (mainPsiFile != null) {
      JSModuleTypeImpl(mainPsiFile, false)
        .asRecordType()
        .properties
        .forEach { export ->
          export.memberSource
            .allSourceElements
            .firstNotNullOfOrNull { memberSource ->
              if ((memberSource as? PsiNamedElement)?.name != export.memberName)
                return@firstNotNullOfOrNull null
              val variable =
                memberSource as? TypeScriptVariable
                ?: resolveElementTo(memberSource, TypeScriptVariable::class)
              if (variable != null && componentDefs.contains(variable)) {
                VueTypedComponent.create(variable)
                  ?.let {
                    if (memberSource != variable)
                      VueLocallyDefinedComponent.create(it, memberSource)
                    else
                      it
                  }
              }
              else null
            }?.let {
              componentsFromDts[it.name] = it
            }
        }
    }
    else {
      componentDefs.forEach { variable ->
        val name = variable.name
        if (variable.isExported && name != null) {
          VueTypedComponent.create(variable)?.let { componentsFromDts[name] = it }
        }
      }
    }
    return componentsFromDts
  }

}