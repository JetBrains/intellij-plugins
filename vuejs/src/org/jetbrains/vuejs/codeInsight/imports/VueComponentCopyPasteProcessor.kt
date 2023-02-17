// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.imports

import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.CreateImportExportInfo
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.*
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.resolveLocally
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.parser.VueFile
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.COMPONENTS_PROP
import org.jetbrains.vuejs.model.source.NAME_PROP
import java.awt.datatransfer.DataFlavor
import kotlin.Pair
import com.intellij.openapi.util.Pair as OpenApiPair

class VueComponentCopyPasteProcessor : ES6CopyPasteProcessorBase<VueComponentCopyPasteProcessor.VueComponentImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = VUE_COMPONENT_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    return file is VueFile
           && file.langMode
             .let {
               (it == LangMode.HAS_TS && settings.isUseTypeScriptAutoImport)
               || (it != LangMode.HAS_TS && settings.isUseJavaScriptAutoImport)
             }
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is VueFile
    && context.parentOfTypes(JSExecutionScope::class, XmlTag::class, PsiFile::class, withSelf = true)
      .let { (it !is JSExecutionScope || it is XmlElement) && it != null }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean {
    var result = false
    parent.acceptChildren(object : JSRecursiveWalkingElementVisitor() {
      override fun visitJSElement(node: JSElement) {
        if (node is JSEmbeddedContentImpl && textRange.intersects(node.textRange)) {
          result = true
          stopWalking()
        }
      }
    })
    return result && isAcceptablePasteContext(parent)
  }

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    super.getExportScope(file, caret)
    ?: WriteAction.compute<PsiElement, Throwable> {
      VueComponentSourceEdit.getOrCreateScriptScope(VueModelManager.findEnclosingContainer(file))
    }

  override fun collectTransferableData(rangesWithParents: List<Pair<PsiElement, TextRange>>): VueComponentImportsTransferableData? {
    val scriptSetup = findModule(rangesWithParents[0].first, true)
    if (scriptSetup != null) {
      val elements = mutableListOf<OpenApiPair<String, ES6ImportExportDeclarationPart>>()
      val importedElements = mutableSetOf<ImportedElement>()
      rangesWithParents.forEach { (parent, range) ->
        val componentName = toAsset(FileUtil.getNameWithoutExtension(parent.containingFile.name), true)
        parent.accept(object : XmlRecursiveElementWalkingVisitor() {
          override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)
            if (XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true) {
              val capitalizedName = toAsset(tag.name, true)
              if (capitalizedName == componentName) {
                createImportedElementForComponentFile(parent.containingFile as VueFile, capitalizedName)
                  ?.let { importedElements.add(it) }
              }
              else {
                val source = JSStubBasedPsiTreeUtil.resolveLocally(capitalizedName, scriptSetup, false)
                if (source is ES6ImportExportDeclarationPart) {
                  elements.add(OpenApiPair(capitalizedName, source))
                }
              }
            }
          }
        })
      }
      importedElements.addAll(toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(elements)),
                                                 rangesWithParents.map { it.second }))
      return if (importedElements.isNotEmpty())
        VueComponentImportsTransferableData(ArrayList(importedElements), null, emptyList())
      else
        null
    }
    else {
      val components = mutableSetOf<String>()
      rangesWithParents.forEach { (parent, range) ->
        parent.accept(object : XmlRecursiveElementWalkingVisitor() {
          override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)
            if (XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true) {
              components.add(toAsset(tag.name, true))
            }
          }
        })
      }
      val path = PsiUtilCore.getVirtualFile(rangesWithParents[0].first)?.path
      return if (path != null && components.isNotEmpty())
        VueComponentImportsTransferableData(ArrayList(), path, components.toList())
      else null
    }
  }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): VueComponentImportsTransferableData =
    throw UnsupportedOperationException()

  override fun processTransferableData(values: List<VueComponentImportsTransferableData>,
                                       exportScope: PsiElement,
                                       pasteContext: PsiElement,
                                       pasteContextLanguage: Language) {
    val originFilePath = values.firstNotNullOfOrNull { it.originFilePath }
    if (originFilePath == null) {
      super.processTransferableData(values, exportScope, pasteContext, pasteContextLanguage)
    }
    else {
      val pasteContextPtr = pasteContext.createSmartPointer()
      val exportScopePtr = exportScope.createSmartPointer()
      val project = exportScope.project
      runInBackground(project, VueBundle.message("vue.progress.title.auto-importing-vue-components-on-paste")) {
        processComponentsFromOriginContext(project, pasteContextPtr, exportScopePtr,
                                           originFilePath, values.flatMapTo(HashSet()) { it.components })
      }
    }
  }

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: VueComponentImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<OpenApiPair<CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    if (imports.isEmpty()) return
    WriteAction.run<RuntimeException> {
      val componentSourceEdit = VueComponentSourceEdit.create(VueModelManager.findEnclosingContainer(pasteContext))
                                ?: return@run
      val scriptScope = componentSourceEdit.getOrCreateScriptScope() ?: return@run
      for (import in imports) {
        val info = import.first
        val elementToImport = import.second
        val name = info.effectiveName
        if (componentSourceEdit.isScriptSetup() || componentSourceEdit.addClassicPropertyReference(COMPONENTS_PROP, name)) {
          ES6ImportPsiUtil.insertJSImport(scriptScope, info, elementToImport)
        }
      }
      componentSourceEdit.reformatChanges()
    }
  }

  private fun processComponentsFromOriginContext(
    project: Project,
    pasteContextPtr: SmartPsiElementPointer<PsiElement>,
    exportScopePtr: SmartPsiElementPointer<PsiElement>,
    originFilePath: String,
    fragmentComponentNames: Set<String>,
  ) {
    val elementsToImport = ReadAction.compute<List<OpenApiPair<CreateImportExportInfo, SmartPsiElementPointer<PsiElement>>>, Throwable> {
      val pasteContext = pasteContextPtr.dereference() ?: return@compute emptyList()
      val exportScope = exportScopePtr.dereference() ?: return@compute emptyList()
      val resolveScope = JSResolveUtil.getResolveScope(exportScope)
      val sourceComponentFile = findFile(originFilePath, exportScope, resolveScope) as? VueFile ?: return@compute emptyList()

      val sourceComponent = VueModelManager.findEnclosingContainer(sourceComponentFile)
      val destinationComponent = VueModelManager.findEnclosingContainer(pasteContext)

      val destinationComponents = mutableSetOf<String>()
      destinationComponent.acceptEntities(object : VueModelVisitor() {
        override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
          destinationComponents.add(toAsset(name, true))
          return true
        }
      })

      val toImport = mutableMapOf<String, VueComponent>()
      sourceComponent.acceptEntities(object : VueModelVisitor() {
        override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
          val capitalizedName = toAsset(name, true)
          if (fragmentComponentNames.contains(capitalizedName) && !destinationComponents.contains(capitalizedName)) {
            toImport.putIfAbsent(name, component)
          }
          return true
        }
      })

      val elements = mutableListOf<OpenApiPair<String, ES6ImportExportDeclarationPart>>()
      val result = mutableSetOf<ImportedElement>()
      toImport.entries.forEach { (capitalizedName, component) ->
        val source = component.rawSource
          ?.let {
            when (it) {
              is JSProperty -> {
                it.value
                  ?.asSafely<JSReferenceExpression>()
                  ?.let(::resolveLocally)
                  ?.firstOrNull()
              }
              is JSImplicitElement -> {
                val implicitContext = it.context
                // Self referenced component
                if (implicitContext is JSProperty && implicitContext.name == NAME_PROP) {
                  JSImplicitElementImpl(capitalizedName, sourceComponentFile)
                }
                else null
              }
              else -> it
            }
          }
        if (source is ES6ImportExportDeclarationPart) {
          elements.add(OpenApiPair(capitalizedName, source))
        }
        else if (source is JSImplicitElement && source.context is VueFile) {
          createImportedElementForComponentFile(source.context as VueFile, capitalizedName)
            ?.let { result.add(it) }
        }
      }
      result.addAll(toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(elements)), emptyList()))
      result.mapNotNull {
        resolveImportedElement(it, exportScope, resolveScope)
          ?.let { el ->
            OpenApiPair(el.first, el.second.createSmartPointer())
          }
      }
    }

    if (elementsToImport.isNotEmpty()) {
      WriteAction.runAndWait<Throwable> {
        val exportScope = exportScopePtr.dereference() ?: return@runAndWait
        val pasteContext = pasteContextPtr.dereference() ?: return@runAndWait
        CommandProcessor.getInstance().executeCommand(
          project,
          {
            insertRequiredImports(pasteContext, VueComponentImportsTransferableData(ArrayList(), null, emptyList()), exportScope,
                                  elementsToImport.mapNotNull { info -> info.second.dereference()?.let { OpenApiPair(info.first, it) } },
                                  VueJSLanguage.INSTANCE)
          },
          VueBundle.message("vue.command.name.auto-import-vue-components"),
          null,
          UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION,
          PsiDocumentManager.getInstance(project).getDocument(pasteContext.containingFile)
        )
      }
    }
  }

  private fun createImportedElementForComponentFile(
    file: VueFile,
    capitalizedName: String,
  ): ImportedElement? {
    val path = getModuleNameOrPath(file)
    return if (path != null && capitalizedName.isNotBlank())
      ImportedElement(path, CreateImportExportInfo(capitalizedName, ES6ImportPsiUtil.ImportExportType.DEFAULT), false)
    else null
  }

  class VueComponentImportsTransferableData(
    list: ArrayList<ImportedElement>,
    val originFilePath: String?,
    val components: List<String>,
  ) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor {
      return VUE_COMPONENT_IMPORTS_FLAVOR
    }
  }

  companion object {
    private val VUE_COMPONENT_IMPORTS_FLAVOR = DataFlavor(VueComponentImportsTransferableData::class.java, "vue component imports")
  }
}
