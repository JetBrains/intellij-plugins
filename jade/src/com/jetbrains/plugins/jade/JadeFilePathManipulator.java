package com.jetbrains.plugins.jade;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import com.jetbrains.plugins.jade.psi.impl.JadeFilePathImpl;
import org.jetbrains.annotations.NotNull;

final class JadeFilePathManipulator extends AbstractElementManipulator<JadeFilePathImpl> {
  @Override
  public JadeFilePathImpl handleContentChange(final @NotNull JadeFilePathImpl element, final @NotNull TextRange range, final String newContent)
    throws IncorrectOperationException {
    StringBuilder sb = new StringBuilder();
    String oldText = element.getText();
    sb.append(oldText, 0, range.getStartOffset());
    sb.append(JadeToPugTransitionHelper.trimAnyExtension(newContent, JadeToPugTransitionHelper.ALL_EXTENSIONS));
    sb.append(oldText.substring(range.getEndOffset()));
    PsiFile file = PsiFileFactory.getInstance(element.getProject())
      .createFileFromText("dummy." + JadeFileType.INSTANCE.getDefaultExtension(), "extends " + sb);
    PsiElement extendsStatement = file.getFirstChild().getFirstChild();
    JadeFilePathImpl filePathElement = PsiTreeUtil.findChildOfType(extendsStatement, JadeFilePathImpl.class);
    element.getFirstChild().replace(filePathElement.getFirstChild());
    return element;
  }
}
