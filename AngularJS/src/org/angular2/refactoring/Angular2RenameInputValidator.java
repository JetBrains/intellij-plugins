// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.util.ProcessingContext;
import org.angular2.entities.Angular2DirectiveSelectorSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class Angular2RenameInputValidator implements RenameInputValidator {

  private static final Pattern TAG_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*");
  private static final Pattern ATTRIBUTE_NAME_PATTERN = Pattern.compile("[^\\p{Space}\"'>/=\\p{Cntrl}]+");

  @Override
  public @NotNull ElementPattern<? extends PsiElement> getPattern() {
    return null;//PlatformPatterns.psiElement(Angular2DirectiveSelectorSymbol.class);
  }

  @Override
  public boolean isInputValid(@NotNull String newName, @NotNull PsiElement element, @NotNull ProcessingContext context) {
    Angular2DirectiveSelectorSymbol selector = (Angular2DirectiveSelectorSymbol)element;
    return selector.isElementSelector()
           ? TAG_NAME_PATTERN.matcher(newName).matches()
           : selector.isAttributeSelector()
             ? ATTRIBUTE_NAME_PATTERN.matcher(newName).matches()
             : error();
  }

  private static boolean error() {
    throw new IllegalStateException();
  }
}
