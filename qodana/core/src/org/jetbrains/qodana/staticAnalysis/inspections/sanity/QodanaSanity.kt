package org.jetbrains.qodana.staticAnalysis.inspections.sanity

import com.intellij.codeInsight.daemon.impl.HighlightVisitorBasedInspection
import com.intellij.codeInsight.daemon.impl.PROBLEM_DESCRIPTOR_TAG
import com.intellij.codeInsight.daemon.impl.withUserData
import com.intellij.codeInspection.*
import com.intellij.lang.LanguageUtil
import com.intellij.lang.annotation.HighlightSeverity.ERROR
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

val IGNORED_LANGUAGES = setOf("Groovy", "XML", "Manifest", "SPI", "FTL", "FTL>", "FTL]", "CSS", "SCSS", "CSHARP")

class QodanaSanity : LocalInspectionTool() {
  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
    if (isOnTheFly) return null

    val language = file.language
    val baseLanguages = LanguageUtil.getBaseLanguages(language)
    if (language.id in IGNORED_LANGUAGES || baseLanguages.any { it.id in IGNORED_LANGUAGES }) return null

    val highlightInfos = HighlightVisitorBasedInspection.runAnnotatorsInGeneralHighlighting(
      file,
      true,
      true,
      true

    ).filter {
      it.severity == ERROR
    }


    val results = ArrayList<ProblemDescriptor>(highlightInfos.size)
    for (info in highlightInfos) {
      val range = TextRange(info.startOffset, info.endOffset)
      var element: PsiElement? = file.findElementAt(info.startOffset)
      while (element != null && !element.textRange.contains(range)) {
        element = element.parent
      }
      if (element == null) {
        element = file
      }

      val descriptor = GlobalInspectionUtil.createProblemDescriptor(
        element,
        info,
        range.shiftRight(-element.node.startOffset),
        info.problemGroup,
        manager
      )

      results.add(addMessageTag(descriptor))
    }

    return results.toTypedArray()
  }

  private fun addMessageTag(descriptor: ProblemDescriptor): ProblemDescriptor {
    if (!descriptor.descriptionTemplate.startsWith("[")) return descriptor
    val tag = descriptor.descriptionTemplate.substringBefore(']', "").substringAfter('[', "")
    if (tag.isEmpty()) return descriptor
    return descriptor.withUserData {
      putUserData(PROBLEM_DESCRIPTOR_TAG, listOf(tag))
    }
  }
}