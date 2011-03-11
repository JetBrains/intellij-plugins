package com.intellij.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.refactoring.RenameMoveUtils;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;

import java.util.List;
import java.util.Map;

/**
 * @author Maxim.Mossienko
 *         Date: Sep 18, 2008
 *         Time: 3:49:24 PM
 */
public class FlexMoveFileHandler extends MoveFileHandler {
  public boolean canProcessElement(final PsiFile element) {
    return element instanceof JSFile || JavaScriptSupportLoader.isFlexMxmFile(element);
  }

  public void prepareMovedFile(final PsiFile file, PsiDirectory moveDestination, Map<PsiElement, PsiElement> oldToNewMap) {
    if (file instanceof JSFile) {
      RenameMoveUtils.prepareMovedFile((JSFile)file);
    } else if (file instanceof XmlFile) {
      RenameMoveUtils.prepareMovedMxmlFile((XmlFile)file, null);
    }
  }

  @Override
  public List<UsageInfo> findUsages(PsiFile psiFile, PsiDirectory newParent, boolean searchInComments, boolean searchInNonJavaFiles) {
    return null;
  }

  @Override
  public void retargetUsages(List<UsageInfo> usageInfos, Map<PsiElement, PsiElement> oldToNewMap) {
    
  }

  public void updateMovedFile(final PsiFile file) throws IncorrectOperationException {
    if (file instanceof JSFile) {
      RenameMoveUtils.updateMovedFile((JSFile)file);
    } else if (file instanceof XmlFile) {
      RenameMoveUtils.updateMovedMxmlFile((XmlFile)file);
    }
  }
}
