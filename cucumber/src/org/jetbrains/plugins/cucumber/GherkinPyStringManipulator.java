// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.psi.GherkinPystring;

import static org.jetbrains.plugins.cucumber.psi.GherkinLexer.PYSTRING_MARKER;

public class GherkinPyStringManipulator extends AbstractElementManipulator<GherkinPystring> {
  private static final String PY_STRING_FILE_TEMPLATE =
    "Feature: \n" +
    "  Scenario: Test\n" +
    "    Given step\n" +
    "\"\"\"%s\"\"\"";

  @Override
  public @NotNull TextRange getRangeInElement(@NotNull GherkinPystring element) {
    return TextRange.create(PYSTRING_MARKER.length(), element.getTextLength() - PYSTRING_MARKER.length());
  }

  @Override
  public @Nullable GherkinPystring handleContentChange(@NotNull GherkinPystring element,
                                                       @NotNull TextRange range, String newContent) throws IncorrectOperationException {


    String dummyFileText = String.format(PY_STRING_FILE_TEMPLATE, newContent);
    PsiFile dummyFile =
      PsiFileFactory.getInstance(element.getProject()).createFileFromText("test.feature", GherkinFileType.INSTANCE, dummyFileText);
    PsiElement pyStringQuotes = dummyFile.findElementAt(dummyFile.getTextLength() - 1);
    if (pyStringQuotes != null && pyStringQuotes.getParent() instanceof GherkinPystring) {
      GherkinPystring pyStringElement = (GherkinPystring)pyStringQuotes.getParent();
      return (GherkinPystring)element.replace(pyStringElement);
    }
    return element;
  }
}
