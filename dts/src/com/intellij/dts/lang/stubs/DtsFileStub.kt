package com.intellij.dts.lang.stubs

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsLanguage
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IStubFileElementType

class DtsFileStub(file: DtsFile?) : PsiFileStubImpl<DtsFile>(file) {
    override fun getType() = Type

    object Type : IStubFileElementType<DtsFileStub>(DtsLanguage) {
        override fun getStubVersion(): Int = 0

        override fun getExternalId(): String = "${language.id}.file"

        override fun getBuilder(): StubBuilder = object : DefaultStubBuilder() {
            override fun createStubForFile(file: PsiFile): StubElement<*> = DtsFileStub(file as? DtsFile)
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): DtsFileStub = DtsFileStub(null)
    }
}