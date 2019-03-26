package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import tanvd.grazi.model.GrammarEngine

class GraziInspection : AbstractBaseJavaLocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<GraziLanguageSupport>("tanvd.grazi.languageSupport")
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(file)
            if (blocks != null) {
                return checkBlocks(blocks, manager, isOnTheFly, ext)
            }
        }
        return emptyArray()
    }

    private fun checkBlocks(
            blocks: List<TextBlock>,
            manager: InspectionManager,
            isOnTheFly: Boolean,
            ext: GraziLanguageSupport
    ): Array<ProblemDescriptor> {
        val result = mutableListOf<ProblemDescriptor>()
        for (block in blocks) {
            val fixes = GrammarEngine.getFixes(block.text)
            fixes.forEach {
                val range = TextRange.create(it.range.start, it.range.endInclusive)
                val quickFixes = it.fix?.map { GraziQuickFix(ext, block, range, it) }?.toTypedArray() ?: emptyArray()
                result += manager.createProblemDescriptor(block.element, range,
                        it.description, ProblemHighlightType.ERROR, isOnTheFly, *quickFixes)
            }
        }
        return result.toTypedArray()
    }

    /**
     * Override this to report problems at method level.
     *
     * @param method     to check.
     * @param manager    InspectionManager to ask for ProblemDescriptors from.
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     * @return `null` if no problems found or not applicable at method level.
     */
    override fun checkMethod(method: PsiMethod, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return null
    }

    /**
     * Override this to report problems at class level.
     *
     * @param aClass     to check.
     * @param manager    InspectionManager to ask for ProblemDescriptors from.
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     * @return `null` if no problems found or not applicable at class level.
     */
    override fun checkClass(aClass: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return null
    }

    /**
     * Override this to report problems at field level.
     *
     * @param field      to check.
     * @param manager    InspectionManager to ask for ProblemDescriptors from.
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     * @return `null` if no problems found or not applicable at field level.
     */
    override fun checkField(field: PsiField, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return null
    }
}
