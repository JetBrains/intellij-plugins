// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartImportStatement;
import com.jetbrains.lang.dart.psi.DartUriElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartUriElementBase extends DartPsiCompositeElementImpl implements DartUriElement {

  private static final Condition<PsiFileSystemItem> DART_FILE_OR_DIR_FILTER = item -> item.isDirectory() || item instanceof DartFile;

  public DartUriElementBase(final @NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable PsiReference getReference() {
    final PsiReference[] references = getReferences();
    return references.length == 0 ? null : references[0];
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return new PsiReference[]{new DartFileReference(this, getUriStringAndItsRange().first)};
  }

  public static final class DartUriElementManipulator extends AbstractElementManipulator<DartUriElement> {
    @Override
    public DartUriElement handleContentChange(final @NotNull DartUriElement oldUriElement,
                                              final @NotNull TextRange range,
                                              final @NotNull String newContent) {
      final String newUriElementText = StringUtil.replaceSubstring(oldUriElement.getText(), getRangeInElement(oldUriElement), newContent);
      final PsiFile fileFromText = PsiFileFactory.getInstance(oldUriElement.getProject())
        .createFileFromText(DartLanguage.INSTANCE, "import " + newUriElementText + ";");

      final DartImportStatement importStatement = PsiTreeUtil.findChildOfType(fileFromText, DartImportStatement.class);
      assert importStatement != null : fileFromText.getText();

      return (DartUriElement)oldUriElement.replace(importStatement.getUriElement());
    }

    @Override
    public @NotNull TextRange getRangeInElement(final @NotNull DartUriElement element) {
      return element.getUriStringAndItsRange().second;
    }
  }
}
