// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.css;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.css.reference.CssReference;
import com.intellij.psi.css.resolve.CssElementProcessor;
import com.intellij.psi.css.resolve.CssResolveManager;
import com.intellij.psi.css.resolve.impl.CssResolverImpl;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiPolyVariantCachingReference;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class CssClassValueReference extends PsiPolyVariantCachingReference implements CssReference {
  private final PsiElement myElement;
  private final int myStart;
  private final int myEnd;

  public CssClassValueReference(@NotNull PsiElement element) {
    myElement = element;
    String value = getValue(myElement);
    int length = value != null ? value.length() : 0;
    if (length == 0) {
      myStart = 0;
      myEnd = 0;
    }
    else if (element instanceof CssString || FlexCssUtil.inQuotes(myElement.getText())) {
      final String text = myElement.getText();
      myStart = text.length() >= 2 && text.charAt(1) == '.' ? 2 : 1;
      myEnd = length + 1;
    }
    else {
      myStart = 0;
      myEnd = length;
    }
  }

  public static String getValue(PsiElement element) {
    if (element instanceof CssString) {
      return ((CssString)element).getValue();
    }
    else {
      String text = element.getText();
      if (FlexCssUtil.inQuotes(text)) {
        return text.substring(text.length() >= 2 && text.charAt(1) == '.' ? 2 : 1, text.length() - 1);
      }
      else {
        return text;
      }
    }
  }

  @NotNull
  @Override
  public String getUnresolvedMessagePattern() {
    return CssBundle.message("invalid.css.class");
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    return new TextRange(myStart, myEnd);
  }

  @Override
  @NotNull
  public String getCanonicalText() {
    String value = getValue(myElement);
    return value != null ? value : "";
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return ElementManipulators.handleContentChange(myElement, getRangeInElement(), newElementName);
  }

  @Override
  @Nullable
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public Object @NotNull [] getVariants() {
    MyCandidatesProcessor processor = new MyCandidatesProcessor();
    processStyles(processor);
    return processor.myStyleNames.toArray();
  }

  @Override
  protected ResolveResult @NotNull [] resolveInner(boolean incompleteCode, @NotNull PsiFile containingFile) {
    String value = getValue(myElement);
    if (value == null) return ResolveResult.EMPTY_ARRAY;
    MyResolveProcessor processor = new MyResolveProcessor(value);
    processStyles(processor);
    if (processor.myTargets.isEmpty()) {
      return ResolveResult.EMPTY_ARRAY;
    }
    return PsiElementResolveResult.createResults(processor.myTargets);
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    if (element instanceof CssSelectorSuffix) {
      String text = element.getText();
      return text != null && !text.isEmpty() && text.substring(1).equals(getValue(myElement));
    }
    return false;
  }

  @Override
  public boolean isSoft() {
    return true;
  }

  private void processStyles(CssElementProcessor processor) {
    PsiFile file = myElement.getContainingFile();
    if (!(file instanceof XmlFile)) {
      PsiElement context = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(file);
      if (context instanceof XmlFile) {
        file = (XmlFile)context;
      }
    }
    if (file instanceof XmlFile) {
      CssResolveManager.getInstance().getNewResolver().processOneFile((XmlFile)file, processor, true);
    }
    else if (file instanceof StylesheetFile) {
      processOneStylesheetFile((StylesheetFile)file, processor);
    }
    Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module != null) {
      CssResolverImpl.processStyles(module, processor, file);
    }
  }

  private static void processOneStylesheetFile(@NotNull StylesheetFile file, @NotNull CssElementProcessor processor) {
    CssStylesheet stylesheet = file.getStylesheet();
    if (stylesheet != null) {
      for (CssRuleset ruleset : stylesheet.getRulesets()) {
        processor.process(ruleset);
      }
    }
  }

  private static class MyCandidatesProcessor extends MyCssElementProcessor {
    Set<String> myStyleNames = new LinkedHashSet<>();

    @Override
    protected void handleSelector(@NotNull CssSelectorSuffix selectorSuffix, @NotNull String text) {
      myStyleNames.add(text);
    }
  }

  private static final class MyResolveProcessor extends MyCssElementProcessor {
    private final String myReferenceText;
    private final Set<CssSelectorSuffix> myTargets = new LinkedHashSet<>();

    private MyResolveProcessor(@NotNull String referenceText) {
      myReferenceText = referenceText;
    }

    @Override
    protected void handleSelector(@NotNull CssSelectorSuffix selectorSuffix, @NotNull String text) {
      if (text.equals(myReferenceText)) {
        myTargets.add(selectorSuffix);
      }
    }
  }

  private abstract static class MyCssElementProcessor extends CssElementProcessor {
    @Override
    public boolean process(@NotNull CssRuleset ruleset) {
      for (CssSelector selector : ruleset.getSelectors()) {
        for (PsiElement child : selector.getChildren()) {
          if (child instanceof CssSimpleSelector) {
            for (CssSelectorSuffix selectorSuffix : ((CssSimpleSelector)child).getSelectorSuffixes()) {
              String text = selectorSuffix.getText();
              if (text != null && !text.isEmpty() && text.charAt(0) == '.') {
                handleSelector(selectorSuffix, text.substring(1));
                ProgressIndicatorProvider.checkCanceled();
              }
            }
          }
        }
      }
      return true;
    }

    protected abstract void handleSelector(@NotNull CssSelectorSuffix selectorSuffix, @NotNull String selectorName);
  }
}
