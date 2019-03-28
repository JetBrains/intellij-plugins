//package tanvd.grazi.ide.language
//
//
//import com.intellij.psi.PsiFile
//import com.intellij.psi.impl.source.html.HtmlFileImpl
//import com.intellij.psi.impl.source.xml.XmlTextImpl
//import com.intellij.psi.util.PsiTreeUtil
//import tanvd.grazi.model.TextBlock
//
//class HtmlSupport : LanguageSupport() {
//    override fun extract(file: PsiFile): List<TextBlock>? {
//        val htmlFile = file as? HtmlFileImpl ?: return null
//        return collectParagraphs(htmlFile).map {
//            TextBlock(it, it.text)
//        }
//    }
//
//    private fun collectParagraphs(htmlFile: HtmlFileImpl): MutableCollection<XmlTextImpl> {
//        return PsiTreeUtil.collectElementsOfType(htmlFile, XmlTextImpl::class.java)
//    }
//}
