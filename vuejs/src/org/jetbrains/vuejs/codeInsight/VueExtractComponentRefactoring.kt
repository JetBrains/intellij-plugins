// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.util.PathUtilRt
import com.intellij.xml.DefaultXmlExtension
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.VueComponentInplaceIntroducer.Companion.GROUP_ID

/**
 * @author Irina.Chernushina on 12/14/2017.
 */
class VueExtractComponentRefactoring(private val project: Project,
                                     private val list: List<XmlTag>,
                                     private val editor: Editor) {
  fun perform(defaultName: String? = null) {
    if (list.isEmpty() ||
        list[0].containingFile == null ||
        list[0].containingFile.parent == null ||
        !CommonRefactoringUtil.checkReadOnlyStatus(project, list[0].containingFile)) return

    val oldText = getSelectedText()

    val data = VueExtractComponentDataBuilder(list)

    val refactoringName = VueBundle.message("vue.template.intention.extract.component")
    val commandProcessor = CommandProcessor.getInstance()
    commandProcessor.executeCommand(project, {
      var newlyAdded: XmlTag? = null
      val validator = TagNameValidator(list[0])
      var startMarkAction: StartMarkAction? = null
      WriteAction.run<RuntimeException> {
        startMarkAction = StartMarkAction.start(editor, project, refactoringName)
        startMarkAction!!.isGlobal = true
        newlyAdded = data.replaceWithNewTag(defaultName ?: "NewComponent") as? XmlTag
      }
      VueComponentInplaceIntroducer(newlyAdded!!, editor, data, oldText, validator::validate, startMarkAction!!).performInplaceRefactoring(linkedSetOf())

    }, refactoringName, GROUP_ID)
  }

  private fun getSelectedText(): String =
    editor.document.getText(TextRange(list[0].textRange.startOffset, list[list.size - 1].textRange.endOffset))

  private class TagNameValidator(context: XmlTag) {
    private val folder = context.containingFile.parent!!
    private val forbidden = mutableSetOf<String>()
    private val alreadyExisting = mutableSetOf<String>()
    init {
      forbidden.addAll(DefaultXmlExtension.DEFAULT_EXTENSION.getAvailableTagNames(context.containingFile as XmlFile, context)
        .map { it.name })
      val elements = mutableListOf<LookupElement>()
      VueTagProvider().addTagNameVariants(elements, context, null)
      alreadyExisting.addAll(elements.map { toAsset(it.lookupString).capitalize() })
    }

    fun validate(text: String): String? {
      val normalized = fromAsset(text.trim())
      val fileName = toAsset(text.trim()).capitalize() + ".vue"
      if (normalized.isEmpty() || !PathUtilRt.isValidFileName(fileName, false) ||
          normalized.contains(' ') || forbidden.contains(normalized.toLowerCase())) return "Invalid component name: $normalized"
      if (alreadyExisting.contains(normalized.toLowerCase())) return "Component $normalized already exists"
      if (folder.findFile(fileName) != null) {
        return "File $fileName already exists"
      }
      return null
    }
  }
}