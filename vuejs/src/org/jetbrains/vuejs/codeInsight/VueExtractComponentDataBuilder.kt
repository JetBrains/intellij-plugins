package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.language.VueJSLanguage

class VueExtractComponentDataBuilder(private val list: List<XmlTag>) {
  private val containingFile = list[0].containingFile
  private val scriptTag = if (containingFile is XmlFile) findScriptTag(containingFile) else null
  private val scriptLanguage = detectLanguage(scriptTag)
  private val templateLanguage = detectLanguage(findTemplate())

  private val importsToCopy: MutableMap<String, ES6ImportDeclaration> = mutableMapOf()
  private val refDataMap: MutableMap<XmlTag, MutableList<RefData>> = calculateProps()

  private fun calculateProps(): MutableMap<XmlTag, MutableList<RefData>> {
    val refList: List<RefData> = gatherReferences()
    val map: MutableMap<XmlTag, MutableList<RefData>> = mutableMapOf()
    refList.forEach { refData ->
      if (refData.ref is VueTagNameReference) {
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

  private fun processVueComponent(ref: VueTagNameReference) {
    if (scriptTag == null) return
    val content = PsiTreeUtil.findChildOfType(scriptTag,
                                              JSEmbeddedContent::class.java) ?: return
    val variants = ref.multiResolve(false)
    val foundImport = variants.mapNotNull {
      if (!it.isValidResult || it.element == null) return@mapNotNull null
      val file = it.element!!.containingFile
      // this means it was not resolved into the other file which we can import
      if (ref.element.containingFile == file) return@mapNotNull null

      val name = FileUtil.getNameWithoutExtension(file.name)
      ES6ImportPsiUtil.findExistingES6Import(content, null, name,
                                             true) ?: ES6ImportPsiUtil.findExistingES6Import(
        content, null, file.name, true)
    }.firstOrNull() ?: return
    importsToCopy.put(toAsset(ref.nameElement.text).capitalize(), foundImport)
  }

  private fun gatherReferences(): List<RefData> {
    val refs = mutableListOf<RefData>()
    val injManager = InjectedLanguageManager.getInstance(list[0].project)
    list.forEach { tag ->
      PsiTreeUtil.processElements(tag, {
        refs.addAll(addElementReferences(it, tag, 0))
        true
      })
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
    return element.references.filter { it != null && (it as? PsiElement)?.parent !is PsiReference }.map { RefData(it, tag, offset) }
  }

  private fun generateNewTemplateContents(mapHasDirectUsage: MutableSet<String>): String {
    return list.joinToString("")
    { tag ->
      val sb = StringBuilder(tag.text)
      val tagStart = tag.textRange.startOffset
      val replaces = refDataMap[tag]?.mapNotNull {
        if (mapHasDirectUsage.contains(it.getRefName())) return@mapNotNull null
        (it.ref as PsiElement).textRange
        val absRange = it.getReplaceRange() ?: return@mapNotNull null
        Trinity(absRange.startOffset - tagStart, absRange.endOffset - tagStart, it.getRefName())
      }?.sortedByDescending { it.first }
      replaces?.forEach { sb.replace(it.first, it.second, it.third) }
      sb.toString()
    }
  }

  private fun findTemplate(): XmlTag? = PsiTreeUtil.findFirstParent(list[0], { "template" == (it as? XmlTag)?.name }) as? XmlTag

  private fun detectLanguage(tag: XmlTag?): String? = tag?.getAttribute("lang")?.value

  fun createNewComponent(newComponentName: String): VirtualFile? {
    val newText = generateNewComponentText(newComponentName) ?: return null
    val folder: PsiDirectory = containingFile.parent ?: return null
    val virtualFile = folder.virtualFile.createChildData(this, toAsset(newComponentName).capitalize() + ".vue")
    VfsUtil.saveText(virtualFile, newText)
    return virtualFile
  }

  private fun generateNewComponentText(newComponentName: String): String? {
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
        } else {
          hasReplaceMap.put(refName, state)
          false
        }
      }
    }
    val newText =
"""<template${langAttribute(templateLanguage)}>
${generateNewTemplateContents(hasDirectUsageSet)}
</template>
<script${langAttribute(scriptLanguage)}>${generateImports()}
export default {
  name: '$newComponentName'${generateDescriptorMembers(hasDirectUsageSet)}
}
</script>"""
    val dummyFile = PsiFileFactory.getInstance(list[0].project).createFileFromText("forFormat.vue", VueFileType.INSTANCE, newText)
    VueInsertHandler.reformatElement(dummyFile)
    return dummyFile.text
  }

  private fun langAttribute(lang: String?) = if (lang == null) "" else " lang=\"" + lang + "\""

  private fun generateImports(): String {
    if (importsToCopy.isEmpty()) return ""
    return importsToCopy.keys.sorted().joinToString("\n", "\n") { "import ${it} from ${importsToCopy[it]!!.fromClause?.referenceText ?: "''"}" }
  }

  private fun generateDescriptorMembers(mapHasDirectUsage: MutableSet<String>): String {
    val members = mutableListOf<String>()
    if (!importsToCopy.isEmpty()) {
      members.add(importsToCopy.keys.sorted().joinToString ( ", ", ",\ncomponents: {", "}" ))
    }
    if (!refDataMap.isEmpty()) {
      members.add(sortedProps(true).joinToString(",\n", ",\nprops: {\n", "\n}")
      { "${it.getRefName()}: ${if (mapHasDirectUsage.contains(it.getRefName())) "{ type: Function }" else "{}" }" })
    }
    return members.joinToString("")
  }

  fun modifyCurrentComponent(newComponentName: String, currentFile: PsiFile, newPsiFile: PsiFile, editor: Editor?) {
    VueInsertHandler.InsertHandlerWorker().insertComponentImport(currentFile, newComponentName, newPsiFile, editor)
    optimizeUnusedComponentsAndImports(currentFile)
  }

  fun replaceWithNewTag(replaceName: String): PsiElement {
    val leader = list[0]
    val newTagName = fromAsset(replaceName)
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
    val content = findModule(file) ?: return
    val defaultExport = ES6PsiUtil.findDefaultExport(
      content) as? JSExportAssignment
    val component = defaultExport?.stubSafeElement as? JSObjectLiteralExpression

    val components = (component?.findProperty("components")?.value as? JSObjectLiteralExpression)?.properties
    if (components != null && !components.isEmpty()) {
      val names = components.map { toAsset(it.name ?: "").capitalize() }.toMutableSet()
      (file as XmlFile).accept(object : VueFileVisitor() {
        override fun visitElement(element: PsiElement?) {
          if (element is XmlTag) {
            names.remove(toAsset(element.name).capitalize())
          }
          if (scriptTag != element) recursion(element)
        }
      })
      components.filter { it.name != null && names.contains(toAsset(it.name!!).capitalize()) }.forEach { it.delete() }
      ES6CreateImportUtil.optimizeImports(file)
    }
  }

  private fun generateProps(): String {
    return sortedProps(true).joinToString(" "){ ":${fromAsset(it.getRefName())}=\"${it.getExpressionText()}\"" }
  }

  private fun sortedProps(distinct: Boolean): List<RefData> {
    val flatten = refDataMap.values.flatten()
    return (if (distinct) flatten.distinctBy { it.getRefName() } else flatten).sortedBy { it.getRefName() }
  }

  private class RefData(val ref: PsiReference, val tag: XmlTag, val offset: Int) {
    fun getRefName(): String {
      val jsRef = ref as? JSReferenceExpression ?: return ref.canonicalText
      return JSResolveUtil.getLeftmostQualifier(jsRef).referenceName ?: ref.canonicalText
    }

    fun resolve() : PsiElement? {
      val jsRef = ref as? JSReferenceExpression ?: return ref.resolve()
      return JSResolveUtil.getLeftmostQualifier(jsRef).resolve()
    }

    fun getReplaceRange(): TextRange? {
      val call = (ref as? PsiElement)?.parent as? JSCallExpression ?: return null
      val range = call.textRange
      return TextRange(offset + range.startOffset, offset + range.endOffset)
    }

    fun getExpressionText(): String {
      return ((ref as? PsiElement)?.parent as? JSCallExpression)?.text ?: return ref.element.text
    }
  }
}