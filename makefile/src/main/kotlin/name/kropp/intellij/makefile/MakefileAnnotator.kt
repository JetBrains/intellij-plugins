package name.kropp.intellij.makefile

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileDependency
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is MakefileTarget) {
      holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.TARGET
    } else if (element is MakefileDependency) {
      holder.createInfoAnnotation(element, null).textAttributes = MakefileSyntaxHighlighter.DEPENDENCY
    }
  }
}