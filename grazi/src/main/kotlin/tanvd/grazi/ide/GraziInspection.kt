package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.GrammarEngineService
import tanvd.grazi.ide.language.JavaDocSupport
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.model.TextBlock
import tanvd.grazi.model.Typo

class GraziInspection : LocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<LanguageSupport>("tanvd.grazi.languageSupport")

        fun typoToProblemDescriptors(fix: Typo, block: TextBlock, manager: InspectionManager,
                                     isOnTheFly: Boolean, ext: LanguageSupport): ProblemDescriptor {
            val range = TextRange.create(fix.range.start, fix.range.endInclusive + 1)
            val quickFixes = fix.fix?.map { GraziQuickFix(fix.category.description, ext, block, range, it) }?.toTypedArray() ?: emptyArray()
            @Suppress("SpreadOperator")
            return manager.createProblemDescriptor(block.element, range,
                    fix.fullDescription, fix.category.highlight, isOnTheFly, *quickFixes)
        }
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val result = mutableListOf<ProblemDescriptor>()
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(file)
            if (blocks != null) {
                result += checkBlocks(blocks, manager, isOnTheFly, ext)
            }
        }
        return result.toTypedArray()
    }

    private fun checkBlocks(blocks: List<TextBlock>, manager: InspectionManager, isOnTheFly: Boolean,
                            ext: LanguageSupport): MutableList<ProblemDescriptor> {
        val result = mutableListOf<ProblemDescriptor>()
        for (block in blocks) {
            if (block.element::class.java == JavaDocSupport.Companion.JavaDocTextElement::class.java) {
                result += (block.element as JavaDocSupport.Companion.JavaDocTextElement).getFixes(manager, isOnTheFly, ext)
                continue
            }
            val fixes = GrammarEngineService.getInstance().getFixes(block.text)
            fixes.forEach { fix ->
                result += typoToProblemDescriptors(fix, block, manager, isOnTheFly, ext)
            }
        }
        return result
    }
}
