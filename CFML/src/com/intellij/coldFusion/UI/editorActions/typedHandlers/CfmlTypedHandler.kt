/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.editorActions.typedHandlers

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.XmlSlashTypedHandler.autoIndent
import com.intellij.coldFusion.UI.editorActions.matchers.CfmlBraceMatcher
import com.intellij.coldFusion.UI.editorActions.utils.CfmlEditorUtil
import com.intellij.coldFusion.model.CfmlLanguage
import com.intellij.coldFusion.model.CfmlUtil
import com.intellij.coldFusion.model.files.CfmlFile
import com.intellij.coldFusion.model.files.CfmlFileViewProvider
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.coldFusion.model.parsers.CfmlElementTypes.TEMPLATE_TEXT
import com.intellij.coldFusion.model.psi.CfmlTag
import com.intellij.coldFusion.model.psi.CfmlTagUtil
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.ASTNode
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.SystemProperties

class CfmlTypedHandler : TypedHandlerDelegate() {
  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    val cfmlFile = file.viewProvider.getPsi(CfmlLanguage.INSTANCE) ?: return Result.CONTINUE

    if (isNotCfmlFile(cfmlFile, editor)) return Result.CONTINUE
    if (charTyped == '/') CfmlUtil.showCompletion(editor)

    return Result.CONTINUE
  }

  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    val cfmlFile = file.viewProvider.getPsi(CfmlLanguage.INSTANCE) ?: return Result.CONTINUE

    if (isNotCfmlFile(cfmlFile, editor)) return Result.CONTINUE
    val offset = editor.caretModel.offset

    when (c) {
      '{' -> {
        val braceMatcher = CfmlBraceMatcher()
        val iterator = editor.highlighter.createIterator(offset)
        if (!braceMatcher.isLBraceToken(iterator, editor.document.charsSequence, fileType)) {
          EditorModificationUtil.insertStringAtCaret(editor, "}", true, 0)
          // return Result.STOP;
        }
        return Result.CONTINUE
      }
      '#' -> {
        if (ourEnableDoublePoundInsertion && CfmlEditorUtil.countSharpsBalance(editor) == 0) {
          val charAtOffset = DocumentUtils.getCharAt(editor.document, offset)
          if (charAtOffset == '#') {
            EditorModificationUtil.moveCaretRelatively(editor, 1)
            return Result.STOP
          }
          EditorModificationUtil.insertStringAtCaret(editor, "#", true, 0)
        }
      }
      '>' -> {
        if (editor.highlighter.createIterator(
            editor.getCaretModel().offset).tokenType === CfmlTokenTypes.COMMENT || editor.highlighter.createIterator(
            editor.getCaretModel().offset).tokenType.language !== CfmlLanguage.INSTANCE) {
          return Result.CONTINUE
        }
        insertCloseTagIfNeeded(editor, cfmlFile, project)
        return Result.STOP
      }
    }
    return Result.CONTINUE
  }

  override fun charTyped(c: Char, project: Project, editor: Editor, psiFile: PsiFile): Result {
    if (isNotCfmlFileViewProvider(psiFile)) return Result.CONTINUE
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return Result.CONTINUE
    val provider = file.viewProvider as CfmlFileViewProvider
    return when (c) {
      '/' -> processCloseTag(provider, editor)
      else -> Result.CONTINUE
    }
  }

  private fun processCloseTag(provider: CfmlFileViewProvider, editor: Editor): Result {
    val offset = editor.caretModel.offset

    val xmlElement = provider.findElementAt(offset - 1, XMLLanguage::class.java) ?: return Result.CONTINUE
    if (xmlElement.language !is XMLLanguage) return Result.CONTINUE
    val prevXmlLeaf: ASTNode = xmlElement.node ?: return Result.CONTINUE

    //is prevXmlLeaf is a XML_END_TAG_START in a mixed CFML and XML case file or (||) current symbol is the closing tag for a pure CFML
    if ("</" == prevXmlLeaf.text && prevXmlLeaf.elementType === XmlTokenType.XML_END_TAG_START || CfmlTagUtil.isClosingTag(provider,
                                                                                                                           offset)) {
      val cfmlElement = provider.findElementAt(offset - 1, CfmlLanguage::class.java) ?: return Result.CONTINUE
      val cfmlTag = findOpeningCfmlTag(cfmlElement, offset) ?: return Result.CONTINUE
      if (!cfmlTag.tagName.isNullOrEmpty()) {
        EditorModificationUtil.insertStringAtCaret(editor, "${cfmlTag.tagName}>", false)
        autoIndent(editor)
        return Result.STOP
      }
    }
    return Result.CONTINUE
  }

  private fun findOpeningCfmlTag(cfmlElement: PsiElement, offset: Int): CfmlTag? {

    var cfmlPsiElement: PsiElement? = cfmlElement
    while (cfmlPsiElement != null) {
      //we've found an opened xml tag here; let's delegate completion to the XmlTypedHandler (IDEA-205201)
      if (cfmlPsiElement.elementType == TEMPLATE_TEXT && hasUnclosedXmlTag(cfmlElement, offset)) return null
      cfmlPsiElement = cfmlPsiElement.getPreviousCfmlTag()
      if (cfmlPsiElement is CfmlTag && CfmlTagUtil.isUnclosedTag(cfmlPsiElement)) return cfmlPsiElement
    }
    return CfmlTagUtil.getUnclosedParentTag(cfmlElement)
  }

  private fun PsiElement.getPreviousCfmlTag(): PsiElement? = PsiTreeUtil.getPrevSiblingOfType(this, CfmlTag::class.java)

  /**
   * returns true if cfmlElement contains unclosed XmlTag in range from cfmlElement startOffset till caretOffset
   */
  private fun hasUnclosedXmlTag(cfmlElement: PsiElement, caretOffset: Int): Boolean {
    val startOffset = cfmlElement.textOffset
    val htmlPsi = cfmlElement.containingFile.viewProvider.allFiles.firstOrNull { it.fileType is HtmlFileType } ?: return false
    return SyntaxTraverser.psiTraverser()
      .withRoot(htmlPsi)
      .traverse()
      .filter { it is XmlTag && (it.textOffset in startOffset..caretOffset) }
      .any { it.lastChild.elementType != XmlTokenType.XML_TAG_END }
  }

  companion object {

    val ourEnableDoublePoundInsertion: Boolean = SystemProperties.getBooleanProperty("idea.cfml.insert.pair.pound", true)

    fun insertCloseTagIfNeeded(editor: Editor, file: PsiFile, project: Project): Boolean {
      val document = editor.document
      val documentManager = PsiDocumentManager.getInstance(project)

      var offset = editor.caretModel.offset
      documentManager.commitDocument(document)
      val charAtOffset = DocumentUtils.getCharAt(document, offset)

      if (charAtOffset != '>') {
        EditorModificationUtil.insertStringAtCaret(editor, ">", true, 0)
      }
      EditorModificationUtil.moveCaretRelatively(editor, 1)
      ++offset
      if (DocumentUtils.getCharAt(document, offset - 2) == '/') {
        return false
      }
      var iterator = editor.highlighter.createIterator(offset - 2)

      while (!iterator.atEnd() && iterator.tokenType != CfmlTokenTypes.CF_TAG_NAME) {
        if (CfmlUtil.isControlToken(iterator.tokenType)) {
          return false
        }
        iterator.retreat()
      }
      if (!iterator.atEnd()) {
        iterator.retreat()
        if (!iterator.atEnd() && iterator.tokenType == CfmlTokenTypes.LSLASH_ANGLEBRACKET) {
          return false
        }
        iterator.advance()
      }
      if (iterator.atEnd()) {
        return false
      }
      var tagName = document.charsSequence.subSequence(iterator.start, iterator.end).toString()
      if (CfmlUtil.isSingleCfmlTag(tagName, project) || CfmlUtil.isUserDefined(tagName)) {
        return false
      }
      var tagElement = file.findElementAt(iterator.start)
      while (tagElement != null && tagElement !is CfmlTag) {
        tagElement = tagElement.parent
      }
      if (tagElement == null) {
        return false
      }
      var doInsertion = false
      if (tagElement.lastChild is PsiErrorElement) {
        doInsertion = true
      }
      else {
        iterator = editor.highlighter.createIterator(0)
        while (!iterator.atEnd() && iterator.start < offset) {
          if (iterator.tokenType === CfmlTokenTypes.CF_TAG_NAME) {
            val currentTagName = document.charsSequence.subSequence(iterator.start, iterator.end).toString()
            if (tagName == currentTagName) {
              var currentTagElement = file.findElementAt(iterator.start)
              currentTagElement = PsiTreeUtil.getParentOfType(currentTagElement, CfmlTag::class.java)
              if (currentTagElement!!.lastChild is PsiErrorElement) {
                doInsertion = true
                break
              }
            }
          }
          iterator.advance()
        }
      }
      val tagNameFromPsi = (tagElement as CfmlTag).tagName // tag name in lowercase
      if (doInsertion && CfmlUtil.isEndTagRequired(tagNameFromPsi, project)) {
        if (!Comparing.equal(tagNameFromPsi, tagName, false)) {
          tagName = tagNameFromPsi // use tagName because it has proper case
        }
        EditorModificationUtil.insertStringAtCaret(editor, "</$tagName>", true, 0)
        return true
      }
      return false
    }

    internal fun isNotCfmlFile(file: PsiFile, editor: Editor): Boolean {
      return file !is CfmlFile || editor.caretModel.offset == 0
    }

    internal fun isNotCfmlFileViewProvider(file: PsiFile): Boolean {
      return file.viewProvider !is CfmlFileViewProvider
    }
  }
}




