package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.psi.HbBlockMustache;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.dmarcotte.handlebars.psi.HbPath;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

class HbBlockMismatchFix implements IntentionAction {
  private final boolean myUpdateOpenMustache;
  private final String myCorrectedName;
  private final String myOriginalName;

  /**
   * @param correctedName      The name this action will update a block element to
   * @param originalName       The original name of the element this action corrects
   * @param updateOpenMustache Whether or not this updates the open mustache of this block
   */
  public HbBlockMismatchFix(String correctedName, String originalName, boolean updateOpenMustache) {
    myUpdateOpenMustache = updateOpenMustache;
    myCorrectedName = correctedName;
    myOriginalName = originalName;
  }

  @NotNull
  @Override
  public String getText() {
    return getName();
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getName();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return true;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file)
    throws IncorrectOperationException {
    final int offset = editor.getCaretModel().getOffset();
    PsiElement psiElement = file.findElementAt(offset);

    if (psiElement == null || !psiElement.isValid()) return;
    if (!FileModificationService.getInstance().prepareFileForWrite(psiElement.getContainingFile())) return;

    if (psiElement instanceof PsiWhiteSpace) psiElement = PsiTreeUtil.prevLeaf(psiElement);

    HbBlockMustache blockMustache = (HbBlockMustache)PsiTreeUtil.findFirstParent(psiElement, true, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement psiElement) {
        return psiElement instanceof HbBlockMustache;
      }
    });

    if (blockMustache == null) {
      return;
    }

    HbBlockMustache targetBlockMustache = blockMustache;

    // ensure we update the open or close mustache for this block appropriately
    if (myUpdateOpenMustache != (targetBlockMustache instanceof HbOpenBlockMustache)) {
      targetBlockMustache = blockMustache.getPairedElement();
    }

    HbPath path = PsiTreeUtil.findChildOfType(targetBlockMustache, HbPath.class);
    final Document document = PsiDocumentManager.getInstance(project).getDocument(file);
    if (path != null && document != null) {
      final TextRange textRange = path.getTextRange();
      document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), myCorrectedName);
    }
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  private String getName() {
    return myUpdateOpenMustache
           ? HbBundle.message("hb.block.mismatch.intention.rename.open", myOriginalName, myCorrectedName)
           : HbBundle.message("hb.block.mismatch.intention.rename.close", myOriginalName, myCorrectedName);
  }
}
