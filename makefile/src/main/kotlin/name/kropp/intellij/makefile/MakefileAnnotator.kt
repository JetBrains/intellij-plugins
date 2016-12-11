package name.kropp.intellij.makefile

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileDependency
import name.kropp.intellij.makefile.psi.MakefileTarget
import name.kropp.intellij.makefile.psi.MakefileTargetLine


class MakefileAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is MakefileTarget) {
      holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.TARGET
    } else if (element is MakefileDependency) {
      val reference = element.reference!!
      if (reference.resolve() == null) {
        val target = (element.parent.parent as MakefileTargetLine).target
        if (target.name?.startsWith('.') == false) {
          holder.createErrorAnnotation(element, "Unresolved dependency").registerFix(CreateRuleFix(element))
        }
      } else {
        holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.DEPENDENCY
      }
    }
  }
}