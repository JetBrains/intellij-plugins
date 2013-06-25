package org.angularjs

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.codeInsight.intention.impl.QuickEditAction
import com.intellij.psi.util.PsiUtilBase
import com.intellij.lang.xml.XMLLanguage

public class OpenInDocsIntention: QuickEditAction() {
    public override fun getText(): String {
        return "Open Angular Docs"
    }

    public override fun getFamilyName(): String {
        return "OpenInDocsIntention"
    }

    public override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
               val element = PsiUtilBase.getElementAtCaret(editor!!)

        return element is com.intellij.psi.impl.source.xml.XmlTokenImpl &&
        element.getLanguage() == XMLLanguage.INSTANCE &&
        element.getText()?.startsWith("ng")!!
    }

    public override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val psiElement = PsiUtilBase.getElementAtCaret(editor!!)


        var string = psiElement?.getText()!!
        val strings = string.split("-")

        var result = "ng"
        for(i in strings.indices){
            val s = strings.get(i)
            if(s == "ng") continue
            result += s.capitalize()
        }


        val url = "http://docs.angularjs.org/api/ng.directive:" + result
        BrowserUtil.launchBrowser(url)
    }
}