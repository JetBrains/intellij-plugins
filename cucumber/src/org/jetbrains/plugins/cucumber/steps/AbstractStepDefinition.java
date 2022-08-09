// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.CommonProcessors.CollectProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class AbstractStepDefinition {
  private static final Pattern ESCAPE_PATTERN = Pattern.compile("(#\\{.+?})");

  private static final String CUCUMBER_START_PREFIX = "\\A";

  private static final String CUCUMBER_END_SUFFIX = "\\z";
  private static final int TIME_TO_CHECK_STEP_BY_REGEXP_MILLIS = 300;

  private final SmartPsiElementPointer<PsiElement> myElementPointer;

  private volatile String myRegexText;

  private volatile Pattern myRegex;

  public AbstractStepDefinition(@NotNull final PsiElement element) {
    myElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
  }

  public abstract List<String> getVariableNames();

  public boolean matches(@NotNull String stepName) {
    final Pattern pattern = getPattern();
    if (pattern == null) {
      return false;
    }

    CharSequence stepChars = StringUtil.newBombedCharSequence(stepName, TIME_TO_CHECK_STEP_BY_REGEXP_MILLIS);
    try {
      return pattern.matcher(stepChars).find();
    }
    catch (ProcessCanceledException ignore) {
      return false;
    }
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

        myRegex = Pattern.compile(patternText.toString(), isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
        myRegexText = cucumberRegex;
      }
      return myRegex;
    }
    catch (final PatternSyntaxException ignored) {
      return null; // Bad regex?
    }
  }

  @Nullable
  public String getCucumberRegex() {
    return getExpression();
  }

  @Nullable
  @Contract("null -> null")
  protected abstract String getCucumberRegexFromElement(PsiElement element);
  
  protected boolean isCaseSensitive() {
    return true;
  }

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
   * @return true if rename is supported
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

  public String getExpression() {
    return getCucumberRegexFromElement(getElement());
  }
}
