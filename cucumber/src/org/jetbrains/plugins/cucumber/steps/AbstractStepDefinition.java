package org.jetbrains.plugins.cucumber.steps;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.CommonProcessors.CollectProcessor;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author yole, Andrey Vokin
 */
public abstract class AbstractStepDefinition {
  private static final java.util.regex.Pattern ESCAPE_PATTERN
    = java.util.regex.Pattern.compile("(\\$\\w+|#\\{.+?\\})");

  private static final String CUCUMBER_START_PREFIX = "\\A";

  private static final String CUCUMBER_END_SUFFIX = "\\z";

  private final SmartPsiElementPointer<PsiElement> myElementPointer;

  private volatile String myRegexText;

  private volatile Pattern myRegex;

  public AbstractStepDefinition(@NotNull final PsiElement element) {
    myElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
  }

  public abstract List<String> getVariableNames();

  public boolean matches(String stepName) {
    final Pattern pattern = getPattern();
    return pattern != null && new Perl5Matcher().contains(stepName, pattern);
  }

  @Nullable
  public PsiElement getElement() {
    return myElementPointer.getElement();
  }

  /**
   * @return regexp pattern for step or null if regexp is malformed
   */
  @Nullable
  public Pattern getPattern() {
    try {
      final String cucumberRegex = getCucumberRegex();
      if (cucumberRegex == null) return null;
      if (myRegexText == null || !cucumberRegex.equals(myRegexText)) {
        final StringBuilder patternText = new StringBuilder(ESCAPE_PATTERN.matcher(cucumberRegex).replaceAll("(.*)"));
        if (patternText.toString().startsWith(CUCUMBER_START_PREFIX)) {
          patternText.replace(0, CUCUMBER_START_PREFIX.length(), "^");
        }

        if (patternText.toString().endsWith(CUCUMBER_END_SUFFIX)) {
          patternText.replace(patternText.length() - CUCUMBER_END_SUFFIX.length(), patternText.length(), "$");
        }

        myRegex = new Perl5Compiler().compile(patternText.toString(), Perl5Compiler.CASE_INSENSITIVE_MASK);
        myRegexText = cucumberRegex;
      }
      return myRegex;
    }
    catch (final MalformedPatternException ignored) {
      return null; // Bad regex?
    }
  }

  @Nullable
  public String getCucumberRegex() {
    return getCucumberRegexFromElement(getElement());
  }

  @Nullable
  protected abstract String getCucumberRegexFromElement(PsiElement element);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractStepDefinition that = (AbstractStepDefinition)o;

    if (!myElementPointer.equals(that.myElementPointer)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myElementPointer.hashCode();
  }

  /**
   * Set new value for step definitions (most likely provided by refactor->rename)
   * @param newValue
   */
  public void setCucumberRegex(@NotNull final String newValue) {
  }

  /**
   * Checks if step definitions point supports certain step (i.e. some step definitions does not support some keywords)
   *
   * @param step Step to check
   * @return true if supports.
   */
  public boolean supportsStep(@NotNull final PsiElement step) {
    return true;
  }

  /**
   * Checks if step definition supports rename.
   * @param newName if null -- check if definition supports renaming at all (regardless new name).
   *                If not null -- check if it can be renamed to the new (provided) name.
   * @return true if rename is supportged
   */
  public boolean supportsRename(@Nullable final String newName) {
    return true;
  }

  /**
   * Finds all steps points to this definition in some scope
   *
   * @param searchScope scope to find steps
   * @return steps
   */
  @NotNull
  public Collection<GherkinStep> findSteps(@NotNull final SearchScope searchScope) {
    final String regex = getCucumberRegex();
    final PsiElement element = getElement();
    if ((regex == null) || (element == null)) {
      return Collections.emptyList();
    }

    final CollectProcessor<PsiReference> consumer = new CollectProcessor<>();
    CucumberUtil.findGherkinReferencesToElement(element, regex, consumer, searchScope);

    // We use hash to get rid of duplicates
    final Collection<GherkinStep> results = new HashSet<>(consumer.getResults().size());
    for (final PsiReference reference : consumer.getResults()) {
      final PsiElement step = reference.getElement();
      if (step instanceof GherkinStep) {
        results.add((GherkinStep)step);
      }
    }
    return results;
  }
}
