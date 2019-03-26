package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import tanvd.grazi.model.GrammarEngine

class GraziInspection : LocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<GraziLanguageSupport>("tanvd.grazi.languageSupport")
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(file)
            if (blocks != null) {
                return checkBlocks(blocks, manager, isOnTheFly)
            }
        }
        return null
    }

    private fun checkBlocks(
            blocks: List<TextBlock>,
            manager: InspectionManager,
            isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val result = mutableListOf<ProblemDescriptor>()
        for (block in blocks) {
            val fixes = GrammarEngine.getFixes(block.text)
            fixes.forEach {
                manager.createProblemDescriptor(block.element, TextRange.create(it.range.start, it.range.endInclusive),
                        it.description, ProblemHighlightType.ERROR, isOnTheFly)
            }
        }
        return result.toTypedArray()
    }
}
