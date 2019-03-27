package tanvd.grazi.ide.language


import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.javadoc.PsiDocTagImpl
import com.intellij.psi.impl.source.javadoc.PsiDocTokenImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.java.IJavaDocElementType
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.grammar.GrammarEngineService
import tanvd.grazi.ide.GraziInspection.Companion.typoToProblemDescriptors
import tanvd.grazi.model.TextBlock

class JavaDocSupport : LanguageSupport {
    companion object {
        class JavaDocTextElement(tokens: List<PsiDocTokenImpl>) : LeafPsiElement(IJavaDocElementType("JAVA_DOC_TEXT_ELEMENT"), "") {
            private val commentTokens = tokens.filter { it.parent::class != PsiDocTagImpl::class }
            private val tagTokens = tokens.filter { it.parent::class == PsiDocTagImpl::class }

            private val commentTokensText = commentTokens.map { x -> x.text }.joinToString(" ")

            fun getFixes(manager: InspectionManager, isOnTheFly: Boolean, ext: LanguageSupport): MutableList<ProblemDescriptor> {
                val grammarEngineService = GrammarEngineService.getInstance()
                val fixesForText = grammarEngineService.getFixes(commentTokensText)

                val mappings: MutableMap<Int, Pair<Int, Int>> = HashMap()

                var curAdd = 0
                var curTokenInd = 0

                for (i in 0 until commentTokensText.length) {
                    if (i < curAdd + commentTokens[curTokenInd].text.length) {
                        mappings[i] = i - curAdd to curTokenInd
                    } else if (i == curAdd + commentTokens[curTokenInd].text.length) {
                        if (i == commentTokensText.length - 1) {
                            mappings[i] = i - 1 - curAdd to curTokenInd
                        } else {
                            mappings[i] = i - curAdd to curTokenInd
                        }
                        curAdd += commentTokens[curTokenInd].text.length + 1
                        curTokenInd += 1
                    }
                }

                val problemDescriptorsForComments = fixesForText.map {
                    val token = commentTokens[mappings.get(it.range.start)!!.second]
                    // TODO fix bad crutch here. should be: mappings.get(it.range.endInclusive)!!.first
                    it.range = IntRange(mappings.get(it.range.start)!!.first, mappings.get(it.range.endInclusive - 1)!!.first + 1)
                    typoToProblemDescriptors(it, TextBlock(token, token.text), manager, isOnTheFly, ext)
                }

                val problemDescriptorsForTags = tagTokens.map {
                    grammarEngineService.getFixes(it.text).map { fix -> it to fix }
                }.flatten().map {
                    typoToProblemDescriptors(it.second, TextBlock(it.first, it.first.text), manager, isOnTheFly, ext)
                }

                return (problemDescriptorsForComments + problemDescriptorsForTags).toMutableList()
            }
        }
    }

    override fun extract(file: PsiFile): List<TextBlock>? {
        return getCommentData(file)
    }

    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val newText = range.replace(textBlock.element.text, replacement)
        (textBlock.element as? PsiDocTokenImpl)?.replaceWithText(newText)
    }

    private fun getCommentData(elem: PsiElement): List<TextBlock>? {
        val tokens = PsiTreeUtil.collectElementsOfType(elem, PsiDocTokenImpl::class.java).filter {
            (it.elementType as? IJavaDocElementType)?.toString()?.equals("DOC_COMMENT_DATA")
                    ?: false
        }

        val element = JavaDocTextElement(tokens)

        return listOf(TextBlock(element, ""))
    }
}
