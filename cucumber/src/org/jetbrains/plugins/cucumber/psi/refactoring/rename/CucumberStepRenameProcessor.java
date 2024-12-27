// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CucumberStepRenameProcessor extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return element instanceof GherkinStep || PsiTreeUtil.getParentOfType(element, GherkinStep.class) != null;
  }

  public static CucumberStepReference getCucumberStepReference(PsiElement element) {
    for (PsiReference ref : element.getReferences()) {
      if (ref instanceof CucumberStepReference) {
        return (CucumberStepReference)ref;
      }
    }
    return null;
  }

  /**
   * Wraps all special symbols of regexp with group and cut static text.
   * @param source regex to work with
   * @return List of strings. The first one is prepared regex, then static elements of the regex
   */
  public static @NotNull List<String> prepareRegexAndGetStaticTexts(final @NotNull String source) {
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
        //Making all group in the regex non capturing
        preparedRegexp.append("?:");
      }

      previous = lexer.getTokenType();
      lexer.advance();
    }

    if (insideAddedGroup) {
      preparedRegexp.append(')');
    }
    result.add(currentStaticText.toString());
    result.add(0, preparedRegexp.toString());
    return result;
  }

  private static @NotNull String getNewStepName(@NotNull String oldStepName,
                                                @NotNull Pattern oldStepDefPattern,
                                                @NotNull List<String> newStaticTexts) {
    final Matcher matcher = oldStepDefPattern.matcher(oldStepName);
    if (matcher.find()) {
      final ArrayList<String> values = new ArrayList<>();
      for (int i = 0; i < matcher.groupCount(); i++) {
        values.add(matcher.group(i + 1));
      }

      final StringBuilder result = new StringBuilder();
      for (int i = 0; i < values.size(); i++) {
        result.append(newStaticTexts.get(i + 1));
        result.append(values.get(i));
      }

      result.append(newStaticTexts.get(newStaticTexts.size() - 1));
      return result.toString();
    } else  {
      return oldStepName;
    }
  }

  @Override
  public void renameElement(@NotNull PsiElement element, @NotNull String newName, UsageInfo @NotNull [] usages, @Nullable RefactoringElementListener listener)
    throws IncorrectOperationException {

    final CucumberStepReference reference = getCucumberStepReference(element);
    if (reference != null) {
      final AbstractStepDefinition stepDefinition = reference.resolveToDefinition();
      if (stepDefinition != null) {
        final PsiElement elementToRename = stepDefinition.getElement();

        final List<String> newStaticTexts = prepareRegexAndGetStaticTexts(newName);
        final String oldStepDefPatternText = stepDefinition.getCucumberRegex();
        if (oldStepDefPatternText != null) {
          final Pattern oldStepDefPattern = Pattern.compile(prepareRegexAndGetStaticTexts(oldStepDefPatternText).get(0));

          for (UsageInfo usage : usages) {
            final PsiElement possibleStep = usage.getElement();
            if (possibleStep instanceof GherkinStep) {
              final String oldStepName = ((GherkinStep)possibleStep).getName();
              final String newStepName = getNewStepName(oldStepName, oldStepDefPattern, newStaticTexts);
              ((GherkinStep)possibleStep).setName(newStepName);
            }
          }

          final String prefix = oldStepDefPatternText.startsWith("^") ? "^" : "";
          final String suffix = oldStepDefPatternText.endsWith("$") ? "$" : "";
          stepDefinition.setCucumberRegex(prefix + newName + suffix);

          if (listener != null && elementToRename != null) {
            listener.elementRenamed(elementToRename);
          }
        }
      }
    }
  }

  @Override
  public @NotNull Collection<PsiReference> findReferences(@NotNull PsiElement element,
                                                          @NotNull SearchScope searchScope,
                                                          boolean searchInCommentsAndStrings) {
    return Arrays.asList(element.getReferences());
  }
}
