package com.intellij.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.refactoring.RenameMoveUtils;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.UpdateAddedFileProcessor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author Maxim.Mossienko
 *         Date: Sep 18, 2008
 *         Time: 3:41:49 PM
 */
public class FlexUpdateAddedFileHandler extends UpdateAddedFileProcessor{
  public boolean canProcessElement(final PsiFile element) {
    return element instanceof JSFile || JavaScriptSupportLoader.isFlexMxmFile(element);
  }

  public void update(final PsiFile element, PsiFile originalElement) throws IncorrectOperationException {
    if (element instanceof JSFile) {
      JSFile file = (JSFile)element;
      RenameMoveUtils.updateFileWithChangedName(file);
      RenameMoveUtils.prepareMovedFile(file);
      RenameMoveUtils.updateMovedFile(file);
    } else if (element instanceof XmlFile) {
      XmlFile file = (XmlFile)element;
      RenameMoveUtils.prepareMovedMxmlFile(file, (XmlFile)originalElement);
      RenameMoveUtils.updateMovedMxmlFile(file);
    }
  }
}
