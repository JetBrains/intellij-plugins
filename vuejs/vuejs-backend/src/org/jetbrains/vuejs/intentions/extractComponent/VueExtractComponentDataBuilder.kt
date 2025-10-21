// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ES6_IMPORT_DECLARATION
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.util.TextRange
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
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import com.intellij.xml.util.HtmlUtil.STYLE_TAG_NAME
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.index.VueFileVisitor
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.expr.isVueExprMetaLanguage
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.COMPONENTS_PROP
import org.jetbrains.vuejs.model.source.NAME_PROP
import org.jetbrains.vuejs.model.source.PROPS_PROP

class VueExtractComponentDataBuilder(
  private val tags: List<XmlTag>,
) {
  private val containingFile = tags[0].containingFile
  private val scriptTag = if (containingFile is XmlFile) findScriptTag(containingFile, false) else null
  private val scriptLanguage = detectLanguage(scriptTag)
  private val templateLanguage = detectLanguage(findTemplate())
  private val styleTags = findStyles(containingFile)
  private var unusedStylesInExistingComponent: List<CssSelectorSuffix> = emptyList()

  private val componentsImportMap: MutableMap<String, ES6ImportDeclaration> = mutableMapOf()

  private val refDataMap: Map<XmlTag, List<RefData>> = buildMap {
    for (refData in gatherReferences()) {
      if (refData.ref is TagNameReference) {
        processVueComponent(refData.ref)
        continue
      }

      val resolved = refData.resolve()
                     ?: continue

      var parentTag = resolved.parentOfType<XmlTag>()
      if (parentTag == null && isVueExprMetaLanguage(resolved.language)) {
        val host = InjectedLanguageManager.getInstance(tags[0].project).getInjectionHost(resolved)
        if (host != null) {
          parentTag = host.parentOfType<XmlTag>()
        }
      }

      if (scriptTag != null && parentTag == scriptTag || PsiTreeUtil.isAncestor(parentTag, refData.tag, true)) {
        compute(refData.tag) { _, oldValue ->
          if (oldValue != null) oldValue + refData else listOf(refData)
        }
      }
    }
  }

  private fun processVueComponent(ref: TagNameReference) {
    val name = fromAsset(ref.nameElement.text)
    val import = sequenceOf(false, true)
      .mapNotNull { setup -> findModule(scriptTag, setup = setup) }
      .flatMap { JSResolveUtil.getStubbedChildren(it, ES6_IMPORT_DECLARATION).asSequence() }
      .filterIsInstance<ES6ImportDeclaration>()
      .firstOrNull { importDeclaration ->
        importDeclaration.importedBindings.asSequence()
          .filter { !it.isNamespaceImport }
          .mapNotNull { it.name }
          .any { fromAsset(it) == name }
      }

    import ?: return

    componentsImportMap[toAsset(ref.nameElement.text, true)] = import
  }

  private fun gatherReferences(): List<RefData> {
    val refs = mutableListOf<RefData>()
    val injManager = InjectedLanguageManager.getInstance(tags[0].project)

    for (tag in tags) {
      PsiTreeUtil.processElements(tag) {
        refs.addAll(addElementReferences(it, tag, 0))
        true
      }

      val tagOffset = tag.textRange.startOffset
      val hosts = PsiTreeUtil.findChildrenOfType(tag, PsiLanguageInjectionHost::class.java)
      for (host in hosts) {
        injManager.getInjectedPsiFiles(host)?.forEach { pair ->
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

  private fun addElementReferences(
    element: PsiElement,
    tag: XmlTag,
    offset: Int,
  ): List<RefData> {
    return element.references
      .filter { (it as? PsiElement)?.parent !is PsiReference }
      .map { RefData(it, tag, offset) }
  }

  private fun generateNewTemplateContents(
    hasDirectUsage: Set<String>,
  ): String {
    return tags.joinToString("") { tag ->
      val dataList = refDataMap[tag]
                     ?: return@joinToString tag.text

      val builder = StringBuilder(tag.text)
      val tagStart = tag.textRange.startOffset

      dataList.asSequence()
        .filter { it.getRefName() !in hasDirectUsage }
        .mapNotNull {
          val absRange = it.getReplaceRange() ?: return@mapNotNull null
          Triple(absRange.startOffset - tagStart, absRange.endOffset - tagStart, it.getRefName())
        }
        .sortedByDescending { it.first }
        .forEach { builder.replace(it.first, it.second, it.third) }

      builder.toString()
    }
  }

  private fun findTemplate(): XmlTag? =
    PsiTreeUtil.findFirstParent(tags.first()) {
      (it as? XmlTag)?.name == TEMPLATE_TAG_NAME
    } as XmlTag?

  fun createNewComponent(newComponentName: String): VirtualFile? {
    val newText = generateNewComponentText(newComponentName)
    val folder: PsiDirectory = containingFile.parent ?: return null
    val virtualFile = folder.virtualFile.createChildData(this, toAsset(newComponentName, true) + VUE_FILE_EXTENSION)
    VfsUtil.saveText(virtualFile, newText)
    return virtualFile
  }

  private fun generateNewComponentText(newComponentName: String): String {
    // this piece of code is responsible for handling the cases when the same function is use in a call and passed further as function
    val hasDirectUsage: Set<String> = buildSet {
      val hasReplaceMap = mutableMapOf<String, Boolean>()
      for ((_, dataList) in refDataMap) {
        for (data in dataList) {
          val state = data.getReplaceRange() == null
          val refName = data.getRefName()
          val existing = hasReplaceMap[refName]
          if (existing != null && state != existing) {
            add(refName)
            break
          }

          hasReplaceMap[refName] = state
        }
      }
    }

    val componentDeclaration = optionsComponentDeclaration(newComponentName, hasDirectUsage)

    val scriptContent = listOfNotNull(
      generateImports(),
      componentDeclaration,
    ).joinToString("\n")

    return sequenceOf(
      createTag(
        name = SCRIPT_TAG_NAME,
        attributes = listOfNotNull(
          langAttribute(scriptLanguage),
        ),
        content = scriptContent,
      ),
      createTag(
        name = TEMPLATE_TAG_NAME,
        attributes = listOfNotNull(langAttribute(templateLanguage)),
        content = generateNewTemplateContents(hasDirectUsage),
      ),
    ).plus(styleTags.map { it.text })
      .joinToString("\n\n")
      .let { text -> psiOperationOnText(text, ::optimizeAndRemoveEmptyStyles) }
      .let { text ->
        psiOperationOnText(text) { file ->
          CodeStyleManager.getInstance(containingFile.project)
            .reformatText(file, 0, file.textRange.endOffset)
        }
      }
  }

  private fun createTag(
    name: String,
    attributes: List<String>,
    content: String,
  ): String {
    return listOf(
      "<$name  ${attributes.joinToString(" ")}>",
      content,
      "</$name>",
    ).joinToString("\n")
  }

  private fun psiOperationOnText(
    text: String,
    operation: (PsiFile) -> Unit,
  ): String =
    createVueFileFromText(containingFile.project, text)
      .also(operation)
      .text

  private fun findStyles(file: PsiFile): List<XmlTag> {
    val document = file.asSafely<XmlFile>()
                     ?.document
                   ?: return emptyList()

    return document.children.asSequence()
      .filterIsInstance<XmlTag>()
      .filter { it.name == STYLE_TAG_NAME }
      .toList()
  }

  private fun optimizeAndRemoveEmptyStyles(file: PsiFile) {
    getUnusedStyles(file).asSequence()
      .minus(unusedStylesInExistingComponent)
      .forEach(RemoveUnusedSymbolIntentionAction::removeUnused)

    findStyles(file)
      .filter { it.isValid }
      .filter { styleTag -> PsiTreeUtil.processElements(styleTag) { !(CssElementTypes.CSS_RULESET_LIST == it.node.elementType && hasMeaningfulChildren(it)) } }
      .forEach { it.delete() }
  }

  private fun hasMeaningfulChildren(element: PsiElement) =
    !PsiTreeUtil.processElements(
      { !(it !is PsiWhiteSpace && it !is PsiComment) },
      *element.children,
    )

  private fun langAttribute(lang: String?) =
    if (lang != null) """lang="$lang"""" else null

  private fun generateImports(): String? {
    if (componentsImportMap.isEmpty())
      return null

    return componentsImportMap.entries
      .sortedBy { it.key }
      .joinToString("\n") { (name, declaration) ->
        "import ${name} from ${declaration.fromClause?.referenceText ?: "''"}"
      }
  }

  private fun optionsComponentDeclaration(
    componentName: String,
    hasDirectUsage: Set<String>,
  ): String {
    val members = buildList {
      add(NAME_PROP to "'$componentName'")

      if (componentsImportMap.isNotEmpty()) {
        add(COMPONENTS_PROP to componentsImportMap.keys.sorted().joinToString(", ", "{", "}"))
      }
      if (refDataMap.isNotEmpty()) {
        val props = getPropReferences().joinToString(",\n", "{\n", "\n}") {
          val type = if (hasDirectUsage.contains(it.getRefName())) "{ type: Function }" else "{}"
          "${it.getRefName()}: $type"
        }

        add(PROPS_PROP to props)
      }
    }.joinToString(",\n") { (name, value) ->
      "$name: $value"
    }

    return """
    export default {
      $members
    }
    """.trimIndent()
  }

  fun modifyCurrentComponent(
    newComponentName: String,
    currentFile: PsiFile,
    newPsiFile: PsiFile,
  ) {
    VueComponentSourceEdit.create(VueModelManager.findEnclosingContainer(currentFile))
      ?.insertComponentImport(newComponentName, newPsiFile)

    optimizeUnusedComponentsAndImports(currentFile)
  }

  fun replaceWithNewTag(replaceName: String): XmlTag {
    unusedStylesInExistingComponent = getUnusedStyles(containingFile)

    val leader = tags[0]
    val newTagName = toAsset(replaceName, true) // Pascal case
    val replaceText = if (templateLanguage in setOf("pug", "jade"))
      "<template lang=\"pug\">\n$newTagName(${generateProps()})\n</template>"
    else "<template><$newTagName ${generateProps()}/></template>"
    val project = leader.project
    val dummyFile = createVueFileFromText(project, replaceText)
    val template = PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java)!!
    val newTag = PsiTreeUtil.findChildOfType(template, XmlTag::class.java)!!

    val newlyAdded = leader.replace(newTag) as XmlTag
    tags.subList(1, tags.size).forEach { it.delete() }
    return newlyAdded
  }

  private fun optimizeUnusedComponentsAndImports(file: PsiFile) {
    val componentsInitializer = objectLiteralFor(findDefaultExport(findModule(file, false)))
      ?.findProperty(COMPONENTS_PROP)?.value?.asSafely<JSObjectLiteralExpression>()?.properties
    if (!componentsInitializer.isNullOrEmpty()) {
      val names = componentsInitializer.map { toAsset(it.name ?: "", true) }.toMutableSet()
      (file as XmlFile).accept(object : VueFileVisitor() {
        override fun visitElement(element: PsiElement) {
          if (element is XmlTag) {
            names.remove(toAsset(element.name, true))
          }
          if (scriptTag != element) recursion(element)
        }
      })
      componentsInitializer
        .filter { it.name != null && names.contains(toAsset(it.name!!, true)) }
        .forEach { it.delete() }
    }
    ES6CreateImportUtil.optimizeImports(file)
    optimizeAndRemoveEmptyStyles(file)
  }

  private fun generateProps(): String {
    return getPropReferences().joinToString(" ") {
      ":${fromAsset(it.getRefName())}=\"${it.getExpressionText()}\""
    }
  }

  private fun getPropReferences(): List<RefData> =
    refDataMap.values.flatten()
      .distinctBy { it.getRefName() }
      .sortedBy { it.getRefName() }

  private class RefData(
    val ref: PsiReference,
    val tag: XmlTag,
    val offset: Int,
  ) {
    fun getRefName(): String {
      val jsRef = ref as? JSReferenceExpression
                  ?: return ref.canonicalText

      return JSResolveUtil.getLeftmostQualifier(jsRef)
               .asSafely<JSReferenceExpression>()
               ?.referenceName
             ?: ref.canonicalText
    }

    fun resolve(): PsiElement? {
      val jsRef = ref as? JSReferenceExpression
                  ?: return ref.resolve()

      return JSResolveUtil.getLeftmostQualifier(jsRef)
        .asSafely<JSReferenceExpression>()
        ?.resolve()
    }

    fun getReplaceRange(): TextRange? {
      val call = ref.asSafely<PsiElement>()
                   ?.parent
                   .asSafely<JSCallExpression>()
                 ?: return null

      val range = call.textRange
      return TextRange(offset + range.startOffset, offset + range.endOffset)
    }

    fun getExpressionText(): String {
      return ref.asSafely<PsiElement>()
               ?.parent
               .asSafely<JSCallExpression>()
               ?.text
             ?: return getRefName()
    }
  }
}
