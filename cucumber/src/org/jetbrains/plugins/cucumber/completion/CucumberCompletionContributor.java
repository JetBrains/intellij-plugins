package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesBlockImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinScenarioOutlineImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author yole
 */
public class CucumberCompletionContributor extends CompletionContributor {
  private static final Set<String> INGORED_MATCHER_TEXT_SET =
    new HashSet<String>(Arrays.asList("(.*)", "(.+)",
                                      "(\\d*)", "(\\d+)",
                                      "([^\"]*)", "([^\"]+)"
    ));

  public CucumberCompletionContributor() {
    final PsiElementPattern.Capture<PsiElement> inScenario = psiElement().inside(psiElement().withElementType(GherkinElementTypes.SCENARIOS));
    final PsiElementPattern.Capture<PsiElement> inStep = psiElement().inside(psiElement().withElementType(GherkinElementTypes.STEP));

    extend(CompletionType.BASIC, psiElement().inFile(psiElement(GherkinFile.class)), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final PsiFile psiFile = parameters.getOriginalFile();
        if (psiFile instanceof GherkinFile) {
          final PsiElement position = parameters.getPosition();

          // if element isn't under feature declaration - suggest feature in autocompletion
          // but don't suggest scenario keywords inside steps
          final PsiElement coveringElement = PsiTreeUtil.getParentOfType(position, GherkinStep.class, GherkinFeature.class, PsiFileSystemItem.class);
          if (coveringElement instanceof PsiFileSystemItem) {
            addFeatureKeywords(result, psiFile, position);
          } else if (coveringElement instanceof GherkinFeature) {
            addScenarioKeywords(result, psiFile, position);
          }
        }
      }
    });

    extend(CompletionType.BASIC, inScenario.andNot(inStep), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        addStepKeywords(result, parameters.getOriginalFile());
      }
    });

    extend(CompletionType.BASIC, inStep, new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        addStepDefinitions(result, parameters.getOriginalFile());
      }
    });
  }

  private static void addScenarioKeywords(CompletionResultSet result, PsiFile originalFile, PsiElement originalPosition) {
    final Project project = originalFile.getProject();
    final GherkinKeywordTable table = GherkinKeywordTable.getKeywordsTable(originalFile, project);
    final List<String> keywords = new ArrayList<String>();

    if (!haveBackground(originalFile)) {
      keywords.addAll(table.getBackgroundKeywords());
    }

    keywords.addAll(table.getScenarioLikeKeywords());

    if (PsiTreeUtil.getParentOfType(originalPosition, GherkinScenarioOutlineImpl.class, GherkinExamplesBlockImpl.class) != null) {
      keywords.addAll(table.getExampleSectionKeywords());
    }
    // add to result
    addKeywordsToResult(keywords, result, true);
  }

  private static void addFeatureKeywords(CompletionResultSet result, PsiFile originalFile, PsiElement originalPosition) {
    final Project project = originalFile.getProject();
    final GherkinKeywordTable table = GherkinKeywordTable.getKeywordsTable(originalFile, project);

    final Collection<String> keywords = table.getFeaturesSectionKeywords();
    // add to result
    addKeywordsToResult(keywords, result, true);
  }

  private static void addKeywordsToResult(final Collection<String> keywords,
                                          final CompletionResultSet result,
                                          final boolean withColonSuffix) {
    for (String keyword : keywords) {
      result.addElement(createKeywordLookupElement(withColonSuffix ? keyword + ":" : keyword));
    }
  }

  private static LookupElement createKeywordLookupElement(final String keyword) {
    return TailTypeDecorator.withTail(LookupElementBuilder.create(keyword), TailType.SPACE);
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
    result = result.withPrefixMatcher(new PlainPrefixMatcher(result.getPrefixMatcher().getPrefix()));
    final List<AbstractStepDefinition> definitions = CucumberStepsIndex.getInstance(file.getProject()).getAllStepDefinitions(file);
    for (AbstractStepDefinition definition : definitions) {
      String text = definition.getElementText();
      // trim regexp line start/end markers
      if (text.startsWith("^")) {
        text = text.substring(1);
      }
      if (text.endsWith("$")) {
        text = text.substring(0, text.length() - 1);
      }
      result.addElement(LookupElementBuilder.create(text).withInsertHandler(new StepInsertHandler()));
    }
  }

  private static class StepInsertHandler implements InsertHandler<LookupElement> {
    public void handleInsert(final InsertionContext context, LookupElement item) {
      final Language rx = Language.findLanguageByID("RegExp");
      if (rx != null) {
        final PsiFile rxFile = PsiFileFactory.getInstance(context.getProject()).createFileFromText("a", rx, item.getLookupString());
        final List<TextRange> groupRanges = new ArrayList<TextRange>();
        rxFile.acceptChildren(new PsiRecursiveElementVisitor() {
          @Override
          public void visitElement(PsiElement element) {
            super.visitElement(element);
            if (element.toString().startsWith("RegExpGroup")) {
              groupRanges.add(element.getTextRange());
            }
          }
        });
        if (!groupRanges.isEmpty()) {
          final PsiElement element = context.getFile().findElementAt(context.getStartOffset());
          final GherkinStep step = PsiTreeUtil.getParentOfType(element, GherkinStep.class);
          if (step != null) {
            final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(step);
            int off = context.getStartOffset() - step.getTextRange().getStartOffset();
            final String stepText = step.getText();
            for (TextRange groupRange : groupRanges) {
              final TextRange shiftedRange = groupRange.shiftRight(off);
              final String matchedText = shiftedRange.substring(stepText);
              builder.replaceRange(shiftedRange, INGORED_MATCHER_TEXT_SET.contains(matchedText) ? "" : matchedText);
            }
            builder.run();
          }
        }
      }
    }
  }

}
