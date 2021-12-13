// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ES6_IMPORT_DECLARATION
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.impl.CssElementTypes
import com.intellij.psi.css.inspections.CssUnusedSymbolUtils.getUnusedStyles
import com.intellij.psi.css.inspections.RemoveUnusedSymbolIntentionAction
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.castSafelyTo
import com.intellij.xml.util.HtmlUtil.STYLE_TAG_NAME
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.index.VueFileVisitor
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType

class VueExtractComponentDataBuilder(private val list: List<XmlTag>) {
  private val containingFile = list[0].containingFile
  private val scriptTag = if (containingFile is XmlFile) findScriptTag(containingFile, false) else null
  private val scriptLanguage = detectLanguage(scriptTag)
  private val templateLanguage = detectLanguage(findTemplate())
  private val styleTags = findStyles(containingFile)
  private var unusedStylesInExistingComponent: List<CssSelectorSuffix> = emptyList()

  private val importsToCopy: MutableMap<String, ES6ImportDeclaration> = mutableMapOf()
  private val refDataMap: MutableMap<XmlTag, MutableList<RefData>> = calculateProps()

  private fun calculateProps(): MutableMap<XmlTag, MutableList<RefData>> {
    val refList: List<RefData> = gatherReferences()
    val map: MutableMap<XmlTag, MutableList<RefData>> = mutableMapOf()
    refList.forEach { refData ->
      if (refData.ref is TagNameReference) {
        processVueComponent(refData.ref)
        return@forEach
      }
      val resolved = refData.resolve() ?: return@forEach
      var parentTag = PsiTreeUtil.getParentOfType(resolved, XmlTag::class.java)
      if (parentTag == null && VueJSLanguage.INSTANCE == resolved.language) {
        val host = InjectedLanguageManager.getInstance(list[0].project).getInjectionHost(resolved)
        if (host != null) {
          parentTag = PsiTreeUtil.getParentOfType(host, XmlTag::class.java)
        }
      }
      if (scriptTag != null && parentTag == scriptTag || PsiTreeUtil.isAncestor(parentTag, refData.tag, true)) {
        map.putIfAbsent(refData.tag, mutableListOf())
        map[refData.tag]!!.add(refData)
      }
    }
    return map
  }

  private fun processVueComponent(ref: TagNameReference) {
    val name = fromAsset(ref.nameElement.text)
    val foundImport = sequenceOf(findModule(scriptTag, false), findModule(scriptTag, true))
                        .filterNotNull()
                        .flatMap { JSResolveUtil.getStubbedChildren(it, ES6_IMPORT_DECLARATION).asSequence() }
                        .filterIsInstance<ES6ImportDeclaration>()
                        .firstOrNull { importDeclaration ->
                          importDeclaration.importedBindings.find { binding ->
                            !binding.isNamespaceImport && binding.name.let { it != null && name == fromAsset(it) }
                          } != null
                        }
                      ?: return
    importsToCopy[toAsset(ref.nameElement.text, true)] = foundImport
  }

  private fun gatherReferences(): List<RefData> {
    val refs = mutableListOf<RefData>()
    val injManager = InjectedLanguageManager.getInstance(list[0].project)
    list.forEach { tag ->
      PsiTreeUtil.processElements(tag) {
        refs.addAll(addElementReferences(it, tag, 0))
        true
      }
      val tagOffset = tag.textRange.startOffset
      val hosts = PsiTreeUtil.findChildrenOfType(tag, PsiLanguageInjectionHost::class.java)
      hosts.forEach { host ->
        injManager.getInjectedPsiFiles(host)?.forEach { pair: Pair<PsiElement, TextRange> ->
          PsiTreeUtil.processElements(pair.first) { element ->
            val offset = host.textRange.startOffset - tagOffset
            refs.addAll(addElementReferences(element, tag, offset + pair.second.startOffset))
            true
          }
        }
      }
    }
    return refs
  }

  private fun addElementReferences(element: PsiElement, tag: XmlTag, offset: Int): List<RefData> {
    return element.references.filter { it != null && (it as? PsiElement)?.parent !is PsiReference }.map {
      RefData(it, tag, offset)
    }
  }

  private fun generateNewTemplateContents(mapHasDirectUsage: MutableSet<String>): String {
    return list.joinToString("")
    { tag ->
      val sb = StringBuilder(tag.text)
      val tagStart = tag.textRange.startOffset
      val replaces = refDataMap[tag]?.mapNotNull {
        if (mapHasDirectUsage.contains(it.getRefName())) return@mapNotNull null
        val absRange = it.getReplaceRange() ?: return@mapNotNull null
        Trinity(absRange.startOffset - tagStart, absRange.endOffset - tagStart, it.getRefName())
      }?.sortedByDescending { it.first }
      replaces?.forEach { sb.replace(it.first, it.second, it.third) }
      sb.toString()
    }
  }

  private fun findTemplate(): XmlTag? = PsiTreeUtil.findFirstParent(list[0]) { TEMPLATE_TAG_NAME == (it as? XmlTag)?.name } as? XmlTag

  fun createNewComponent(newComponentName: String): VirtualFile? {
    val newText = generateNewComponentText(newComponentName)
    val folder: PsiDirectory = containingFile.parent ?: return null
    val virtualFile = folder.virtualFile.createChildData(this, toAsset(newComponentName, true) + ".vue")
    VfsUtil.saveText(virtualFile, newText)
    return virtualFile
  }

  private fun generateNewComponentText(newComponentName: String): String {
    // this piece of code is responsible for handling the cases when the same function is use in a call and passed further as function
    val hasDirectUsageSet = mutableSetOf<String>()
    val hasReplaceMap = mutableMapOf<String, Boolean>()
    refDataMap.forEach { pair ->
      pair.value.any {
        val state = it.getReplaceRange() == null
        val refName = it.getRefName()
        val existing = hasReplaceMap[refName]
        if (existing != null && state != existing) {
          hasDirectUsageSet.add(refName)
          true
        }
        else {
          hasReplaceMap[refName] = state
          false
        }
      }
    }

    var newText =

      """<template${langAttribute(templateLanguage)}>
${generateNewTemplateContents(hasDirectUsageSet)}
</template>
<script${langAttribute(scriptLanguage)}>${generateImports()}
export default {
  name: '$newComponentName'${generateDescriptorMembers(hasDirectUsageSet)}
}
</script>${copyStyles()}"""

    newText = psiOperationOnText(newText) { optimizeAndRemoveEmptyStyles(it) }
    newText = psiOperationOnText(newText) {
      CodeStyleManager.getInstance(containingFile.project).reformatText(it, 0, it.textRange.endOffset)
    }

    return newText
  }

  private fun psiOperationOnText(text: String, operation: (PsiFile) -> Unit): String {
    val dummyFile = PsiFileFactory.getInstance(containingFile.project).createFileFromText("a.vue", VueFileType.INSTANCE, text)
    operation(dummyFile)
    return dummyFile.text
  }

  private fun copyStyles(): String {
    if (styleTags.isEmpty()) return ""
    return "\n" + styleTags.joinToString("\n") { it.text }
  }

  private fun findStyles(file: PsiFile): List<XmlTag> {
    val xmlFile = file as? XmlFile ?: return emptyList()
    val document = xmlFile.document ?: return emptyList()
    return document.children.filter { STYLE_TAG_NAME == (it as? XmlTag)?.name }.map { it as XmlTag }
  }

  private fun optimizeAndRemoveEmptyStyles(file: PsiFile) {
    val currentlyUnused = getUnusedStyles(file)
    currentlyUnused.removeAll(unusedStylesInExistingComponent)
    currentlyUnused.forEach { suffix -> RemoveUnusedSymbolIntentionAction.removeUnused(file.project, suffix, false) }
    val toDelete = findStyles(file).filter { styleTag ->
      styleTag.isValid &&
      PsiTreeUtil.processElements(styleTag) { !(CssElementTypes.CSS_RULESET_LIST == it.node.elementType && hasMeaningfulChildren(it)) }
    }
    toDelete.forEach { styleTag -> styleTag.delete() }
  }

  private fun hasMeaningfulChildren(element: PsiElement) =
    !PsiTreeUtil.processElements({ !(it !is PsiWhiteSpace && it !is PsiComment) }, *element.children)

  private fun langAttribute(lang: String?) = if (lang == null) "" else " lang=\"$lang\""

  private fun generateImports(): String {
    if (importsToCopy.isEmpty()) return ""
    return importsToCopy.keys.sorted().joinToString("\n", "\n") {
      "import ${it} from ${importsToCopy[it]!!.fromClause?.referenceText ?: "''"}"
    }
  }

  private fun generateDescriptorMembers(mapHasDirectUsage: MutableSet<String>): String {
    val members = mutableListOf<String>()
    if (importsToCopy.isNotEmpty()) {
      members.add(importsToCopy.keys.sorted().joinToString(", ", ",\ncomponents: {", "}"))
    }
    if (refDataMap.isNotEmpty()) {
      members.add(sortedProps(true).joinToString(",\n", ",\nprops: {\n", "\n}")
      { "${it.getRefName()}: ${if (mapHasDirectUsage.contains(it.getRefName())) "{ type: Function }" else "{}"}" })
    }
    return members.joinToString("")
  }

  fun modifyCurrentComponent(newComponentName: String, currentFile: PsiFile, newPsiFile: PsiFile, editor: Editor) {
    VueInsertHandler.InsertHandlerWorker().insertComponentImport(currentFile, newComponentName, newPsiFile, editor)
    optimizeUnusedComponentsAndImports(currentFile)
  }

  fun replaceWithNewTag(replaceName: String): PsiElement {
    unusedStylesInExistingComponent = getUnusedStyles(containingFile)

    val leader = list[0]
    val newTagName = toAsset(replaceName, true) // Pascal case
    val replaceText = if (templateLanguage in setOf("pug", "jade"))
      "<template lang=\"pug\">\n$newTagName(${generateProps()})\n</template>"
    else "<template><$newTagName ${generateProps()}/></template>"
    val project = leader.project
    val dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.vue",
                                                                           VueFileType.INSTANCE, replaceText)
    val template = PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java)!!
    val newTag = PsiTreeUtil.findChildOfType(template, XmlTag::class.java)!!

    val newlyAdded = leader.replace(newTag)
    list.subList(1, list.size).forEach { it.delete() }
    return newlyAdded
  }

  private fun optimizeUnusedComponentsAndImports(file: PsiFile) {
    val componentsInitializer = objectLiteralFor(findDefaultExport(findModule(file, false)))
      ?.findProperty("components")?.value?.castSafelyTo<JSObjectLiteralExpression>()?.properties
    if (componentsInitializer != null && componentsInitializer.isNotEmpty()) {
      val names = componentsInitializer.map { toAsset(it.name ?: "", true) }.toMutableSet()
      (file as XmlFile).accept(object : VueFileVisitor() {
        override fun visitElement(element: PsiElement) {
          if (element is XmlTag) {
            names.remove(toAsset(element.name, true))
          }
          if (scriptTag != element) recursion(element)
        }
      })
      componentsInitializer.filter { it.name != null && names.contains(toAsset(it.name!!, true)) }.forEach { it.delete() }
    }
    ES6CreateImportUtil.optimizeImports(file)
    optimizeAndRemoveEmptyStyles(file)
  }

  private fun generateProps(): String {
    return sortedProps(true).joinToString(" ") {
      ":${fromAsset(it.getRefName())}=\"${it.getExpressionText()}\""
    }
  }

  private fun sortedProps(distinct: Boolean): List<RefData> {
    val flatten = refDataMap.values.flatten()
    return (if (distinct) flatten.distinctBy { it.getRefName() } else flatten).sortedBy { it.getRefName() }
  }

  private class RefData(val ref: PsiReference, val tag: XmlTag, val offset: Int) {
    fun getRefName(): String {
      val jsRef = ref as? JSReferenceExpression ?: return ref.canonicalText
      return JSResolveUtil.getLeftmostQualifier(jsRef).castSafelyTo<JSReferenceExpression>()?.referenceName ?: ref.canonicalText
    }

    fun resolve(): PsiElement? {
      val jsRef = ref as? JSReferenceExpression ?: return ref.resolve()
      return JSResolveUtil.getLeftmostQualifier(jsRef).castSafelyTo<JSReferenceExpression>()?.resolve()
    }

    fun getReplaceRange(): TextRange? {
      val call = (ref as? PsiElement)?.parent as? JSCallExpression ?: return null
      val range = call.textRange
      return TextRange(offset + range.startOffset, offset + range.endOffset)
    }

    fun getExpressionText(): String {
      return ((ref as? PsiElement)?.parent as? JSCallExpression)?.text ?: return getRefName()
    }
  }
}
