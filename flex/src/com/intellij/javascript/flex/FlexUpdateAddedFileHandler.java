// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.javascript.flex.refactoring.RenameMoveUtils;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.UpdateAddedFileProcessor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public final class FlexUpdateAddedFileHandler extends UpdateAddedFileProcessor{
  @Override
  public boolean canProcessElement(final @NotNull PsiFile element) {
    return element instanceof JSFile || FlexSupportLoader.isFlexMxmFile(element);
  }

  @Override
  public void update(final PsiFile element, PsiFile originalElement) throws IncorrectOperationException {
    if (element instanceof JSFile file) {
      RenameMoveUtils.updateFileWithChangedName(file);
      RenameMoveUtils.prepareMovedFile(file);
      RenameMoveUtils.updateMovedFile(file);
    } else if (element instanceof XmlFile file) {
      RenameMoveUtils.prepareMovedMxmlFile(file, (XmlFile)originalElement);
      RenameMoveUtils.updateMovedMxmlFile(file);
    }
  }
}
