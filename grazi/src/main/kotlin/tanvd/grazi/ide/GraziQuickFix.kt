package tanvd.grazi.ide

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import java.util.regex.Pattern


class GraziQuickFix(private val ext: GraziLanguageSupport, private val block: TextBlock,
                    private val textRange: TextRange, private val replacement: String) : LocalQuickFix {
    override fun getName(): String {
        return "Replace with '" + StringUtil.shortenTextWithEllipsis(replacement, 20, 0, true) + "'"
    }

    override fun getFamilyName(): String = "Replace with suggested text"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (!block.element.isValid) return

        val start = block.element.textRange.startOffset
        val document = block.element.containingFile.viewProvider.document

        val adjusted = if (replacement.startsWith(".")) TextRange.create(textRange.startOffset - 1, textRange.endOffset) else textRange
        ext.replace(block, adjusted, replacement)

        val matcher = Pattern.compile("\\$\\w+\\$").matcher(replacement)
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (matcher.find() && editor?.document == document) {
            val matchStart = start + adjusted.startOffset + matcher.start(0)
            val matchEnd = matchStart + matcher.end(0) - matcher.start(0)
            editor!!.caretModel.moveToOffset(matchStart)
            editor.selectionModel.setSelection(matchStart, matchEnd)
        }
    }
}
