package tanvd.grazi.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Segment
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*

//For tests, should be fixed once move to IntelliJ Test infra
class SmartPointerStub<T: PsiElement>(private val myElement: T) : SmartPsiElementPointer<T> {
    override fun getRange(): Segment? {
        throw NotImplementedError()
    }

    override fun getContainingFile(): PsiFile? {
        throw NotImplementedError()
    }

    override fun getPsiRange(): Segment? {
        throw NotImplementedError()
    }

    override fun getVirtualFile(): VirtualFile {
        throw NotImplementedError()
    }

    override fun getProject(): Project {
        throw NotImplementedError()
    }

    override fun getElement(): T? {
        return myElement
    }
}
