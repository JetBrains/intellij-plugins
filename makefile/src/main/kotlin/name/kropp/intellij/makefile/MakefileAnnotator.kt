package name.kropp.intellij.makefile

import com.intellij.codeInspection.*
import com.intellij.lang.*
import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.FUNCTION
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.PREREQUISITE
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.SPECIAL_TARGET
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.STRING
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.TARGET
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.VARIABLE
import name.kropp.intellij.makefile.MakefileSyntaxHighlighter.Companion.VARIABLE_VALUE
import name.kropp.intellij.makefile.psi.*


class MakefileAnnotator : Annotator {
  private val lineTokenSet = TokenSet.create(MakefileTypes.IDENTIFIER)

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is MakefileRule && element.isUnused()) {
      holder.newAnnotation(WEAK_WARNING, "Redundant rule").range(element).highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL).withFix(RemoveRuleFix(element)).create()
    } else if (element is MakefileTarget && !(element.parent.parent.parent as MakefileRule).isUnused()) {
      holder.mark(element, if (element.isSpecialTarget) SPECIAL_TARGET else TARGET)
    } else if (element is MakefilePrerequisite) {
      holder.mark(element, PREREQUISITE)

      if (Regex("""\$\((.*)\)""").matches(element.text)) {
        return
      }

      val targetLine = element.parent.parent.parent as MakefileTargetLine
      if (targetLine.targets.targetList.firstOrNull()?.isSpecialTarget == false && targetLine.targetPattern == null) {
        val targetReferences = element.references.filter { it is MakefileTargetReference && it.multiResolve(false).isNotEmpty() }.any()

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
          holder.newAnnotation(WEAK_WARNING, "Unresolved prerequisite").range(element).withFix(CreateRuleFix(element)).create()
        } else if (unresolvedFile != null) {
          holder.newAnnotation(WEAK_WARNING, "File not found").range(unresolvedFile!!).create()
        }
      }
    } else if (element is MakefileVariable) {
      holder.mark(element, VARIABLE)
    } else if (element is MakefileVariableValue) {
      element.node.getChildren(lineTokenSet).forEach {
        holder.mark(it, VARIABLE_VALUE)
      }
    } else if (element is MakefileFunctionName && element.parent is MakefileFunction) {
      holder.mark(element, FUNCTION)
    } else if (element is MakefileString) {
      holder.mark(element, STRING)
    } else if (element is MakefileVariableUsage) {
      holder.mark(element, VARIABLE)
    }
  }

  private fun AnnotationHolder.mark(element: PsiElement, attr: TextAttributesKey) {
    newSilentAnnotation(INFORMATION).range(element).textAttributes(attr).create()
  }

  private fun AnnotationHolder.mark(node: ASTNode, attr: TextAttributesKey) {
    newSilentAnnotation(INFORMATION).range(node).textAttributes(attr).create()
  }

  private fun MakefileRule.isUnused(): Boolean {
    if (recipe?.isEmpty == false) return false
    if (targetLine.targets.targetList.any { it.isSpecialTarget || it.isPatternTarget }) return false
    if (targetLine.prerequisites?.normalPrerequisites?.prerequisiteList?.any() == true) return false
    if (targetLine.prerequisites?.orderOnlyPrerequisites?.prerequisiteList?.any() == true) return false
    if (targetLine.variableAssignment != null) return false
    if (targetLine.privatevar != null) return false

    return true
  }
}