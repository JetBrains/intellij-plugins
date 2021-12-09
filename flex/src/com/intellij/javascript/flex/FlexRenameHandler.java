package com.intellij.javascript.flex;

import com.intellij.javascript.flex.refactoring.RenameMoveUtils;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.findUsages.JSSuperMemberUtil;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.SearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Maxim.Mossienko
 */
public class FlexRenameHandler extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(@NotNull final PsiElement element) {
    return (element instanceof JSFunction || element instanceof JSFile) &&
           DialectDetector.isActionScript(element.getContainingFile());
  }

  @Override
  public void renameElement(@NotNull final PsiElement element,
                            @NotNull final String newName,
                            final UsageInfo @NotNull [] usages,
                            @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    super.renameElement(element, newName, usages, listener);
    if (element instanceof JSFile) {
      // rename invalidates file, so we need to get actual one
      PsiFile file = element.getManager().findFile(element.getContainingFile().getVirtualFile());
      if (file instanceof JSFile) RenameMoveUtils.updateFileWithChangedName((JSFile)file);
    }
  }

  @Override
  public PsiElement substituteElementToRename(@NotNull final PsiElement element, final Editor editor) {
    if (element instanceof JSFunction) {
      return JSSuperMemberUtil.checkSuperMember((JSFunction)element, RefactoringBundle.message("rename.title"),
                                                RefactoringBundle.message("to.rename"));
    }
    return super.substituteElementToRename(element, editor);
  }

  @Override
  public void prepareRenaming(@NotNull final PsiElement element,
                              @NotNull final String newName,
                              @NotNull final Map<PsiElement, String> allRenames,
                              @NotNull final SearchScope scope) {
    if (!(element instanceof JSFunction)) {
      return;
    }

    JSInheritanceUtil.iterateMethodsDown((JSFunction)element, new Processor<>() {
      // synchronized to protect the map
      @Override
      public synchronized boolean process(final JSFunction jsFunction) {
        allRenames.put(jsFunction, newName);
        return true;
      }
    }, true); // check override modifier to keep consistency with current logic
  }
}