package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueLanguage

class DuplicateTagInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag?) {
        if (tag?.language != VueLanguage.INSTANCE) return
        if ("template" != tag.name && !HtmlUtil.isScriptTag(tag)) return
        val parent = tag.parent as? XmlDocument ?: return
        PsiTreeUtil.getChildrenOfType(parent, XmlTag::class.java)!!
          .filter { it != tag && it.name == tag.name }
          .forEach { holder.registerProblem(tag, "Duplicate ${tag.name} tag", DeleteTagFix(tag)) }
      }
    }
  }
}

class DeleteTagFix(tag: XmlTag, val tagName:String = tag.name) : LocalQuickFixOnPsiElement(tag) {
  override fun getFamilyName(): String = "Remove Tag"
  override fun getText(): String = "Remove ${tagName} Tag"

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    startElement.delete()
  }
}