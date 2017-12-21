package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.findExistingES6Import
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.util.PathUtilRt
import com.intellij.util.ui.FormBuilder
import com.intellij.xml.DefaultXmlExtension
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.VueInsertHandler.Companion.reformatElement
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JLabel

/**
 * @author Irina.Chernushina on 12/14/2017.
 */
class VueExtractComponentRefactoring(private val project: Project,
                                     private val list: List<XmlTag>,
                                     private val editor: Editor?) {
  fun perform(defaultName: String? = null) {
    if (list.isEmpty() ||
        list[0].containingFile == null ||
        list[0].containingFile.parent == null ||
        !CommonRefactoringUtil.checkReadOnlyStatus(project, list[0].containingFile)) return

    val componentName = defaultName ?: showDialog(list[0]) ?: return
    performRefactoring(componentName, list)
  }

  private fun performRefactoring(componentName: String, list: List<XmlTag>) {
    val data = MyData(componentName, list)

    var newPsiFile: PsiFile? = null
    var newlyAdded: PsiElement? = null

    val refactoringName = VueBundle.message("vue.template.intention.extract.component")
    CommandProcessor.getInstance().executeCommand(project, {
      PostprocessReformattingAspect.getInstance(project).postponeFormattingInside {
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        WriteAction.run<RuntimeException> {
          newPsiFile = data.generateNewComponent() ?: return@run
          newlyAdded = data.modifyCurrentComponent(newPsiFile!!, editor)
        }
      }
      reformatElement(newPsiFile)
      reformatElement(newlyAdded)
      positionOldEditor(editor, newlyAdded)
    }, refactoringName, refactoringName)
    if (newPsiFile != null) {
      FileEditorManager.getInstance(project).openFile(newPsiFile!!.viewProvider.virtualFile, true)
    }
  }

  private fun positionOldEditor(editor: Editor?, newlyAdded: PsiElement?) {
    if (editor != null) {
      editor.caretModel.moveToOffset(newlyAdded!!.textRange.startOffset)
      editor.selectionModel.setSelection(0, 0)
      editor.scrollingModel.scrollToCaret(ScrollType.RELATIVE)
    }
  }

  private fun showDialog(context: XmlTag): String? {
    val folder = context.containingFile.parent!!
    val nameField = JBTextField(20)
    nameField.emptyText.text = "Component name (in kebab notation)"
    val errorLabel = JLabel("")
    errorLabel.foreground = JBColor.red
    val panel = FormBuilder()
      .addLabeledComponent("Component name:", nameField)
      .addComponent(errorLabel)
      .panel

    val builder = DialogBuilder()
    builder.setTitle(VueBundle.message("vue.template.intention.extract.component"))
    builder.setCenterPanel(panel)
    builder.setPreferredFocusComponent(nameField)
    builder.setDimensionServiceKey(VueExtractComponentRefactoring::class.java.name)

    val validator = TagNameValidator(context)

    val changesHandler = {
      val normalized = toAsset(nameField.text.trim()).capitalize()
      val fileName = normalized + ".vue"
      errorLabel.text = ""
      if (normalized.isEmpty() || !PathUtilRt.isValidFileName(fileName, false) || !validator.validate(normalized)) {
        builder.okActionEnabled(false)
      } else if (folder.findFile(fileName) != null) {
        builder.okActionEnabled(false)
        errorLabel.text = "File $fileName already exists"
      } else {
        builder.okActionEnabled(true)
      }
    }
    nameField.addActionListener({ changesHandler.invoke() })
    nameField.addKeyListener(object: KeyAdapter() {
      override fun keyReleased(e: KeyEvent?) {
        changesHandler.invoke()
      }
    })

    return if (builder.showAndGet()) nameField.text.trim()
    else null
  }

  private class TagNameValidator(context: XmlTag) {
    private val forbidden = mutableSetOf<String>()
    init {
      forbidden.addAll(DefaultXmlExtension.DEFAULT_EXTENSION.getAvailableTagNames(context.containingFile as XmlFile, context)
        .map { it.name })
      val elements = mutableListOf<LookupElement>()
      VueTagProvider().addTagNameVariants(elements, context, null)
      forbidden.addAll(elements.map { toAsset(it.lookupString).capitalize() })
    }

    fun validate(normalized: String): Boolean {
      return !normalized.contains(' ') && !forbidden.contains(normalized.toLowerCase())
    }
  }
}

class MyData(private val newComponentName: String, private val list: List<XmlTag>) {
  private val folder: PsiDirectory? = list[0].containingFile.parent
  private val scriptTag = if (list[0].containingFile is XmlFile) findScriptTag(list[0].containingFile as XmlFile) else null
  private val detectedLanguage = detectLanguage()
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
      val parentTag = PsiTreeUtil.getParentOfType(resolved, XmlTag::class.java)
      if (scriptTag != null && parentTag == scriptTag || PsiTreeUtil.isAncestor(parentTag, refData.tag, true)) {
        map.putIfAbsent(refData.tag, mutableListOf())
        map[refData.tag]!!.add(refData)
      }
    }
    return map
  }

  private fun processVueComponent(ref: VueTagNameReference) {
    if (scriptTag == null) return
    val content = PsiTreeUtil.findChildOfType(scriptTag, JSEmbeddedContent::class.java) ?: return
    val variants = ref.multiResolve(false)
    val foundImport = variants.mapNotNull {
      if (!it.isValidResult || it.element == null) return@mapNotNull null
      val file = it.element!!.containingFile
      // this means it was not resolved into the other file which we can import
      if (ref.element.containingFile == file) return@mapNotNull null

      val name = FileUtil.getNameWithoutExtension(file.name)
      findExistingES6Import(content, null, name, true) ?: findExistingES6Import(content, null, file.name, true)
    }.firstOrNull() ?: return
    importsToCopy.put(toAsset(ref.nameElement.text).capitalize(), foundImport)
  }

  private fun gatherReferences(): List<RefData> {
    folder ?: return emptyList()
    val refs = mutableListOf<RefData>()
    val injManager = InjectedLanguageManager.getInstance(folder.project)
    list.forEach { tag ->
      PsiTreeUtil.processElements(tag, {
        refs.addAll(addElementReferences(it, tag, 0))
        true
      })
      val hosts = PsiTreeUtil.findChildrenOfType(tag, PsiLanguageInjectionHost::class.java)
      hosts.forEach { host ->
        injManager.getInjectedPsiFiles(host)?.forEach { pair: Pair<PsiElement, TextRange> ->
          PsiTreeUtil.processElements(pair.first) { element ->
            refs.addAll(addElementReferences(element, tag, host.textRange.startOffset + pair.second.startOffset))
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

  private fun generateNewTemplateContents(): String {
    return list.joinToString("")
    { tag ->
      val sb = StringBuilder(tag.text)
      val tagStart = tag.textRange.startOffset
      val replaces = refDataMap[tag]?.mapNotNull {
        val absRange = it.getReplaceRange() ?: return@mapNotNull null
        Trinity(absRange.startOffset - tagStart, absRange.endOffset - tagStart, it.getRefName())
      }?.sortedByDescending { it.first }
      replaces?.forEach { sb.replace(it.first, it.second, it.third) }
      sb.toString()
    }
  }

  private fun detectLanguage(): String {
    val lang = scriptTag?.getAttribute("lang")?.value ?: return ""
    return "lang=\"$lang\""
  }

  fun generateNewComponent(): PsiFile? {
    folder ?: return null
    val newFile = folder.virtualFile.createChildData(this, toAsset(newComponentName).capitalize() + ".vue")
    val newText = """<template>
${generateNewTemplateContents()}
</template>
<script $detectedLanguage>${generateImports()}
export default {
  name: '$newComponentName'${generateDescriptorMembers()}
}
</script>"""
    VfsUtil.saveText(newFile, newText)
    return PsiManager.getInstance(folder.project).findFile(newFile)
  }

  private fun generateImports(): String {
    if (importsToCopy.isEmpty()) return ""
    return importsToCopy.keys.sorted().joinToString("\n", "\n") { "import ${it} from ${importsToCopy[it]!!.fromClause?.referenceText ?: "''"}" }
  }

  private fun generateDescriptorMembers(): String {
    val members = mutableListOf<String>()
    if (!importsToCopy.isEmpty()) {
      members.add(importsToCopy.keys.sorted().joinToString ( ", ", ",\ncomponents: {", "}" ))
    }
    if (!refDataMap.isEmpty()) {
      members.add(sortedProps().joinToString(",\n", ",\nprops: {\n", "\n}") { "${it.getRefName()}: {}" })
    }
    return members.joinToString("")
  }

  fun modifyCurrentComponent(newPsiFile: PsiFile, editor: Editor?): PsiElement? {
    val leader = list[0]
    val newTagName = fromAsset(newComponentName)
    val replaceText = "<template><$newTagName ${generateProps()}/></template>"
    val project = leader.project
    val dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.vue", VueFileType.INSTANCE, replaceText)
    val template = PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java)!!
    val newTag = template.findFirstSubTag(newTagName)!!

    val newlyAdded = leader.replace(newTag)
    list.subList(1, list.size).forEach { it.delete() }

    VueInsertHandler.InsertHandlerWorker().insertComponentImport(newlyAdded, newComponentName, newPsiFile, editor)
    optimizeUnusedComponentsAndImports(newlyAdded.containingFile)
    return newlyAdded
  }

  private fun optimizeUnusedComponentsAndImports(file: PsiFile) {
    val content = findModule(file) ?: return
    val defaultExport = ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment
    val component = defaultExport?.stubSafeElement as? JSObjectLiteralExpression

    val components = (component?.findProperty("components")?.value as? JSObjectLiteralExpression)?.properties
    if (components != null && !components.isEmpty()) {
      val names = components.map { toAsset(it.name ?: "").capitalize() }.toMutableSet()
      (file as XmlFile).accept(object : VueFileVisitor() {
        override fun visitElement(element: PsiElement?) {
          if (element is XmlTag) {
            names.remove(toAsset(element.name).capitalize())
            recursion(element)
          }
        }
      })
      components.filter { it.name != null && names.contains(toAsset(it.name!!).capitalize()) }.forEach { it.delete() }
      ES6CreateImportUtil.optimizeImports(file)
    }
  }

  private fun generateProps(): String {
    return sortedProps().joinToString(" "){ ":${fromAsset(it.getRefName())}=\"${it.getExpressionText()}\"" }
  }

  private fun sortedProps() = refDataMap.values.flatten().sortedBy { it.getRefName() }

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