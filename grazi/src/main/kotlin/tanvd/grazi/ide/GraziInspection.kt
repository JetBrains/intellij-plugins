package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
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

    private fun checkBlocks(blocks: List<TextBlock>, manager: InspectionManager, isOnTheFly: Boolean,
                            ext: GraziLanguageSupport): Array<ProblemDescriptor> {
        val result = mutableListOf<ProblemDescriptor>()
        for (block in blocks) {
            val fixes = GrammarEngine.getFixes(block.text)
            fixes.forEach { fix ->
                val range = TextRange.create(fix.range.start, fix.range.endInclusive)
                val quickFixes = fix.fix?.map { GraziQuickFix(fix.category.description, ext, block, range, it) }?.toTypedArray() ?: emptyArray()
                val problemDescriptor = manager.createProblemDescriptor(block.element, range,
                        fix.fullDescription, fix.category.highlight, isOnTheFly, *quickFixes)

                result += problemDescriptor
            }
        }
        return result.toTypedArray()
    }

    override fun checkMethod(method: PsiMethod, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(method)
            if (blocks != null) {
                return checkBlocks(blocks, manager, isOnTheFly, ext)
            }
        }
        return emptyArray()
    }

    override fun checkClass(aClass: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(aClass)
            if (blocks != null) {
                return checkBlocks(blocks, manager, isOnTheFly, ext)
            }
        }
        return emptyArray()
    }

    override fun checkField(field: PsiField, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(field)
            if (blocks != null) {
                return checkBlocks(blocks, manager, isOnTheFly, ext)
            }
        }
        return emptyArray()
    }
}
