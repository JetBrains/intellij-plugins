package com.google.jstestdriver.idea.testIntegration;

import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdQUnitCreateTestAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Context context = fetchContext(dataContext);
    if (context == null) {
      throw new RuntimeException("Can't fetch " + JstdQUnitCreateTestAction.class.getSimpleName() + " from ActionEvent " + e);
    }
    final PsiElement topPsiElement = getTopmostPsiElements(context.elementUnderCaret);
    final Project project = topPsiElement.getProject();

    Document document = context.editor.getDocument();
    final int offset;
    if (topPsiElement instanceof PsiWhiteSpace) {
      int lineNo = document.getLineNumber(context.caretOffsetInDocument);
      offset = document.getLineStartOffset(lineNo);
    } else {
      offset = topPsiElement.getTextOffset();
    }

    context.editor.getCaretModel().moveToOffset(offset);

    TemplateManager templateManager = TemplateManager.getInstance(project);
    TemplateImpl template = new TemplateImpl("", "");
    template.setKey("jstd.generate.create-qunit-test");
    template.setDescription("sync edit template");
    template.setToIndent(true);
    template.setToReformat(true);
//            template.parseSegments();
    template.setToShortenLongNames(false);
    template.setInline(false);

    template.addTextSegment("test");
    template.addTextSegment("(");
    template.addTextSegment("\"");
    template.addVariable("NAME", new ConstantNode("name"), new ConstantNode("name"), true);
    template.addTextSegment("\", ");
    template.addTextSegment("function()");
    template.addTextSegment("{");
    template.addEndVariable();
    template.addTextSegment("}");
    template.addTextSegment(");\n");
    template.addEndVariable();

    templateManager.startTemplate(context.editor, "", template);
  }

  @Nullable
  private Context fetchContext(DataContext dataContext) {
    Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    if (editor == null) {
      return null;
    }
    PsiFile psiFile = LangDataKeys.PSI_FILE.getData(dataContext);
    if (!(psiFile instanceof JSFile)) {
      return null;
    }
    int caretOffsetInDocument = editor.getCaretModel().getOffset();
    PsiElement psiElementUnderCaret = psiFile.findElementAt(caretOffsetInDocument);
    if (psiElementUnderCaret == null) {
      psiElementUnderCaret = psiFile.getLastChild();
    }
    return psiElementUnderCaret != null ? new Context(psiElementUnderCaret, caretOffsetInDocument, editor) : null;
  }

  private PsiElement getTopmostPsiElements(@NotNull PsiElement psiElement) {
    PsiElement lastElement = psiElement;
    while (!(psiElement instanceof PsiFile)) {
      lastElement = psiElement;
      psiElement = psiElement.getParent();
    }
    return lastElement;
  }

  @Override
  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    Context context = fetchContext(e.getDataContext());

    if (context == null || !(context.getPsiFile() instanceof JSFile)) {
      presentation.setVisible(false);
      return;
    }

    presentation.setVisible(true);
    presentation.setText("QUnit Test", true);
    presentation.setEnabled(true);
  }

  private static class Context {
    final int caretOffsetInDocument;
    final PsiElement elementUnderCaret;
    final Editor editor;

    Context(PsiElement elementUnderCaret, int caretOffsetInDocument, Editor editor) {
      this.caretOffsetInDocument = caretOffsetInDocument;
      this.elementUnderCaret = elementUnderCaret;
      this.editor = editor;
    }

    PsiFile getPsiFile() {
      return elementUnderCaret.getContainingFile();
    }
  }

}
