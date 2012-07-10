package org.jetbrains.plugins.cucumber.steps;


import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
* @author yole, Andrey Vokin
*/
public abstract class AbstractStepDefinition {
  private static final String ourEscapePattern = "(\\$\\w+|#\\{.+\\})";

  private static final String CUCUMBER_START_PREFIX = "\\A";

  private static final String CUCUMBER_END_SUFFIX = "\\z";

  private final SmartPsiElementPointer<PsiElement> myElementPointer;

  public AbstractStepDefinition(@NotNull final PsiElement element) {
    myElementPointer = SmartPointerManager.getInstance(element.getProject()).createLazyPointer(element);
  }

  public abstract List<String> getVariableNames();

  public abstract String getElementText();

  public boolean matches(String stepName) {
    final Pattern pattern = getPattern();
    return pattern != null && new Perl5Matcher().contains(stepName, pattern);
  }

  public PsiElement getElement() {
    return myElementPointer.getElement();
  }

  @Nullable
  public Pattern getPattern() {
     try {
       final String elementText = getElementText();
       if (elementText == null) return null;
       final StringBuilder patternText = new StringBuilder(elementText.replaceAll(ourEscapePattern, "(.*)"));
       if (patternText.toString().startsWith(CUCUMBER_START_PREFIX)) {
         patternText.replace(0, CUCUMBER_START_PREFIX.length(), "^");
       }

       if (patternText.toString().endsWith(CUCUMBER_END_SUFFIX)) {
         patternText.replace(patternText.length() - CUCUMBER_END_SUFFIX.length(), patternText.length(), "$");
       }

       return new Perl5Compiler().compile(patternText.toString());
    } catch (MalformedPatternException e) {
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractStepDefinition that = (AbstractStepDefinition)o;

    if (myElementPointer != null ? !myElementPointer.equals(that.myElementPointer) : that.myElementPointer != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myElementPointer != null ? myElementPointer.hashCode() : 0;
  }
}
