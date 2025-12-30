package org.angular2.lang.html.psi.arrangement

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.arrangement.HtmlRearranger
import com.intellij.xml.arrangement.XmlArrangementParseInfo
import com.intellij.xml.arrangement.XmlArrangementVisitor
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlBlockContents

class Angular2HtmlRearranger : HtmlRearranger() {

  override fun createVisitor(info: XmlArrangementParseInfo, document: Document?, ranges: Collection<TextRange>): XmlArrangementVisitor =
    AngularArrangementVisitor(info, document, ranges)

  private class AngularArrangementVisitor(info: XmlArrangementParseInfo, document: Document?, ranges: Collection<TextRange>)
    : HtmlArrangementVisitor(info, document, ranges) {

    override fun visitXmlFile(file: XmlFile) {
      file.document?.children?.forEach {
        if (it is XmlTag || it is Angular2HtmlBlock) {
          it.accept(this)
        }
      }
    }

    override fun visitElement(element: PsiElement) {
      if (element is Angular2HtmlBlock || element is Angular2HtmlBlockContents)
        element.acceptChildren(this)
      else
        super.visitElement(element)
    }

  }

}