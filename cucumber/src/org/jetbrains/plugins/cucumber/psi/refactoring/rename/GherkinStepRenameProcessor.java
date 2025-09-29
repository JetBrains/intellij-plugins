// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.Stack;
import org.intellij.lang.regexp.RegExpCapability;
import org.intellij.lang.regexp.RegExpLexer;
import org.intellij.lang.regexp.RegExpTT;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.MapParameterTypeManager;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NotNullByDefault
public final class GherkinStepRenameProcessor extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(PsiElement element) {
    boolean isGherkinStep = element instanceof GherkinStep || PsiTreeUtil.getParentOfType(element, GherkinStep.class) != null;
    return isGherkinStep;
  }

  /**
   * Wraps all special symbols of regexp with group and cut static text.
   *
   * @param source regex to work with
   * @return List of strings. The first one is prepared regex, then static elements of the regex
   */
  public static List<String> prepareRegexAndGetStaticTexts(String source) {
    final ArrayList<String> result = new ArrayList<>();
    final StringBuilder preparedRegexp = new StringBuilder();

    final RegExpLexer lexer = new RegExpLexer(EnumSet.noneOf(RegExpCapability.class));
    lexer.start(source);
    IElementType previous = null;
    final TokenSet toSkip = TokenSet.create(RegExpTT.CHARACTER, RegExpTT.CARET, RegExpTT.DOLLAR, RegExpTT.REDUNDANT_ESCAPE);

    StringBuilder currentStaticText = new StringBuilder();

    boolean insideAddedGroup = false;
    final Stack<IElementType> elementsWaitingToClose = new Stack<>();

    while (lexer.getTokenType() != null) {
      if (!toSkip.contains(lexer.getTokenType())) {
        if (!insideAddedGroup) {
          insideAddedGroup = true;
          preparedRegexp.append('(');

          result.add(currentStaticText.toString());
          currentStaticText = new StringBuilder();
        }
        if (lexer.getTokenType() == RegExpTT.GROUP_BEGIN || lexer.getTokenType() == RegExpTT.NON_CAPT_GROUP) {
          elementsWaitingToClose.push(RegExpTT.GROUP_END);
        }
        else if (lexer.getTokenType() == RegExpTT.CLASS_BEGIN) {
          elementsWaitingToClose.push(RegExpTT.CLASS_END);
        }
        else if (!elementsWaitingToClose.isEmpty() && lexer.getTokenType() == elementsWaitingToClose.peek()) {
          elementsWaitingToClose.pop();
        }
      }
      else {
        if (elementsWaitingToClose.isEmpty()) {
          if (previous != null && previous != RegExpTT.CHARACTER && insideAddedGroup) {
            insideAddedGroup = false;
            preparedRegexp.append(')');
          }

          if (lexer.getTokenType() == RegExpTT.CHARACTER) {
            currentStaticText.append(lexer.getTokenText());
          }
        }
      }
      preparedRegexp.append(lexer.getTokenText());
      if (lexer.getTokenType() == RegExpTT.GROUP_BEGIN) {
        // Making all group in the regex non-capturing
        preparedRegexp.append("?:");
      }

      previous = lexer.getTokenType();
      lexer.advance();
    }

    if (insideAddedGroup) {
      preparedRegexp.append(')');
    }
    result.add(currentStaticText.toString());
    result.addFirst(preparedRegexp.toString());
    return result;
  }

  public static String prepareRegexFromCukex(String cukex) {
    String preparedRegex = CucumberUtil.buildRegexpFromCucumberExpression(cukex, MapParameterTypeManager.DEFAULT);
    return preparedRegex.substring(1).substring(0, preparedRegex.length() - 2);
  }

  /// Wraps all special cukex special symbols with regex groups and cuts out static text.
  ///
  /// @return List of strings. Each string is a static part of the cukex.
  public static List<String> getStaticTextsFromCukex(String cukex) {
    List<TextRange> ranges = CucumberUtil.getCukexRanges(cukex);
    return CucumberUtil.textRangesOutsideToSubstrings(cukex, ranges);
  }

  /// Returns a _concrete_ new name of a specific renamed step usage.
  public static String getNewStepName(String oldStepName, Pattern oldStepDefPattern, List<String> newStaticTexts) {
    newStaticTexts = new ArrayList<>(newStaticTexts);
    final Matcher matcher = oldStepDefPattern.matcher(oldStepName);
    if (matcher.find()) {
      // List to hold the concrete values of parameters, optional texts, and alternative texts.
      final List<@Nullable String> concreteValues = new ArrayList<>();
      for (int i = 0; i < matcher.groupCount(); i++) {
        final String concreteValue = matcher.group(i + 1);
        concreteValues.add(concreteValue);
      }

      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < concreteValues.size(); i++) {
        String staticText = newStaticTexts.removeFirst();
        sb.append(staticText);
        String concreteValue = concreteValues.get(i);
        if (concreteValue != null) {
          sb.append(concreteValue);
        }
      }

      // Append the remaining static text, if any.
      for (String staticText : newStaticTexts) {
        sb.append(staticText);
      }

      return sb.toString();
    }
    else {
      return oldStepName;
    }
  }

  @Override
  public void renameElement(PsiElement element, String newName, UsageInfo[] usages, @Nullable RefactoringElementListener listener)
    throws IncorrectOperationException {
    final CucumberStepReference reference = CucumberUtil.getCucumberStepReference(element);
    if (reference != null) {
      List<AbstractStepDefinition> stepDefinitions = reference.resolveToDefinitions().stream().toList();
      if (stepDefinitions.size() != 1) return; // TODO: signal this to the user in some way
      final AbstractStepDefinition stepDefinition = stepDefinitions.getFirst();
      if (stepDefinition == null) throw new IllegalStateException("step definition must not be null");
      final PsiElement elementToRename = stepDefinition.getElement();
      final String regexp = stepDefinition.getCucumberRegex();
      final String expression = stepDefinition.getExpression();
      if (expression != null && regexp != null) {
        final boolean expressionIsRegex = expression.equals(regexp);
        final Pattern oldStepDefPattern = Pattern.compile(
          (expressionIsRegex ? prepareRegexAndGetStaticTexts(regexp).getFirst() : prepareRegexFromCukex(expression))
        );
        final List<String> newStaticTexts = expressionIsRegex ? prepareRegexAndGetStaticTexts(newName) : getStaticTextsFromCukex(newName);
        if (expressionIsRegex) {
          newStaticTexts.removeFirst();
        }

        for (UsageInfo usage : usages) {
          final PsiElement possibleStep = usage.getElement();
          if (possibleStep instanceof GherkinStep gherkinStep) {
            final String oldStepName = gherkinStep.getName();
            final String newStepName = getNewStepName(oldStepName, oldStepDefPattern, newStaticTexts);
            gherkinStep.setName(newStepName);
          }
        }

        final String prefix = expression.startsWith("^") ? "^" : "";
        final String suffix = expression.endsWith("$") ? "$" : "";
        stepDefinition.setValue(prefix + newName + suffix);

        if (listener != null && elementToRename != null) {
          listener.elementRenamed(elementToRename);
        }
      }
    }
  }

  @Override
  public Collection<PsiReference> findReferences(PsiElement element, SearchScope searchScope, boolean searchInCommentsAndStrings) {
    if (!(element instanceof GherkinStep)) throw new IllegalStateException("element must be a GherkinStep, but is: " + element);
    final CucumberStepReference cucumberStepReference = CucumberUtil.getCucumberStepReference(element);
    if (cucumberStepReference != null) {
      final AbstractStepDefinition abstractStepDef = cucumberStepReference.resolveToDefinition();
      if (abstractStepDef != null) {
        final PsiElement stepDefElement = abstractStepDef.getElement();
        if (stepDefElement != null) {
          final String cucumberRegex = abstractStepDef.getCucumberRegex();
          if (cucumberRegex != null) {
            final List<PsiReference> result = new ArrayList<>();
            CucumberUtil.findGherkinReferencesToElement(stepDefElement, cucumberRegex, reference -> result.add(reference), searchScope);
            return result;
          }
        }
      }
    }

    return List.of();
  }
}
