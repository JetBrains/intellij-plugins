package com.intellij.dts.documentation

import com.intellij.dts.DtsTestBase
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking

abstract class DtsDocumentationTest : DtsTestBase() {
    protected fun doTest() {
        val content = getTestFixture("overlay")

        myFixture.configureByText("esp32.overlay", content)

        val documentations = IdeDocumentationTargetProvider
            .getInstance(project)
            .documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset - 1)
            .mapNotNull { computeDocumentationBlocking(it.createPointer())?.html }

        assertOneElement(documentations)
        compareWithFixture("html", documentations.first())
    }
}