// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesBlockImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinScenarioOutlineImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author yole
 */
public class CucumberCompletionContributor extends CompletionContributor {
  private static final Map<String, String> GROUP_TYPE_MAP = new HashMap<>();
  private static final Map<String, String> INTERPOLATION_PARAMETERS_MAP = new HashMap<>();
  static {
    GROUP_TYPE_MAP.put("(.*)", "<string>");
    GROUP_TYPE_MAP.put("(.+)", "<string>");
    GROUP_TYPE_MAP.put("([^\"]*)", "<string>");
    GROUP_TYPE_MAP.put("([^\"]+)", "<string>");
    GROUP_TYPE_MAP.put("(\\d*)", "<number>");
    GROUP_TYPE_MAP.put("(\\d)", "<number>");
    GROUP_TYPE_MAP.put("(\\d+)", "<number>");
    GROUP_TYPE_MAP.put("(\\.[\\d]+)", "<number>");
    INTERPOLATION_PARAMETERS_MAP.put("#\\{[^\\}]*\\}", "<param>");
  }

  private static final int SCENARIO_KEYWORD_PRIORITY = 70;
  private static final int SCENARIO_OUTLINE_KEYWORD_PRIORITY = 60;
  public static final Pattern POSSIBLE_GROUP_PATTERN = Pattern.compile("\\(([^\\)]*)\\)");
  public static final Pattern QUESTION_MARK_PATTERN = Pattern.compile("([^\\\\])\\?:?");
  public static final Pattern ARGS_INTO_BRACKETS_PATTERN = Pattern.compile("\\(\\?:[^)]*\\)");
  public static final Pattern PARAMETERS_PATTERN = Pattern.compile("<string>|<number>|<param>");
  public static final String INTELLIJ_IDEA_RULEZZZ = "IntellijIdeaRulezzz";

  public CucumberCompletionContributor() {
    final PsiElementPattern.Capture<PsiElement> inScenario = psiElement().inside(psiElement().withElementType(GherkinElementTypes.SCENARIOS));
    final PsiElementPattern.Capture<PsiElement> inStep = psiElement().inside(psiElement().withElementType(GherkinElementTypes.STEP));

    extend(CompletionType.BASIC, psiElement().inFile(psiElement(GherkinFile.class)), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final PsiFile psiFile = parameters.getOriginalFile();
        if (psiFile instanceof GherkinFile) {
          final PsiElement position = parameters.getPosition();

          // if element isn't under feature declaration - suggest feature in autocompletion
          // but don't suggest scenario keywords inside steps
          final PsiElement coveringElement = PsiTreeUtil.getParentOfType(position, GherkinStep.class, GherkinFeature.class, PsiFileSystemItem.class);
          if (coveringElement instanceof PsiFileSystemItem) {
            addFeatureKeywords(result, psiFile);
          } else if (coveringElement instanceof GherkinFeature) {
            addScenarioKeywords(result, psiFile, position);
          }
        }
      }
    });

    extend(CompletionType.BASIC, inScenario.andNot(inStep), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        addStepKeywords(result, parameters.getOriginalFile());
      }
    });

    extend(CompletionType.BASIC, inStep, new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        addStepDefinitions(result, parameters.getOriginalFile());
      }
    });
  }

  private static void addScenarioKeywords(CompletionResultSet result, PsiFile originalFile, PsiElement originalPosition) {
    final Project project = originalFile.getProject();
    final GherkinKeywordTable table = GherkinKeywordTable.getKeywordsTable(originalFile, project);
    final List<String> keywords = new ArrayList<>();

    if (!haveBackground(originalFile)) {
      keywords.addAll(table.getBackgroundKeywords());
    }

    final PsiElement prevElement = getPreviousElement(originalPosition);
    if (prevElement != null && prevElement.getNode().getElementType() == GherkinTokenTypes.SCENARIO_KEYWORD) {
      String scenarioKeyword = (String)table.getScenarioKeywords().toArray()[0];
      result = result.withPrefixMatcher(result.getPrefixMatcher().cloneWithPrefix(scenarioKeyword + " " + result.getPrefixMatcher().getPrefix()));

      boolean haveColon = false;
      final String elementText = originalPosition.getText();
      final int rulezzIndex = elementText.indexOf(INTELLIJ_IDEA_RULEZZZ);
      if (rulezzIndex >= 0) {
        haveColon = elementText.substring(rulezzIndex + INTELLIJ_IDEA_RULEZZZ.length()).trim().startsWith(":");
      }

      addKeywordsToResult(table.getScenarioOutlineKeywords(), result, !haveColon, SCENARIO_OUTLINE_KEYWORD_PRIORITY, !haveColon);
    } else {
      addKeywordsToResult(table.getScenarioKeywords(), result, true, SCENARIO_KEYWORD_PRIORITY, true);
      addKeywordsToResult(table.getScenarioOutlineKeywords(), result, true, SCENARIO_OUTLINE_KEYWORD_PRIORITY, true);
    }

    if (PsiTreeUtil.getParentOfType(originalPosition, GherkinScenarioOutlineImpl.class, GherkinExamplesBlockImpl.class) != null) {
      keywords.addAll(table.getExampleSectionKeywords());
    }
    // add to result
    addKeywordsToResult(keywords, result, true);
  }

  private static PsiElement getPreviousElement(PsiElement element) {
    PsiElement prevElement = element.getPrevSibling();
    if (prevElement instanceof PsiWhiteSpace) {
      prevElement = prevElement.getPrevSibling();
    }
    return prevElement;
  }

  private static void addFeatureKeywords(CompletionResultSet result, PsiFile originalFile) {
    final Project project = originalFile.getProject();
    final GherkinKeywordTable table = GherkinKeywordTable.getKeywordsTable(originalFile, project);

    final Collection<String> keywords = table.getFeaturesSectionKeywords();
    // add to result
    addKeywordsToResult(keywords, result, true);
  }

  private static void addKeywordsToResult(final Collection<String> keywords,
                                          final CompletionResultSet result,
                                          final boolean withColonSuffix) {
    addKeywordsToResult(keywords, result, withColonSuffix, 0, true);
  }

  private static void addKeywordsToResult(final Collection<String> keywords,
                                          final CompletionResultSet result,
                                          final boolean withColonSuffix, int priority, boolean withSpace) {
    for (String keyword : keywords) {
      LookupElement element = createKeywordLookupElement(withColonSuffix ? keyword + ":" : keyword, withSpace);

      result.addElement(PrioritizedLookupElement.withPriority(element, priority));
    }
  }

  private static LookupElement createKeywordLookupElement(final String keyword, boolean withSpace) {
    LookupElement result = LookupElementBuilder.create(keyword);
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      result = ((LookupElementBuilder)result).withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
    }
    if (withSpace) {
      result = TailTypeDecorator.withTail(result, TailType.SPACE);
    }

    return result;
  }

  private static boolean haveBackground(PsiFile originalFile) {
    PsiElement scenarioParent = PsiTreeUtil.getChildOfType(originalFile, GherkinFeature.class);
    if (scenarioParent == null) {
      scenarioParent = originalFile;
    }
    final GherkinScenario[] scenarios = PsiTreeUtil.getChildrenOfType(scenarioParent, GherkinScenario.class);
    if (scenarios != null) {
      for (GherkinScenario scenario : scenarios) {
        if (scenario.isBackground()) {
          return true;
        }
      }
    }
    return false;
  }

  private static void addStepKeywords(CompletionResultSet result, PsiFile file) {
    if (!(file instanceof GherkinFile)) return;
    final GherkinFile gherkinFile = (GherkinFile)file;

    addKeywordsToResult(gherkinFile.getStepKeywords(), result, false);
  }


    private static void addStepDefinitions(CompletionResultSet result, PsiFile file) {
        CompletionResultSet fResult = result.withPrefixMatcher(new CucumberPrefixMatcher(result.getPrefixMatcher().getPrefix()));
        final List<AbstractStepDefinition> definitions = CucumberStepsIndex.getInstance(file.getProject()).getAllStepDefinitions(file);
        for (AbstractStepDefinition definition : definitions) {
            Optional.ofNullable(definition.getCucumberRegex())
                    .map(x -> parseVariationsIntoBrackets(definition.getCucumberRegex()))
                    .ifPresent(variations -> variations.forEach(x -> {
                        // trim regexp line start/end markers
                        x = StringUtil.trimStart(x, "^");
                        x = StringUtil.trimEnd(x, "$");
                        x = StringUtil.replace(x, "\\\"", "\"");
                        for (Map.Entry<String, String> group : GROUP_TYPE_MAP.entrySet()) {
                            x = StringUtil.replace(x, group.getKey(), group.getValue());
                        }

                        for (Map.Entry<String, String> group : INTERPOLATION_PARAMETERS_MAP.entrySet()) {
                            x = x.replaceAll(group.getKey(), group.getValue());
                        }

                        final List<TextRange> ranges = new ArrayList<>();
                        Matcher m = QUESTION_MARK_PATTERN.matcher(x);
                        if (m.find()) {
                            x = m.replaceAll("$1");
                        }

                        m = POSSIBLE_GROUP_PATTERN.matcher(x);
                        while (m.find()) {
                            x = m.replaceAll("$1");
                        }

                        m = PARAMETERS_PATTERN.matcher(x);
                        while (m.find()) {
                            ranges.add(new TextRange(m.start(), m.end()));
                        }

                        final PsiElement element = definition.getElement();
                        final LookupElementBuilder lookup = element != null
                                ? LookupElementBuilder.create(element, x).bold()
                                : LookupElementBuilder.create(x);
                        fResult.addElement(lookup.withInsertHandler(new StepInsertHandler(ranges)));
                    }));
        }
    }

    /**
     * For example: step = "^(?:user|he|) do something$" when result will be:
     * {
     *     "^user do something$"
     *     "^he do something$"
     *     "^ do something$"
     * }
     * @param cucumberRegex
     * @return
     */
    private static List<String> parseVariationsIntoBrackets(String cucumberRegex) {
        List<Pair<String, List<String>>> insertions = new ArrayList<>();
        Matcher m = ARGS_INTO_BRACKETS_PATTERN.matcher(cucumberRegex);
        String mainSample = cucumberRegex;
        while (m.find()) {
            String key = "@key=" + Guid.GUID.newGuid().toGuidString() + "@";
            mainSample = mainSample.replace(m.group(), key);
            insertions.add(Pair.create(key, Arrays.asList(cucumberRegex.substring(m.start() + 3, m.end() - 1).split("\\|"))));
        }

        // Example: @sampleCounts = [2, 3] when @indexes = [1, 1], [1, 2], [1, 3], [2, 1], [2, 2], [2, 3]
        int[] sampleCounts = new int[insertions.size()];
        for (int i = 0; i < insertions.size(); i++)
            sampleCounts[i] = insertions.get(i).getSecond().size();
        List<int[]> indexes = new ArrayList<>();
        int[] pArray = new int[sampleCounts.length];
        for (int i = 0; i < sampleCounts.length; i++)
            pArray[i] = 1;
        indexes.add(pArray);
        while (!Arrays.equals(sampleCounts, pArray)) {
            int[] t = Arrays.copyOf(pArray, pArray.length);
            for (int i = sampleCounts.length - 1; i >= 0; i--) {
                if (t[i] != sampleCounts[i]) {
                    t[i]++;
                    break;
                } else t[i] = 1;
            }
            indexes.add(pArray = t);
        }

        final String toSampling = mainSample;
        List<String> result = new ArrayList<>();
        for (int[] combination : indexes) {
            String stepVar = toSampling;
            for (int j = 0; j < combination.length; j++) {
                Pair<String, List<String>> insertion = insertions.get(j);
                stepVar = stepVar.replace(insertion.getFirst(), insertion.getSecond().get(combination[j] - 1));
            }
            result.add(stepVar);
        }
        return result;
    }

  private static class StepInsertHandler implements InsertHandler<LookupElement> {
    private final List<TextRange> ranges;

    private StepInsertHandler(List<TextRange> ranges) {
      this.ranges = ranges;
    }

    @Override
    public void handleInsert(@NotNull final InsertionContext context, @NotNull LookupElement item) {
      if (!ranges.isEmpty()) {
        final PsiElement element = context.getFile().findElementAt(context.getStartOffset());
        final GherkinStep step = PsiTreeUtil.getParentOfType(element, GherkinStep.class);
        if (step != null) {
          final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(step);
          int off = context.getStartOffset() - step.getTextRange().getStartOffset();
          final String stepText = step.getText();
          for (TextRange groupRange : ranges) {
            final TextRange shiftedRange = groupRange.shiftRight(off);
            final String matchedText = shiftedRange.substring(stepText);
            builder.replaceRange(shiftedRange, matchedText);
          }
          builder.run(context.getEditor(), false);
        }
      }
    }
  }
}
