// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  public AbstractStepDefinition(@NotNull PsiElement element) {
    myElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
  }

  public abstract List<String> getVariableNames();

  /// @return true if a concrete step named `stepName` is defined by this step definition.
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

  public @Nullable PsiElement getElement() {
    return myElementPointer.getElement();
  }

  /// Returns a [Pattern] pattern for the step (or null if the regex is malformed).
  ///
  /// Depends on [#getCucumberRegex()].
  public @Nullable Pattern getPattern() {
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
    catch (PatternSyntaxException ignored) {
      return null; // Bad regex?
    }
  }

  /// Returns the step definition string as a regex.
  ///
  /// If the step definition string is a regex, it is returned as-is.
  /// If the step definition is a cukex, it will be converted to a regex.
  public @Nullable String getCucumberRegex() {
    return getExpression();
  }

  @Contract("null -> null")
  protected abstract @Nullable String getCucumberRegexFromElement(PsiElement element);

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

  /// Sets the new value for this step definition (either a regex or a cukex).
  ///
  /// What the value exactly is depends on the particular Cucumber implementation in some programming language.
  /// For example, it could be a string inside the annotation `@When` or a method name.
  public void setValue(@NotNull String newValue) { }

  /// @return True if this step definition supports some certain `step` (e.g., some step definitions do not support some keywords).
  public boolean supportsStep(@NotNull PsiElement step) {
    return true;
  }

  /// If `newName` is not null, it returns true if this step definition can be renamed to this specific new name.
  ///
  /// If `newName` is null, it returns true if this step definition can be renamed at all.
  public boolean supportsRename(@Nullable String newName) {
    return true;
  }

  /// Finds all steps that refer to this definition in some [SearchScope] (kind of like "find usages").
  public @NotNull Collection<GherkinStep> findSteps(@NotNull SearchScope searchScope) {
    final String regex = getCucumberRegex();
    final PsiElement element = getElement();
    if (regex == null || element == null) {
      return Collections.emptyList();
    }

    final CollectProcessor<PsiReference> consumer = new CollectProcessor<>();
    CucumberUtil.findGherkinReferencesToElement(element, regex, consumer, searchScope);

    // We use a hash set to get rid of duplicates
    final Collection<GherkinStep> results = new HashSet<>(consumer.getResults().size());
    for (final PsiReference reference : consumer.getResults()) {
      if (reference.getElement() instanceof GherkinStep gherkinStep) {
        results.add(gherkinStep);
      }
    }
    return results;
  }

  /// Returns either a regex or cukex associated with this step.
  ///
  /// If the step is defined with a cukex, it returns this cukex.
  /// If the step is defined with a regex, it returns this regex.
  public @Nullable String getExpression() {
    return getCucumberRegexFromElement(getElement());
  }
}
