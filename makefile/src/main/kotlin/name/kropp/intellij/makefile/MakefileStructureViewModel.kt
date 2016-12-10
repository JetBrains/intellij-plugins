package name.kropp.intellij.makefile

import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.psi.PsiFile

class MakefileStructureViewModel(psiFile: PsiFile) : StructureViewModelBase(psiFile, MakefileStructureViewElement(psiFile)) {

}

