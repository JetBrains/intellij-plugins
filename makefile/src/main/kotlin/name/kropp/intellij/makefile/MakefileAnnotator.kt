package name.kropp.intellij.makefile

import com.intellij.codeInspection.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.psi.*


class MakefileAnnotator : Annotator {
  private val lineTokenSet = TokenSet.create(MakefileTypes.IDENTIFIER)

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is MakefileRule && element.isUnused()) {
      holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Redundant rule").range(element).highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL).withFix(RemoveRuleFix(element)).create()
    } else if (element is MakefileTarget && !(element.parent.parent.parent as MakefileRule).isUnused()) {
      holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "").range(element).textAttributes(if (element.isSpecialTarget) MakefileSyntaxHighlighter.SPECIAL_TARGET else MakefileSyntaxHighlighter.TARGET).create()
    } else if (element is MakefilePrerequisite) {
      holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "").range(element).textAttributes(MakefileSyntaxHighlighter.PREREQUISITE).create()

      if (Regex("""\$\((.*)\)""").matches(element.text)) {
        return
      }

      val targetLine = element.parent.parent.parent as MakefileTargetLine
      if (targetLine.targets.targetList.firstOrNull()?.isSpecialTarget == false && targetLine.targetPattern == null) {
        val targetReferences = element.references.filter { it is MakefileTargetReference && it.resolve() != null }.any()

        var fileReferenceResolved = false
        var unresolvedFile: TextRange? = null
        element.references.filter { it is FileReference }.forEach {
          if (it.resolve() == null) {
            if (!targetReferences) {
              val startOffset = element.textRange.startOffset
              val start = startOffset + it.rangeInElement.startOffset
              val end = startOffset + it.rangeInElement.endOffset
              val textRange = TextRange.create(start, end)
              unresolvedFile = unresolvedFile?.union(textRange) ?: textRange
            }
          } else {
            fileReferenceResolved = true
          }
        }

        if (!targetReferences && !fileReferenceResolved) {
          holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Unresolved prerequisite").range(element).withFix(CreateRuleFix(element)).create()
        } else if (unresolvedFile != null) {
          holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "File not found").range(unresolvedFile!!).create()
        }
      }
    } else if (element is MakefileVariable) {
      holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "").range(element).textAttributes(MakefileSyntaxHighlighter.VARIABLE).create()
    } else if (element is MakefileVariableValue) {
      element.node.getChildren(lineTokenSet).forEach {
        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "").range(it).textAttributes(MakefileSyntaxHighlighter.VARIABLE_VALUE).create()
      }
    } else if (element is MakefileFunctionName && element.parent is MakefileFunction) {
      holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "").range(element).textAttributes(MakefileSyntaxHighlighter.FUNCTION).create()
    }
  }

  private fun MakefileRule.isUnused(): Boolean {
    if (recipe?.isEmpty == false) return false
    if (targetLine.targets.targetList.any { it.isSpecialTarget || it.isPatternTarget }) return false
    if (targetLine.prerequisites?.normalPrerequisites?.prerequisiteList?.any() == true) return false
    if (targetLine.variableAssignment != null) return false
    if (targetLine.privatevar != null) return false
    return true
  }
}