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
package com.intellij.coldFusion.model

import com.intellij.codeInsight.AutoPopupController
import com.intellij.coldFusion.model.info.CfmlAttributeDescription
import com.intellij.coldFusion.model.info.CfmlLangInfo
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes
import com.intellij.coldFusion.model.parsers.CfmlKeywords
import com.intellij.coldFusion.model.psi.CfmlImport
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.PsiBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Couple
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.ContainerUtil
import java.util.*

/**
 * Created by Lera Nikolaenko
 */
object CfmlUtil {

  private val EMPTY_STRING_ARRAY = ArrayUtil.EMPTY_STRING_ARRAY

  //http://livedocs.adobe.com/coldfusion/8/htmldocs/help.html?content=functions_c-d_15.html
  val createObjectArgumentValues: Array<String>
    get() = arrayOf("component", "java", "com", "corba", "webservice")

  fun findFileByLibTag(originalFile: PsiFile, libtag: String): VirtualFile? {
    var libtag = libtag
    var base = getRealVirtualFile(originalFile)
    val module = if (base == null) null else ModuleUtilCore.findModuleForFile(base, originalFile.project)
    base = module?.moduleFile
    base = base?.parent

    libtag = StringUtil.trimStart(libtag, "/")

    if (ApplicationManager.getApplication().isUnitTestMode) {
      val virtualFile = getRealVirtualFile(originalFile)!!
      base = virtualFile.parent
    }

    return VfsUtil.findRelativeFile(base, *libtag.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
  }

  fun getImportByPrefix(context: PsiElement?, prefix: String?): CfmlImport? {
    if (prefix == null || context == null) {
      return null
    }
    val cfmlImports = PsiTreeUtil.getChildrenOfType(context.containingFile, CfmlImport::class.java) ?: return null
    return ContainerUtil.find(cfmlImports) { anImport -> prefix.equals(anImport.prefix!!, ignoreCase = true) }
  }

  fun isSearchedScope(scopeText: String): Boolean {
    return scopeText.equals("variables", ignoreCase = true) ||
           scopeText.equals("arguments", ignoreCase = true) ||
           scopeText.equals("url", ignoreCase = true) ||
           scopeText.equals("form", ignoreCase = true)
  }

  fun hasEqualScope(ref1: CfmlReferenceExpression, ref2: CfmlReferenceExpression): Boolean {
    if (ref1.scope == null && ref2.scope == null) {
      return true
    }
    else if (ref2.scope == null) {
      return CfmlUtil.isSearchedScope(ref1.scope!!.text)
    }
    else if (ref1.scope == null) {
      return CfmlUtil.isSearchedScope(ref2.scope!!.text)
    }
    else if (ref2.scope!!.text.equals(ref1.scope!!.text, ignoreCase = true)) {
      return true
    }
    return false
  }

  fun getTagList(project: Project): Set<String> {
    return CfmlLangInfo.getInstance(project).tagAttributes.keys
  }

  private fun anyProject(project: Project?): Project {
    if (project != null) return project
    val projects = ProjectManager.getInstance().openProjects
    return if (projects.isNotEmpty()) projects[0] else ProjectManager.getInstance().defaultProject
  }

  fun hasAnyAttributes(tagName: String, project: Project): Boolean {
    if (isUserDefined(tagName)) {
      return true
    }
    return if (!(CfmlLangInfo.getInstance(anyProject(project)).tagAttributes[tagName] == null || CfmlLangInfo.getInstance(
        anyProject(project)).tagAttributes[tagName]?.attributes == null)) {
      CfmlLangInfo.getInstance(anyProject(project)).tagAttributes[tagName]?.attributes?.size != 0
    }
    else false
  }

  fun getAttributes(tagName: String, project: Project): Collection<CfmlAttributeDescription> {
    return if (CfmlLangInfo.getInstance(anyProject(project)).tagAttributes[tagName] != null && CfmlLangInfo.getInstance(
        anyProject(project)).tagAttributes[tagName]?.attributes != null) {
      Collections
        .unmodifiableCollection(CfmlLangInfo.getInstance(anyProject(project)).tagAttributes[tagName]?.attributes)
    }
    else emptyList()
  }

  fun isStandardTag(tagName: String, project: Project): Boolean {
    return CfmlLangInfo.getInstance(anyProject(project)).tagAttributes.containsKey(tagName)
  }

  fun isUserDefined(tagName: String?): Boolean {
    return tagName != null && (tagName.toLowerCase().startsWith("cf_") || tagName.contains(":"))
  }

  fun isSingleCfmlTag(tagName: String, project: Project): Boolean {
    if (isUserDefined(tagName)) return false
    val tagAttributes = CfmlLangInfo.getInstance(anyProject(project)).tagAttributes ?: return false
    return if (!tagAttributes.containsKey(tagName)) {
      false
    }
    else !tagAttributes[tagName]!!.isEndTagRequired && tagAttributes[tagName]!!.isSingle
  }

  fun isEndTagRequired(tagName: String, project: Project?): Boolean {
    val cfmlLangInfo = CfmlLangInfo.getInstance(anyProject(project))
    return if (!cfmlLangInfo.tagAttributes.containsKey(tagName)) true
    else cfmlLangInfo.tagAttributes[tagName]!!.isEndTagRequired
  }

  fun getTagDescription(tagName: String, project: Project): String? {
    if (!CfmlLangInfo.getInstance(anyProject(project)).tagAttributes.containsKey(tagName)) return null
    if (!CfmlLangInfo.getInstance(anyProject(project)).tagAttributes.containsKey(tagName)) return null
    val tagAttributes = CfmlLangInfo.getInstance(anyProject(project)).tagAttributes[tagName] ?: throw Exception("Unable to get tag attributes for tag: $tagName")
    return "<div>Name: " +
           tagName +
           "</div>" +
           "<div>IsEndTagRequired: " +
           tagAttributes.isEndTagRequired +
           "</div>" +
           "<div>Descriprion: " +
           tagAttributes.description +
           "</div>" +
           "<div>For more information visit <a href = \"http://livedocs.adobe.com/coldfusion/8/htmldocs/Tags-pt0_01.html\">" +
           "\"http://livedocs.adobe.com/coldfusion/8/htmldocs/Tags-pt0_01.html\"</div>"
  }

  fun getAttributeDescription(tagName: String, attributeName: String, project: Project): String {
    val af = getAttribute(tagName, attributeName, project) ?: return ""
    return af.toString()
  }

  fun getAttribute(tagName: String, attributeName: String, project: Project): CfmlAttributeDescription? {
    val tagDescription = CfmlLangInfo.getInstance(anyProject(project)).tagAttributes[tagName] ?: return null
    val attributesCollection = tagDescription.attributes
    for (af in attributesCollection) {
      if (af.acceptName(attributeName)) {
        return af
      }
    }
    return null
  }

  fun isControlToken(type: IElementType): Boolean {
    return type === CfmlTokenTypes.OPENER ||
           type === CfmlTokenTypes.CLOSER ||
           type === CfmlTokenTypes.LSLASH_ANGLEBRACKET ||
           type === CfmlTokenTypes.R_ANGLEBRACKET ||
           type === CfscriptTokenTypes.L_CURLYBRACKET ||
           type === CfscriptTokenTypes.SEMICOLON
  }

  fun isActionName(builder: PsiBuilder): Boolean {
    val tokenText = builder.tokenText ?: return false

    val name = tokenText.toLowerCase()
    val keyword = CfmlKeywords.values().any { it.keyword == name }
    return keyword && checkAheadActionTokens(builder.lookAhead(1), builder.lookAhead(2))
  }

  private fun checkAheadActionTokens(second: IElementType?, third: IElementType?): Boolean {
    return (second === CfscriptTokenTypes.IDENTIFIER || second === CfscriptTokenTypes.L_CURLYBRACKET
            || second === CfmlTokenTypes.ASSIGN && third === CfscriptTokenTypes.L_CURLYBRACKET)
  }

  fun getPredefinedFunctions(project: Project): Array<String> {
    return CfmlLangInfo.getInstance(anyProject(project)).predefinedFunctions
  }

  fun isPredefinedFunction(functionName: String, project: Project): Boolean {
    return ArrayUtil.find(CfmlLangInfo.getInstance(anyProject(project)).predefinedFunctionsInLowCase, functionName.toLowerCase()) != -1
  }

  fun isPredefinedTagVariables(cfmlRef: CfmlReferenceExpression, project: Project): Boolean {
    val predefVarText = if (cfmlRef.lastChild != null) cfmlRef.lastChild!!.text else null
    //try to find tag type by name//
    var referenceName = cfmlRef.firstChild
    if (referenceName !is CfmlReferenceExpression || predefVarText == null) {
      return false
    }
    referenceName = referenceName.resolve()
    referenceName = referenceName?.parent
    if (referenceName !is CfmlTagImpl) {
      return false
    }
    val tagName = referenceName.tagName
    val tagNameWithoutCf = if (tagName.startsWith("cf")) tagName.substring(2) else tagName
    return CfmlLangInfo.getInstance(anyProject(project)).predefinedVariables.keys
      .contains(tagNameWithoutCf.toLowerCase() + "." + predefVarText
        .toLowerCase())
  }

  fun getAttributeValues(tagName: String, attributeName: String, project: Project): Array<String> {
    val attribute = getAttribute(tagName, attributeName, project)
    if (attribute != null) {
      val values = attribute.values
      return values ?: EMPTY_STRING_ARRAY
    }
    return EMPTY_STRING_ARRAY
  }

  fun getVariableScopes(project: Project): Array<String> {
    return CfmlLangInfo.getInstance(anyProject(project)).variableScopes
  }

  fun getFileName(element: PsiElement): String {
    val fileName = element.containingFile.name
    return if (fileName.indexOf('.') == -1) {
      fileName
    }
    else fileName.substring(0, fileName.indexOf('.'))
  }

  fun showCompletion(editor: Editor) {
    AutoPopupController.getInstance(editor.project).autoPopupMemberLookup(editor, null)
  }

  fun getRealVirtualFile(psiFile: PsiFile): VirtualFile? {
    return psiFile.originalFile.virtualFile
  }

  fun getPrefixAndName(name: String?): Couple<String> {
    if (name == null) {
      return Couple.getEmpty()
    }
    val index = name.indexOf(':')
    return if (index == -1) {
      Couple.of(null, name)
    }
    else Couple.of(name.substring(0, index), name.substring(index + 1))
  }
}
