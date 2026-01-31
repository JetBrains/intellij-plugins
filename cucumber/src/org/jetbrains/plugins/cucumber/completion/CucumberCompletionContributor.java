// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.TailTypes;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
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
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;
import org.jetbrains.plugins.cucumber.psi.GherkinRule;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;
import org.jetbrains.plugins.cucumber.psi.GherkinUtil;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesBlockImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinScenarioOutlineImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;


@NotNullByDefault
public final class CucumberCompletionContributor extends CompletionContributor {
  private static final Map<String, String> GROUP_TYPE_MAP = new HashMap<>();
  private static final Map<String, String> PARAMETERS_MAP = new HashMap<>();

  static {
    GROUP_TYPE_MAP.put("(.*)", "<any>");
    GROUP_TYPE_MAP.put("[^\\s]+", "<word>");
    GROUP_TYPE_MAP.put("(.+)", "<string>");
    GROUP_TYPE_MAP.put("([^\"]*)", "<string>");
    GROUP_TYPE_MAP.put("([^\"]+)", "<string>");
    GROUP_TYPE_MAP.put("(\\d*)", "<number>");
    GROUP_TYPE_MAP.put("(\\d)", "<number>");
    GROUP_TYPE_MAP.put("(-?\\d+)", "<number>");
    GROUP_TYPE_MAP.put("(\\d+)", "<number>");
    GROUP_TYPE_MAP.put("(-?\\d*[.,]?\\d+)", "<float>");
    GROUP_TYPE_MAP.put("(\\.[\\d]+)", "<float>");
    GROUP_TYPE_MAP.put("(\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)')", "<string>");
    PARAMETERS_MAP.put("\\([^|]*\\|[^|]*(?:\\|[^|]*)*\\)", "<param>");
    PARAMETERS_MAP.put("#\\{[^\\}]*\\}", "<param>");
  }

  private static final int SCENARIO_KEYWORD_PRIORITY = 70;
  private static final int SCENARIO_OUTLINE_KEYWORD_PRIORITY = 60;
  public static final Pattern POSSIBLE_GROUP_PATTERN = Pattern.compile("\\(([^)]*)\\)");
  public static final Pattern QUESTION_MARK_PATTERN = Pattern.compile("([^\\\\])\\?:?");
  public static final Pattern ARGS_INTO_BRACKETS_PATTERN = Pattern.compile("\\((?:\\?[!:])?([^)]*\\|[^)]*)\\)");
  public static final Pattern PARAMETERS_PATTERN = Pattern.compile("<string>|<number>|<param>|<word>|<float>|<any>|\\{[^}]+}");
  public static final String INTELLIJ_IDEA_RULEZZZ = "IntellijIdeaRulezzz";

  public CucumberCompletionContributor() {
    PsiElementPattern.Capture<PsiElement> inTable = psiElement().inside(psiElement().withElementType(GherkinElementTypes.TABLE));
    PsiElementPattern.Capture<PsiElement> inScenario =
      psiElement().inside(psiElement().withElementType(GherkinElementTypes.SCENARIOS)).andNot(inTable);
    PsiElementPattern.Capture<PsiElement> inStep =
      psiElement().inside(psiElement().withElementType(GherkinElementTypes.STEP)).andNot(inTable);

    extend(CompletionType.BASIC, psiElement().inFile(psiElement(GherkinFile.class)).andNot(inTable), new CompletionProvider<>() {
      @Override
      protected void addCompletions(CompletionParameters parameters,
                                    ProcessingContext context,
                                    CompletionResultSet result) {
        final PsiFile psiFile = parameters.getOriginalFile();
        if (psiFile instanceof GherkinFile file) {
          Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
          boolean gherkin6Enabled = module != null && CucumberStepHelper.isGherkin6Supported(module);
          GherkinKeywordProvider keywordProvider = JsonGherkinKeywordProvider.getKeywordProvider(gherkin6Enabled);
          final String language = GherkinUtil.getFeatureLanguage(file);
          GherkinKeywordTable gherkinKeywordTable = keywordProvider.getKeywordsTable(language);

          final PsiElement position = parameters.getPosition();

          // if element isn't under feature declaration - suggest feature in autocompletion
          // but don't suggest scenario keywords inside steps
          final PsiElement coveringElement =
            PsiTreeUtil.getParentOfType(position, GherkinStep.class, GherkinFeature.class, PsiFileSystemItem.class);
          if (coveringElement instanceof PsiFileSystemItem) {
            addFeatureKeywords(result, gherkinKeywordTable);
          }
          else if (coveringElement instanceof GherkinFeature) {
            if (gherkin6Enabled) {
              addRuleKeyword(result, gherkinKeywordTable);
            }
            addScenarioKeywords(result, psiFile, position, gherkinKeywordTable);
          }
          else if (coveringElement instanceof GherkinRule) {
            addScenarioKeywords(result, psiFile, position, gherkinKeywordTable);
          }
        }
      }
    });

    extend(CompletionType.BASIC, inScenario.andNot(inStep), new CompletionProvider<>() {
      @Override
      protected void addCompletions(CompletionParameters parameters,
                                    ProcessingContext context,
                                    CompletionResultSet result) {
        addStepKeywords(result, parameters.getOriginalFile());
      }
    });

    extend(CompletionType.BASIC, inStep, new CompletionProvider<>() {
      @Override
      protected void addCompletions(CompletionParameters parameters,
                                    ProcessingContext context,
                                    CompletionResultSet result) {
        addStepDefinitions(result, parameters.getOriginalFile());
      }
    });
  }

  private static void addRuleKeyword(CompletionResultSet result,
                                     GherkinKeywordTable gherkinKeywordTable) {
    addKeywordsToResult(gherkinKeywordTable.getRuleKeywords(), result, true);
  }

  private static void addScenarioKeywords(CompletionResultSet result, PsiFile originalFile,
                                          PsiElement originalPosition, GherkinKeywordTable table) {
    final List<String> keywords = new ArrayList<>();

    if (!haveBackground(originalFile)) {
      keywords.addAll(table.getBackgroundKeywords());
    }

    final PsiElement prevElement = getPreviousElement(originalPosition);
    if (prevElement != null && prevElement.getNode().getElementType() == GherkinTokenTypes.SCENARIO_KEYWORD) {
      for (String scenarioKeyword : table.getScenarioKeywords()) {
        if (prevElement.getText().startsWith(scenarioKeyword)) {
          result = result
            .withPrefixMatcher(result.getPrefixMatcher().cloneWithPrefix(scenarioKeyword + " " + result.getPrefixMatcher().getPrefix()));
          break;
        }
      }

      boolean haveColon = false;
      final String elementText = originalPosition.getText();
      final int rulezzIndex = elementText.indexOf(INTELLIJ_IDEA_RULEZZZ);
      if (rulezzIndex >= 0) {
        haveColon = elementText.substring(rulezzIndex + INTELLIJ_IDEA_RULEZZZ.length()).trim().startsWith(":");
      }

      addKeywordsToResult(table.getScenarioOutlineKeywords(), result, !haveColon, SCENARIO_OUTLINE_KEYWORD_PRIORITY, !haveColon);
    }
    else {
      addKeywordsToResult(table.getScenarioKeywords(), result, true, SCENARIO_KEYWORD_PRIORITY, true);
      addKeywordsToResult(table.getScenarioOutlineKeywords(), result, true, SCENARIO_OUTLINE_KEYWORD_PRIORITY, true);
    }

    if (PsiTreeUtil.getParentOfType(originalPosition, GherkinScenarioOutlineImpl.class, GherkinExamplesBlockImpl.class) != null) {
      keywords.addAll(table.getExampleSectionKeywords());
    }
    // add to result
    addKeywordsToResult(keywords, result, true);
  }

  private static @Nullable PsiElement getPreviousElement(PsiElement element) {
    PsiElement prevElement = element.getPrevSibling();
    if (prevElement instanceof PsiWhiteSpace) {
      prevElement = prevElement.getPrevSibling();
    }
    return prevElement;
  }

  private static void addFeatureKeywords(CompletionResultSet result, GherkinKeywordTable gherkinKeywordTable) {
    final Collection<String> keywords = gherkinKeywordTable.getFeaturesSectionKeywords();
    // add to result
    addKeywordsToResult(keywords, result, true);
  }

  private static void addKeywordsToResult(Collection<String> keywords,
                                          CompletionResultSet result,
                                          boolean withColonSuffix) {
    addKeywordsToResult(keywords, result, withColonSuffix, 0, true);
  }

  private static void addKeywordsToResult(Collection<String> keywords,
                                          CompletionResultSet result,
                                          boolean withColonSuffix, int priority, boolean withSpace) {
    for (String keyword : keywords) {
      LookupElement element = createKeywordLookupElement(withColonSuffix ? keyword + ":" : keyword, withSpace);

      result.addElement(PrioritizedLookupElement.withPriority(element, priority));
    }
  }

  private static LookupElement createKeywordLookupElement(String keyword, boolean withSpace) {
    LookupElement result = LookupElementBuilder.create(keyword);
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      result = ((LookupElementBuilder)result).withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
    }
    if (withSpace) {
      result = TailTypeDecorator.withTail(result, TailTypes.spaceType());
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
    if (!(file instanceof GherkinFile gherkinFile)) return;

    addKeywordsToResult(gherkinFile.getStepKeywords(), result, false);
  }


  private static void addStepDefinitions(CompletionResultSet result, PsiFile file) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) {
      return;
    }
    final Collection<AbstractStepDefinition> stepDefinitions = CucumberStepHelper.loadStepsFor(file, module);
    for (AbstractStepDefinition stepDefinition : stepDefinitions) {
      String expression = stepDefinition.getExpression();
      if (expression == null) {
        continue;
      }
      for (String stepCompletion : parseVariationsIntoBrackets(expression)) {
        // trim regexp line start/end markers
        stepCompletion = StringUtil.trimStart(stepCompletion, "^");
        stepCompletion = StringUtil.trimEnd(stepCompletion, "$");
        stepCompletion = StringUtil.replace(stepCompletion, "\\\"", "\"");
        for (Map.Entry<String, String> group : GROUP_TYPE_MAP.entrySet()) {
          stepCompletion = StringUtil.replace(stepCompletion, group.getKey(), group.getValue());
        }

        for (Map.Entry<String, String> group : PARAMETERS_MAP.entrySet()) {
          stepCompletion = stepCompletion.replaceAll(group.getKey(), group.getValue());
        }

        final List<TextRange> ranges = new ArrayList<>();
        Matcher m = QUESTION_MARK_PATTERN.matcher(stepCompletion);
        if (m.find()) {
          stepCompletion = m.replaceAll("$1");
        }

        m = POSSIBLE_GROUP_PATTERN.matcher(stepCompletion);
        while (m.find()) {
          stepCompletion = m.replaceAll("$1");
        }

        m = PARAMETERS_PATTERN.matcher(stepCompletion);
        while (m.find()) {
          ranges.add(new TextRange(m.start(), m.end()));
        }

        final PsiElement element = stepDefinition.getNavigationElement();
        final LookupElementBuilder lookup = element != null
                                            ? LookupElementBuilder.create(element, stepCompletion).bold()
                                            : LookupElementBuilder.create(stepCompletion);
        result.addElement(lookup.withInsertHandler(new StepInsertHandler(ranges)));
      }
    }
  }

  /**
   * For example: step = "^(?:user|he|) do something$" when result will be:
   * {
   * "^user do something$"
   * "^he do something$"
   * "^ do something$"
   * }
   *
   * @return set of steps accepted by step definition regexp
   */
  private static Set<String> parseVariationsIntoBrackets(String cucumberRegex) {
    List<Pair<String, List<String>>> insertions = new ArrayList<>();
    Matcher m = ARGS_INTO_BRACKETS_PATTERN.matcher(cucumberRegex);
    String mainSample = cucumberRegex;
    int k = 0;
    while (m.find()) {
      String values = cucumberRegex.substring(m.start(1), m.end(1));
      if (values.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '|')) {
        String key = "@key=" + k++ + "@";
        mainSample = mainSample.replace(m.group(), key);
        insertions.add(Pair.create(key, Arrays.asList(values.split("\\|"))));
      }
    }

    // Example: @sampleCounts = [2, 3] when @combinations = [1, 1], [1, 2], [1, 3], [2, 1], [2, 2], [2, 3]
    int[] sampleCounts = new int[insertions.size()];
    int combinationCount = 1;
    for (int i = 0; i < insertions.size(); i++) {
      sampleCounts[i] = insertions.get(i).getSecond().size();
      combinationCount *= sampleCounts[i];
    }
    List<int[]> combinations = new ArrayList<>();
    int[] combination = new int[sampleCounts.length];

    for (int i = 0; i < sampleCounts.length; i++) {
      combination[i] = 1;
    }
    combinations.add(combination);
    for (int j = 0; j < combinationCount; j++) {
      int[] currentCombination = combination.clone();
      for (int i = sampleCounts.length - 1; i >= 0; i--) {
        if (currentCombination[i] != sampleCounts[i]) {
          currentCombination[i]++;
          break;
        }
        else {
          currentCombination[i] = 1;
        }
      }
      combinations.add(combination = currentCombination);
    }

    Set<String> result = new HashSet<>();
    for (int[] c : combinations) {
      String stepVar = mainSample;
      for (int i = 0; i < c.length; i++) {
        Pair<String, List<String>> insertion = insertions.get(i);
        String key = insertion.getFirst();
        stepVar = stepVar.replace(key, insertion.getSecond().get(c[i] - 1));
      }
      result.add(stepVar);
    }
    return result;
  }

  private static final class StepInsertHandler implements InsertHandler<LookupElement> {
    private final List<TextRange> ranges;

    private StepInsertHandler(List<TextRange> ranges) {
      this.ranges = ranges;
    }

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
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
