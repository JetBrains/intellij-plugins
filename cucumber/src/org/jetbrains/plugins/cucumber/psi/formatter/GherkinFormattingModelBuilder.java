// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.formatter;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.psi.PsiFile;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

public class GherkinFormattingModelBuilder implements FormattingModelBuilder {
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    final PsiFile file = formattingContext.getContainingFile();
    final FileElement fileElement = TreeUtil.getFileElement(
      (TreeElement)SourceTreeToPsiMap.psiElementToTree(formattingContext.getPsiElement()));
    final GherkinBlock rootBlock = new GherkinBlock(fileElement);
    //FormattingModelDumper.dumpFormattingModel(rootBlock, 0, System.out);
    return new DocumentBasedFormattingModel(rootBlock, file.getProject(), formattingContext.getCodeStyleSettings(), file.getFileType(),
                                            file);
  }
}
