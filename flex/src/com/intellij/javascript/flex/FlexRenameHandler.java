package com.intellij.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.findUsages.SuperMethodUtil;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.refactoring.RenameMoveUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 *         Date: Sep 18, 2008
 *         Time: 3:03:37 PM
 */
public class FlexRenameHandler extends RenamePsiElementProcessor {
  public boolean canProcessElement(@NotNull final PsiElement element) {
    return element instanceof JSFunction || (element instanceof JSFile && element.getLanguage() == JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  @Override
  public void renameElement(final PsiElement element,
                            final String newName,
                            final UsageInfo[] usages, final RefactoringElementListener listener) throws IncorrectOperationException {
    super.renameElement(element, newName, usages, listener);
    if (element instanceof JSFile) {
      // rename invalidates file, so we need to get actual one
      PsiFile file = PsiManager.getInstance(element.getProject()).findFile(element.getContainingFile().getVirtualFile());
      if (file instanceof JSFile) RenameMoveUtils.updateFileWithChangedName((JSFile)file);
    }
  }

  @Override
   public PsiElement substituteElementToRename(final PsiElement element, final Editor editor) {
    if (element instanceof JSFunction) {
      return SuperMethodUtil
        .checkSuperMethod((JSFunction)element, RefactoringBundle.message("rename.title"), RefactoringBundle.message("to.rename"));
    }
    return super.substituteElementToRename(element, editor);
  }
}
