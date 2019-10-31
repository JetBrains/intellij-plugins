/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check.go

import com.goide.psi.GoFile
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import training.check.Check

class GoCheckStringFormatArguments : Check {

    private var project: Project? = null
    private var editor: Editor? = null

    override fun set(project: Project, editor: Editor) {
        this.project = project
        this.editor = editor
    }

    override fun before() {}

    override fun check(): Boolean {
        val document = editor?.document ?: return false
        val manager = project?.let { PsiDocumentManager.getInstance(it) } ?: return false
        val file = manager.getPsiFile(document) as? GoFile ?: return false
        val block = file.functions.find { it.name == "main" }?.block ?: return false
        return block.statementList.any { it.text == """fmt.Printf("hello %s #%d", subj.name, subj.id)""" }
    }

    override fun listenAllKeys(): Boolean = false

}