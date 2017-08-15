package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.PsiSearchHelper.SERVICE;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.steps.search.CucumberStepSearchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CucumberUtil {
  @NonNls public static final String STEP_DEFINITIONS_DIR_NAME = "step_definitions";

  public static final String[][] ARR = {
    {"\\\\", "\\\\\\\\"},
    {"\\|", "\\\\|"},
    {"\\$", "\\\\\\$"},
    {"\\^", "\\\\^"},
    {"\\+", "\\+"},
    {"\\-", "\\\\-"},
    {"\\#", "\\\\#"},
    {"\\?", "\\\\?"},
    {"\\*", "\\\\*"},
    {"\\/", "\\\\/"},
    {"\\{", "\\\\{"},
    {"\\}", "\\\\}"},
    {"\\[", "\\\\["},
    {"\\]", "\\\\]"},
    {"\\(", "\\\\("},
    {"\\)", "\\\\)"},
    {"\\+", "\\\\+"},
    {"\"([^\\\\\"]*)\"", "\"([^\"]*)\""},
    {"(?<=^|[ .,])\\d+[ ]", "(\\\\d+) "},
    {"(?<=^|[ .,])\\d+[,]", "(\\\\d+),"},
    {"(?<=^|[ .,])\\d+[.]", "(\\\\d+)."},
    {"(?<=^|[ .,])\\d+$", "(\\\\d+)"},
    {"\\.", "\\\\."},
    {"(<[^>]*>)", "(.*)"},
  };

  public static final char LEFT_PAR = '(';
  public static final char RIGHT_PAR = ')';
  public static final char LEFT_SQUARE_BRACE = '[';
  public static final char RIGHT_SQUARE_BRACE = ']';
  public static final char LEFT_BRACE = '{';
  public static final char RIGHT_BRACE = '}';

  public static final char ESCAPE_SLASH = '\\';
  public static final String PREFIX_CHAR = "^";
  public static final String SUFFIX_CHAR = "$";

  /**
   * Searches for the all references to element, representing step definition from Gherkin steps.
   * Each step should have poly reference that resolves to this element.
   * Uses {@link #findPossibleGherkinElementUsages(com.intellij.psi.PsiElement, String, com.intellij.psi.search.TextOccurenceProcessor, com.intellij.psi.search.SearchScope)}
   * to find elements. Than, checks for references.
   *
   * @param stepDefinitionElement step defining element (most probably method)
   * @param regexp                regexp step should match
   * @param consumer              each reference would be reported here
   * @param effectiveSearchScope  search scope
   * @return whether reference was found and reported to consumer
   * @see #findPossibleGherkinElementUsages(com.intellij.psi.PsiElement, String, com.intellij.psi.search.TextOccurenceProcessor, com.intellij.psi.search.SearchScope)
   */
  public static boolean findGherkinReferencesToElement(@NotNull final PsiElement stepDefinitionElement,
                                                       @NotNull final String regexp,
                                                       @NotNull final Processor<PsiReference> consumer,
                                                       @NotNull final SearchScope effectiveSearchScope) {
    return findPossibleGherkinElementUsages(stepDefinitionElement, regexp,
                                            new MyReferenceCheckingProcessor(stepDefinitionElement, consumer),
                                            effectiveSearchScope);
  }

  /**
   * Passes to {@link com.intellij.psi.search.TextOccurenceProcessor} all elements in gherkin files that <em>may</em> have reference to
   * provided argument. I.e: calling this function for string literal "(.+)foo" would find step "Given I am foo".
   * To extract search text, {@link #getTheBiggestWordToSearchByIndex(String)} is used.
   *
   * @param stepDefinitionElement step defining element to search refs for.
   * @param regexp                regexp step should match
   * @param processor             each text occurence would be reported here
   * @param effectiveSearchScope  search scope
   * @return whether reference was found and passed to processor
   * @see #findGherkinReferencesToElement(com.intellij.psi.PsiElement, String, com.intellij.util.Processor, com.intellij.psi.search.SearchScope)
   */
  public static boolean findPossibleGherkinElementUsages(@NotNull final PsiElement stepDefinitionElement,
                                                         @NotNull final String regexp,
                                                         @NotNull final TextOccurenceProcessor processor,
                                                         @NotNull final SearchScope effectiveSearchScope) {
    final String word = getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmptyOrSpaces(word)) {
      return true;
    }

    final SearchScope searchScope = CucumberStepSearchUtil.restrictScopeToGherkinFiles(() -> effectiveSearchScope);


    final short context = (short)(UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_CODE);
    final PsiSearchHelper instance = SERVICE.getInstance(stepDefinitionElement.getProject());
    return instance.processElementsWithWord(processor, searchScope, word, context, true);
  }

  public static String getTheBiggestWordToSearchByIndex(@NotNull String regexp) {
    String result = "";
    if (regexp.startsWith(PREFIX_CHAR)) {
      regexp = regexp.substring(1);
    }
    if (regexp.endsWith(SUFFIX_CHAR)) {
      regexp = regexp.substring(0, regexp.length() - 1);
    }

    int par = 0;
    int squareBrace = 0;
    int brace = 0;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < regexp.length(); i++) {
      char c = regexp.charAt(i);
      if (c == '#') {
        sb = new StringBuilder();
        continue;
      }
      if (c != ESCAPE_SLASH) {
        if (c == LEFT_PAR) {
          par++;
        }
        if (c == RIGHT_PAR) {
          if (par > 0) {
            par--;
          }
        }

        if (c == LEFT_BRACE) {
          brace++;
        }
        if (c == RIGHT_BRACE) {
          if (brace > 0) {
            brace--;
          }
        }

        if (c == LEFT_SQUARE_BRACE) {
          squareBrace++;
        }
        if (c == RIGHT_SQUARE_BRACE) {
          if (squareBrace > 0) {
            squareBrace--;
          }
        }
      }
      else {
        sb = new StringBuilder();
        i++;
      }
      if (par > 0 | squareBrace > 0 | brace > 0) {
        if (par + squareBrace + brace == 1) {
          // if it's first brace
          sb = new StringBuilder();
        }
        continue;
      }
      if (Character.isLetterOrDigit(c)) {
        sb.append(c);
        if (sb.length() > 0) {
          if (sb.toString().length() > result.length()) {
            result = sb.toString();
          }
        }
      }
      else {
        sb = new StringBuilder();
      }
    }
    if (sb.length() > 0) {
      if (sb.toString().length() > result.length()) {
        result = sb.toString();
      }
    }
    return result;
  }

  public static String prepareStepRegexp(String stepName) {
    String result = stepName;
    for (String[] rule : ARR) {
      result = result.replaceAll(rule[0], rule[1]);
    }
    return result;
  }

  /**
   * Accepts each element and checks if it has reference to some other element
   */
  private static class MyReferenceCheckingProcessor implements TextOccurenceProcessor {
    @NotNull
    private final PsiElement myElementToFind;
    @NotNull
    private final Processor<PsiReference> myConsumer;

    private MyReferenceCheckingProcessor(@NotNull final PsiElement elementToFind,
                                         @NotNull final Processor<PsiReference> consumer) {
      myElementToFind = elementToFind;
      myConsumer = consumer;
    }

    @Override
    public boolean execute(@NotNull final PsiElement element, final int offsetInElement) {
      final PsiElement parent = element.getParent();
      final boolean result = executeInternal(element);
      // We check element and its parent (StringLiteral is probably child of GherkinStep that has reference)
      // TODO: Search for GherkinStep parent?
      if (result && (parent != null)) {
        return executeInternal(parent);
      }
      return result;
    }

    /**
     * Gets all injected reference and checks if some of them points to {@link #myElementToFind}
     *
     * @param referenceOwner element with injected references
     * @return true if element found and consumed
     */
    private boolean executeInternal(@NotNull final PsiElement referenceOwner) {
      for (final PsiReference ref : referenceOwner.getReferences()) {
        if ((ref != null) && ref.isReferenceTo(myElementToFind)) {
          if (!myConsumer.process(ref)) {
            return false;
          }
        }
      }
      return true;
    }
  }

  /**
   * Substitutes scenario outline parameters into step. For example step from
   * Scenario Outline
   *   Given project with <count> participants
   * Example
   *   | count |
   *   | 10    |
   *
   * will be transformed to
   *   Given project with 10 participants
   *
   * @param stepName
   * @param outlineTableMap mapping from header to the first data row
   * @return OutlineStepSubstitution that contains result step name and can calculate offsets
   */
  @NotNull
  public static OutlineStepSubstitution substituteTableReferences(String stepName, Map<String, String> outlineTableMap) {
    List<Pair<Integer, Integer>> offsets = new ArrayList<>();
    StringBuilder result = new StringBuilder();

    int currentOffset = 0;
    while (true) {
      int start = stepName.indexOf('<', currentOffset);
      if (start < 0) {
        break;
      }

      int end = stepName.indexOf('>', start);
      if (end < 0) {
        break;
      }

      String columnName = stepName.substring(start + 1, end);
      String value = outlineTableMap.get(columnName);
      if (value == null) {
        return new OutlineStepSubstitution(stepName);
      }
      result.append(stepName.subSequence(currentOffset, start));
      int replaceOffset = result.length();
      result.append(value);

      int outlineParameterLength = end - start + 1;
      int valueLength = value.length();
      offsets.add(new Pair<>(replaceOffset, outlineParameterLength - valueLength));

      currentOffset = end + 1;
    }
    result.append(stepName.subSequence(currentOffset, stepName.length()));
    return new OutlineStepSubstitution(result.toString(), offsets);
  }
}
