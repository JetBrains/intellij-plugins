package name.kropp.intellij.makefile

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.tree.TokenSet
import name.kropp.intellij.makefile.psi.*


class MakefileAnnotator : Annotator {
  private val lineTokenSet = TokenSet.create(MakefileTypes.LINE)

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is MakefileTarget) {
      holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.TARGET
    } else if (element is MakefilePrerequisite) {
      holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.PREREQUISITE

      val targets = (element.parent.parent.parent as MakefileTargetLine).targets
      if (targets.targetList.firstOrNull()?.isSpecialTarget == false) {
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
          holder.createErrorAnnotation(element, "Unresolved prerequisite").registerFix(CreateRuleFix(element))
        } else if (unresolvedFile != null) {
          holder.createErrorAnnotation(unresolvedFile!!, "File not found")
        }
      }
    } else if (element is MakefileVariable) {
      holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.VARIABLE
    } else if (element is MakefileVariableValue) {
      element.node.getChildren(lineTokenSet).forEach {
        holder.createInfoAnnotation(it, null).textAttributes = MakefileSyntaxHighlighter.VARIABLE_VALUE
      }
    }
  }
}