// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.stubs.JSFrameworkMarkersIndex
import com.intellij.lang.javascript.psi.stubs.TypeScriptSingleTypeStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptTypeArgumentListStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptUnionOrIntersectionTypeStub
import com.intellij.lang.javascript.psi.types.JSModuleTypeImpl
import com.intellij.lang.typescript.modules.TypeScriptNodeSearchProcessor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.stubs.Stub
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.source.VueEntityDescriptor

object VueTypedEntitiesProvider {

  private val defineComponentRegex = Regex("import\\s*\\(\\s*['\"]vue['\"]\\s*\\)\\s*\\.\\s*DefineComponent")

  fun isComponentDefinition(definition: JSQualifiedNamedElement): Boolean {
    if (definition.name == null || definition is JSField) return false
    when (definition) {
      is TypeScriptVariable -> {
        val typeElement = definition.typeElement ?: return false

        var result = false
        val typeStub = (typeElement as? StubBasedPsiElement<*>)?.stub

        fun checkTypeName(typeName: String?) =
          typeName != null && (typeName == "DefineComponent" || typeName.matches(defineComponentRegex))

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
          typeElement.accept(object : JSElementVisitor() {
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
      is TypeScriptClass -> return TypeScriptUtil.isDefinitionFile(definition.containingFile)
      else -> return false
    }
  }

  fun getComponentDescriptor(element: PsiElement?): VueEntityDescriptor? {
    if (element == null) return null
    val source = if (element is JSPsiNamedElementBase) {
      element.resolveIfImportSpecifier()
    }
    else element
    val componentDefinition: JSQualifiedNamedElement? = resolveElementTo(element, TypeScriptVariable::class,
                                                                         TypeScriptInterfaceClass::class)
    return componentDefinition?.takeIf { isComponentDefinition(it) }?.let {
      VueTypedComponentDescriptor(source, (source as? PsiNamedElement)?.name ?: componentDefinition.name!!)
    }
  }

  fun getComponent(descriptor: VueEntityDescriptor?): VueTypedComponent? =
    (descriptor as? VueTypedComponentDescriptor)?.let {
      VueTypedComponent(it.source, it.name)
    }

  fun calculateDtsComponents(moduleDir: PsiDirectory): Map<String, VueComponent> {
    val componentsFromDts = mutableMapOf<String, VueComponent>()
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