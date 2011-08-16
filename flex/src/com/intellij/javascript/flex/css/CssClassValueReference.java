package com.intellij.javascript.flex.css;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.css.impl.util.references.CssReference;
import com.intellij.psi.css.resolve.CssElementProcessor;
import com.intellij.psi.css.resolve.CssResolveManager;
import com.intellij.psi.css.resolve.impl.CssResolverImpl;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiPolyVariantCachingReference;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Eugene.Kudelevsky
 */
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

  public String getUnresolvedMessage() {
    return CssBundle.message("invalid.css.class");
  }

  public PsiElement getElement() {
    return myElement;
  }

  public TextRange getRangeInElement() {
    return new TextRange(myStart, myEnd);
  }

  @NotNull
  public String getCanonicalText() {
    String value = getValue(myElement);
    return value != null ? value : "";
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(myElement);
    assert manipulator != null;
    return manipulator.handleContentChange(myElement, getRangeInElement(), newElementName);
  }

  @Nullable
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @NotNull
  public Object[] getVariants() {
    MyCandidatesProcessor processor = new MyCandidatesProcessor();
    processStyles(processor);
    return processor.myStyleNames.toArray();
  }

  @NotNull
  @Override
  protected ResolveResult[] resolveInner(boolean incompleteCode) {
    String value = getValue(myElement);
    if (value == null) return ResolveResult.EMPTY_ARRAY;
    MyResolveProcessor processor = new MyResolveProcessor(value);
    processStyles(processor);
    if (processor.myTargets.size() == 0) {
      return ResolveResult.EMPTY_ARRAY;
    }
    return PsiElementResolveResult.createResults(processor.myTargets);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    if (element instanceof CssSelectorSuffix) {
      String text = element.getText();
      return text != null && text.length() > 0 && text.substring(1).equals(getValue(myElement));
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
      PsiElement context = file.getContext();
      if (context != null) {
        context = context.getContainingFile();
      }
      if (context instanceof XmlFile) {
        file = (XmlFile)context;
      }
    }
    if (file instanceof XmlFile) {
      CssResolveManager.getInstance().getNewResolver().processOneFile((XmlFile)file, processor, true);
    }
    else if (file instanceof CssFile) {
      processOneCssFile((CssFile)file, processor);
    }
    Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module != null) {
      CssResolverImpl.processStyles(module, processor, file);
    }
  }

  private static void processOneCssFile(@NotNull CssFile file, @NotNull CssElementProcessor processor) {
    CssStylesheet stylesheet = file.getStylesheet();
    if (stylesheet != null) {
      for (CssRuleset ruleset : stylesheet.getRulesets()) {
        processor.process(ruleset);
      }
    }
  }

  private static class MyCandidatesProcessor extends MyCssElementProcessor {
    Set<String> myStyleNames = new HashSet<String>();

    protected void handleSelector(@NotNull CssSelectorSuffix selectorSuffix, @NotNull String text) {
      myStyleNames.add(text);
    }
  }

  private static class MyResolveProcessor extends MyCssElementProcessor {
    private final String myReferenceText;
    private Set<CssSelectorSuffix> myTargets = new HashSet<CssSelectorSuffix>();

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

  private static abstract class MyCssElementProcessor implements CssElementProcessor {
    public void process(CssRuleset ruleset) {
      if (ruleset == null) return;
      CssSelectorList selectorList = ruleset.getSelectorList();
      if (selectorList == null) return;
      for (CssSelector selector : selectorList.getSelectors()) {
        for (PsiElement child : selector.getChildren()) {
          if (child instanceof CssSimpleSelector) {
            for (CssSelectorSuffix selectorSuffix : ((CssSimpleSelector)child).getSelectorSuffixes()) {
              String text = selectorSuffix.getText();
              if (text != null && text.length() > 0 && text.charAt(0) == '.') {
                handleSelector(selectorSuffix, text.substring(1));
              }
            }
          }
        }
      }
    }

    public void process(CssDeclaration declaration) {
    }

    @Override
    public void process(CssStylesheet stylesheet) {
    }

    protected abstract void handleSelector(@NotNull CssSelectorSuffix selectorSuffix, @NotNull String selectorName);
  }
}
