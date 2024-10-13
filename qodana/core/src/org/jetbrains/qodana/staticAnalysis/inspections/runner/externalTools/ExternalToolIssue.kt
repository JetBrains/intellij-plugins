package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ProblemDescriptorUtil.sanitizeIllegalXmlChars
import org.jdom.Element

data class ExternalToolIssue(
  val inspectionId: String,
  val severity: HighlightDisplayLevel,
  val description: String,
  val file: String? = null,
  val line: Int? = null,
  val offset: Int? = null,
  val length: Int? = null,
  val module: String? = null,
  val highlight: String? = null,
  val language: String? = null,
  val framework: String? = null
) {
  fun toElement() : Element {
    val element = Element("problem")
    val problemClass = Element("problem_class")
    problemClass.setAttribute("id", inspectionId)
    problemClass.setAttribute("severity", severity.name.uppercase())
    element.addContent(problemClass)
    val parts = listOf(
      Pair("file", file),
      Pair("line", line?.toString()),
      Pair("module", if (module != null) sanitizeIllegalXmlChars(module) else null),
      Pair("description", sanitizeIllegalXmlChars(description)),
      Pair("highlighted_element", if (highlight != null) sanitizeIllegalXmlChars(highlight) else null),
      Pair("language", language),
      Pair("framework", framework),
      Pair("offset", offset?.toString()),
      Pair("length", length?.toString()),
    )

    for (part in parts.filter { it.second != null }) {
      val ele = Element(part.first)
      ele.addContent(part.second)
      element.addContent(ele)
    }
    return element
  }
}