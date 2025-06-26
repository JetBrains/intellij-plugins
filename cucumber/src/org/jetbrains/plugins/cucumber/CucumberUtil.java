// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;
import org.jetbrains.plugins.cucumber.steps.search.CucumberStepSearchUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CucumberUtil {
  public static final @NonNls String STEP_DEFINITIONS_DIR_NAME = "step_definitions";

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

  public static final String PREFIX_CHAR = "^";
  public static final String SUFFIX_CHAR = "$";

  /// This regex matches any single character that has special meaning in regular expressions and needs to be escaped.
  ///
  /// Specifically, it matches any one of these characters:
  /// - `\\` (backslash)
  /// - `^` (caret/start anchor)
  /// - `[` (opening square bracket)
  /// - `$` (dollar sign/end anchor)
  /// - `.` (dot/any character)
  /// - `|` (pipe/alternation)
  /// - `?` (question mark/optional)
  /// - `*` (asterisk/zero or more)
  /// - `+` (plus/one or more)
  /// - `]` (closing square bracket)
  private static final Pattern ESCAPE_PATTERN = Pattern.compile("([\\\\^\\[$.|?*+\\]])");

  /// This regex matches optional patterns in Cucumber expressions.
  ///
  /// It distinguishes between `(text)` (to be converted to regex optional groups) and `\\\\(text)` (to be treated as literal characters).
  private static final Pattern OPTIONAL_PATTERN = Pattern.compile("(\\\\\\\\)?\\(([^)]+)\\)");

  /// This regex is designed to match _parameter placeholders_ in Cucumber scenario outlines.
  ///
  /// @see <a href="https://cucumber.io/docs/gherkin/reference#scenario-outline">Gherkin Reference | Scenario Outline</a>
  private static final Pattern PARAMETER_SUBSTITUTION_PATTERN = Pattern.compile("<(?!<)([^>\n\r]+)>");

  private static final Pattern SCRIPT_STYLE_REGEXP = Pattern.compile("^/(.*)/$");

  public static final Map<String, String> STANDARD_PARAMETER_TYPES;

  static {
    Map<String, String> standardParameterTypes = new HashMap<>();
    standardParameterTypes.put("int", "-?\\d+");
    standardParameterTypes.put("float", "-?\\d*[.,]?\\d+");
    standardParameterTypes.put("word", "[^\\s]+");
    standardParameterTypes.put("string", "\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)'");
    standardParameterTypes.put("", "(.*)");

    STANDARD_PARAMETER_TYPES = Collections.unmodifiableMap(standardParameterTypes);
  }

  /**
   * Searches for the all references to element, representing step definition from Gherkin steps.
   * Each step should have poly reference that resolves to this element.
   * Uses {@link #findPossibleGherkinElementUsages(PsiElement, String, TextOccurenceProcessor, SearchScope)}
   * to find elements. Than, checks for references.
   *
   * @param stepDefinitionElement step defining element (most probably method)
   * @param regexp                regexp step should match
   * @param consumer              each reference would be reported here
   * @param effectiveSearchScope  search scope
   * @return whether reference was found and reported to consumer
   * @see #findPossibleGherkinElementUsages(PsiElement, String, TextOccurenceProcessor, SearchScope)
   */
  public static boolean findGherkinReferencesToElement(final @NotNull PsiElement stepDefinitionElement,
                                                       final @NotNull String regexp,
                                                       final @NotNull Processor<? super PsiReference> consumer,
                                                       final @NotNull SearchScope effectiveSearchScope) {
    return findPossibleGherkinElementUsages(stepDefinitionElement, regexp,
                                            new MyReferenceCheckingProcessor(stepDefinitionElement, consumer),
                                            effectiveSearchScope);
  }

  /**
   * Passes to {@link TextOccurenceProcessor} all elements in gherkin files that <em>may</em> have reference to
   * provided argument. I.e: calling this function for string literal "(.+)foo" would find step "Given I am foo".
   * To extract search text, {@link #getTheBiggestWordToSearchByIndex(String)} is used.
   *
   * @param stepDefinitionElement step defining element to search refs for.
   * @param regexp                regexp step should match
   * @param processor             each text occurence would be reported here
   * @param effectiveSearchScope  search scope
   * @return whether reference was found and passed to processor
   * @see #findGherkinReferencesToElement(PsiElement, String, Processor, SearchScope)
   */
  public static boolean findPossibleGherkinElementUsages(final @NotNull PsiElement stepDefinitionElement,
                                                         final @NotNull String regexp,
                                                         final @NotNull TextOccurenceProcessor processor,
                                                         final @NotNull SearchScope effectiveSearchScope) {
    final String word = getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmptyOrSpaces(word)) {
      return true;
    }

    final SearchScope searchScope = ReadAction.compute(() -> CucumberStepSearchUtil.restrictScopeToGherkinFiles(effectiveSearchScope));


    final short context = (short)(UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_CODE);
    final PsiSearchHelper instance = PsiSearchHelper.getInstance(stepDefinitionElement.getProject());
    return instance.processElementsWithWord(processor, searchScope, word, context, true);
  }

  public static void findPossibleGherkinElementUsages(final @NotNull PsiElement stepDefinitionElement,
                                                      final @NotNull String regexp,
                                                      final @NotNull ReferencesSearch.SearchParameters params,
                                                      final @NotNull RequestResultProcessor processor) {
    final String word = getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmptyOrSpaces(word)) {
      return;
    }

    final SearchScope searchScope = CucumberStepSearchUtil.restrictScopeToGherkinFiles(params.getEffectiveSearchScope());
    final short searchContext = (short)(UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_CODE);

    params.getOptimizer().searchWord(word, searchScope, searchContext, true, stepDefinitionElement, processor);
  }

  public static String getTheBiggestWordToSearchByIndex(@NotNull String regexp) {
    String result = "";
    int start = 0;
    if (regexp.startsWith(PREFIX_CHAR)) {
      start += PREFIX_CHAR.length();
    }
    int end = regexp.length();
    if (regexp.endsWith(SUFFIX_CHAR)) {
      end -= SUFFIX_CHAR.length();
    }

    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      char c = regexp.charAt(i);
      if (sb != null && Character.isLetterOrDigit(c)) {
        sb.append(c);
      }
      else {
        if (Character.isWhitespace(c)) {
          if (sb != null && sb.length() > result.length()) {
            result = sb.toString();
          }
          sb = new StringBuilder();
        }
        else {
          sb = null;
        }
      }
    }
    if (sb != null && sb.toString().length() > result.length()) {
      result = sb.toString();
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

  /// Finds the ranges of parameter parts (e.g., `{int}`) in a Cucumber expression.
  ///
  /// This method correctly handles escaped brace `\\{`, which means that what follows is not a parameter.
  ///
  /// @see <a href="https://docs.cucumber.io/cucumber/cucumber-expressions/">Cucumber Expressions</a>
  public static @NotNull List<TextRange> getCukexHighlightRanges(@NotNull String expression) {
    List<TextRange> ranges = new ArrayList<>();

    int parameterStartIndex = -1; // -1 indicates we are not currently inside a parameter.
    boolean isEscaped = false;
    for (int i = 0; i < expression.length(); i++) {
      final char currentChar = expression.charAt(i);

      if (isEscaped) {
        // If the previous character was a backslash, this character is escaped.
        // We do nothing with it and reset the escape flag.
        isEscaped = false;
        continue;
      }

      if (currentChar == '\\') {
        // This is an escape character. Set the flag for the next iteration.
        isEscaped = true;
        continue;
      }

      if (currentChar == '{') {
        // An unescaped opening brace marks the start of a new parameter,
        // but only if we're not already inside another one. This handles
        // malformed input like "a {{b} c" gracefully.
        if (parameterStartIndex == -1) {
          parameterStartIndex = i;
        }
      }
      else if (currentChar == '}') {
        // An unescaped closing brace marks the end of a parameter,
        // but only if a corresponding opening brace was found.
        if (parameterStartIndex != -1) {
          ranges.add(new TextRange(parameterStartIndex, i + 1));
          // Reset the start index to indicate we are no longer inside a parameter.
          parameterStartIndex = -1;
        }
      }
    }

    return ranges;
  }

  /// Finds the ranges of parameter parts (e.g., `{int}`), optional texts (e.g. `(int)`), and alternative texts (e.g., `(int|float)`)
  /// in a Cucumber expression.
  public static @NotNull List<TextRange> getCukexRanges(@NotNull String expression) {
    List<TextRange> ranges = new ArrayList<>();
    int parameterStartIndex = -1; // -1 indicates we are not currently inside a parameter.
    boolean isEscaped = false;
    boolean inAlternativeGroup = false;
    int alternativeGroupStartIndex = -1;

    for (int i = 0; i < expression.length(); i++) {
      final char currentChar = expression.charAt(i);

      if (isEscaped) {
        // If the previous character was a backslash, this character is escaped.
        // We do nothing with it and reset the escape flag.
        isEscaped = false;
      }
      else if (currentChar == '\\') {
        // This is an escape character. Set the flag for the next iteration.
        isEscaped = true;
      }
      else if (currentChar == '{' || currentChar == '(') {
        // An unescaped opening brace marks the start of a new parameter,
        // but only if we're not already inside another one. This handles
        // malformed input like "a {{b} c" gracefully.
        if (parameterStartIndex == -1) {
          parameterStartIndex = i;
        }
      }
      else if (currentChar == '}' || currentChar == ')') {
        // An unescaped closing brace marks the end of a parameter,
        // but only if a corresponding opening brace was found.
        if (parameterStartIndex != -1) {
          ranges.add(new TextRange(parameterStartIndex, i + 1));
          // Reset the start index to indicate we are no longer inside a parameter.
          parameterStartIndex = -1;
        }
      }
      else if (currentChar == '/') {
        // Handle alternative text (e.g., "one/few/many")
        if (!inAlternativeGroup) {
          // Find the start of the word before the slash
          int j = i - 1;
          while (j >= 0 && !Character.isWhitespace(expression.charAt(j))) {
            j--;
          }
          alternativeGroupStartIndex = j + 1;
          inAlternativeGroup = true;
        }
      }
      else if (inAlternativeGroup && Character.isWhitespace(currentChar)) {
        // End the alternative group when we hit whitespace
        ranges.add(new TextRange(alternativeGroupStartIndex, i));
        inAlternativeGroup = false;
        alternativeGroupStartIndex = -1;
      }
    }

    // Handle case where an alternative group extends to the end of the string
    if (inAlternativeGroup && alternativeGroupStartIndex != -1) {
      ranges.add(new TextRange(alternativeGroupStartIndex, expression.length()));
    }

    return ranges;
  }

  public static @NotNull List<String> textRangesOutsideToSubstrings(String cukex, @NotNull List<TextRange> ranges) {
    List<String> result = new ArrayList<>();
    int lastStart = 0;
    for (TextRange range : ranges) {
      String part = cukex.substring(lastStart, range.getStartOffset());
      result.add(part);
      lastStart = range.getEndOffset();
    }
    if (lastStart != 0) {
      String lastPart = cukex.substring(lastStart);
      result.add(lastPart);
    }
    else {
      // If no parameters and alternative/optional texts were found, return the original text
      result.add(cukex);
    }

    return result;
  }

  /**
   * Checks if the expression should be considered as a CucumberExpression or as a RegEx
   *
   * @see <a href="https://github.com/cucumber/cucumber-expressions/blob/v18.0.0/java/heuristics.adoc">heuristic from cucumber library</a>
   * @see <a href="https://github.com/cucumber/cucumber-expressions/blob/v18.0.0/java/src/main/java/io/cucumber/cucumberexpressions/ExpressionFactory.java">implementation in cucumber library</a>
   */
  public static boolean isCucumberExpression(@NotNull String expression) {
    return !expression.startsWith("^") && !expression.endsWith("$") && !SCRIPT_STYLE_REGEXP.matcher(expression).find();
  }

  /// Replaces optional texts inside a Cucumber expression with corresponding regex non-capturing groups.
  ///
  /// ### Example
  ///
  /// See tests of this method for sample inputs and outputs.
  ///
  /// The cukex `I have {int} cucumber(s) in my belly` is equal to the regex `I have \d+ cucumber(?:s)? in my belly`.
  ///
  /// @see <a href="https://github.com/cucumber/cucumber-expressions#optional-text">Cucumber Expressions | Optional text (GitHub repo)</a>
  public static @NotNull String replaceOptionalTextWithRegex(@NotNull String cucumberExpression) {
    Matcher matcher = OPTIONAL_PATTERN.matcher(cucumberExpression);
    StringBuilder result = new StringBuilder();

    while (matcher.find()) {
      String parameterPart = matcher.group(2);
      if ("\\\\".equals(matcher.group(1))) {
        matcher.appendReplacement(result, "\\\\(" + parameterPart + "\\\\)");
      }
      else {
        // Non-capturing group
        matcher.appendReplacement(result, "(" + parameterPart + ")?");
      }
    }

    matcher.appendTail(result);
    return result.toString();
  }

  /// This helper method:
  /// - processes unescaped slashes `/` (alternative text), and wraps converts them into capturing groups, and
  /// - escapes unescaped pipes `|`
  ///
  /// See tests of this method for sample inputs and outputs.
  ///
  /// @see <a href="https://docs.cucumber.io/cucumber/cucumber-expressions">Cucumber Expressions</a>
  public static @NotNull String replaceAlternativeTextWithRegex(@NotNull String cucumberExpression) {
    StringBuilder result = new StringBuilder();
    int i = 0;
    boolean inGroup = false;
    while (i < cucumberExpression.length()) {
      char c = cucumberExpression.charAt(i);
      if (c == '/') {
        if (!inGroup) {
          int j = result.length() - 1;
          while (j >= 0 && !Character.isWhitespace(result.charAt(j))) {
            j--;
          }
          result.insert(j + 1, "(");
          inGroup = true;
        }
        result.append('|');
      }
      else if (c == '|') {
        result.append("\\|");
      }
      else {
        if (inGroup && Character.isWhitespace(c)) {
          result.append(')');
          inGroup = false;
        }
        result.append(c);
      }

      i++;
    }
    if (inGroup) {
      result.append(')');
    }

    return result.toString();
  }

  @TestOnly
  public static @NotNull String buildRegexpFromCucumberExpression(@NotNull String cucumberExpression) {
    return buildRegexpFromCucumberExpression(cucumberExpression, MapParameterTypeManager.DEFAULT);
  }

  //@formatter:off Temporarily disable formatter because of bug IDEA-371809
  /// Builds a regexp from the `cucumberExpression` containing `ParameterType`s.
  /// ### Example
  /// We can go from Cucumber expression:
  /// ```plaintext
  /// provided {int} cucumbers
  /// ```
  /// to regexp:
  /// ```
  /// ^provided (-?\d+) cucumbers$
  /// ```
  ///
  /// @param parameterTypeManager provides mapping from `ParameterType`s name to its value
  /// @return regular expression defined by Cucumber Expression and `ParameterType`s value
  /// @see <a href="https://cucumber.io/docs/cucumber/configuration/#parameter-types">Cucumber Reference | Step Definitions</a>
  /// @see <a href="https://github.com/cucumber/cucumber-expressions">Cucumber Expressions on GitHub</a>
  //@formatter:on
  public static @NotNull String buildRegexpFromCucumberExpression(@NotNull String cucumberExpression,
                                                                  @NotNull ParameterTypeManager parameterTypeManager) {
    String cucumberExpression1 = escapeCucumberExpression(cucumberExpression);
    String cucumberExpression2 = replaceOptionalTextWithRegex(cucumberExpression1);
    String escapedCucumberExpression = replaceAlternativeTextWithRegex(cucumberExpression2);

    List<Pair<TextRange, String>> rangesAndParameterTypeValues = new ArrayList<>();
    processParameterTypesInCucumberExpression(escapedCucumberExpression, range -> {
      String parameterTypeName = escapedCucumberExpression.substring(range.getStartOffset() + 1, range.getEndOffset() - 1);
      String parameterTypeValue = parameterTypeManager.getParameterTypeValue(parameterTypeName);
      rangesAndParameterTypeValues.add(Pair.create(range, parameterTypeValue));
      return true;
    });

    StringBuilder result = new StringBuilder(escapedCucumberExpression);
    Collections.reverse(rangesAndParameterTypeValues);
    for (Pair<TextRange, String> rangeAndValue : rangesAndParameterTypeValues) {
      String value = rangeAndValue.getSecond();
      if (value == null) {
        return escapedCucumberExpression;
      }
      int startOffset = rangeAndValue.first.getStartOffset();
      int endOffset = rangeAndValue.first.getEndOffset();
      result.replace(startOffset, endOffset, "(" + value + ")");
    }
    result.insert(0, '^');
    result.append('$');
    return result.toString();
  }

  /**
   * Processes text ranges of every Parameter Type in Cucumber Expression
   */
  public static void processParameterTypesInCucumberExpression(@NotNull String cucumberExpression,
                                                               @NotNull Processor<? super TextRange> processor) {
    int i = 0;
    boolean isEscaped = false;
    while (i < cucumberExpression.length()) {
      char c = cucumberExpression.charAt(i);

      if (isEscaped) {
        isEscaped = false;
        i++;
        continue;
      }

      if (c == '{') {
        int j = i;
        while (j < cucumberExpression.length()) {
          char parameterTypeChar = cucumberExpression.charAt(j);
          if (parameterTypeChar == '}') {
            break;
          }
          if (parameterTypeChar == '\\') {
            j++;
          }
          j++;
        }
        if (j < cucumberExpression.length()) {
          processor.process(TextRange.create(i, j + 1));
          i = j + 1;
          continue;
        }
        else {
          // unclosed parameter type
          return;
        }
      }

      if (c == '\\') {
        if (i >= cucumberExpression.length() - 1) {
          // escape without following symbol;
          return;
        }
        isEscaped = true;
        i++;
        continue;
      }
      i++;
    }
  }

  /**
   * Accepts each element and checks if it has reference to some other element
   */
  private static final class MyReferenceCheckingProcessor implements TextOccurenceProcessor {
    private final @NotNull PsiElement myElementToFind;
    private final @NotNull Processor<? super PsiReference> myConsumer;

    private MyReferenceCheckingProcessor(final @NotNull PsiElement elementToFind,
                                         final @NotNull Processor<? super PsiReference> consumer) {
      myElementToFind = elementToFind;
      myConsumer = consumer;
    }

    @Override
    public boolean execute(final @NotNull PsiElement element, final int offsetInElement) {
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
    private boolean executeInternal(final @NotNull PsiElement referenceOwner) {
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

  //@formatter:off Temporarily disable formatter because of bug IDEA-371809
  /// Substitutes scenario outline parameters into step.
  /// 
  /// For example we can go from:
  /// ```
  /// Scenario Outline
  ///   Given project with <count> participants
  /// Example
  ///   | count |
  ///   | 10    |
  /// ```
  /// to:
  /// ```
  ///   Given project with 10 participants
  /// ```
  ///
  /// @param outlineTableMap mapping from the header to the first data row
  /// @return OutlineStepSubstitution that contains the result step name and can calculate offsets
  //@formatter:on
  public static @NotNull OutlineStepSubstitution substituteTableReferences(String stepName, @Nullable Map<String, String> outlineTableMap) {
    if (outlineTableMap == null) {
      return new OutlineStepSubstitution(stepName, Collections.emptyList());
    }
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

  public static void addSubstitutionFromText(String text, List<String> substitutions) {
    final Matcher matcher = PARAMETER_SUBSTITUTION_PATTERN.matcher(text);
    boolean result = matcher.find();
    if (!result) {
      return;
    }

    do {
      final String substitution = matcher.group(1);
      if (!StringUtil.isEmpty(substitution) && !substitutions.contains(substitution)) {
        substitutions.add(substitution);
      }
      result = matcher.find();
    }
    while (result);
  }

  /// Escapes a cucumber expression.
  ///
  /// For example, in a Cucumber expression like `I have $5`, the `$` will be escaped to `I have \\$5` so it matches a literal dollar
  /// sign rather than being interpreted as an end-of-line anchor in the resulting regex.
  ///
  /// @see #ESCAPE_PATTERN
  public static String escapeCucumberExpression(@NotNull String stepPattern) {
    return ESCAPE_PATTERN.matcher(stepPattern).replaceAll("\\\\$1");
  }

  public static @Nullable PsiElement resolveSep(@NotNull GherkinStep step) {
    PsiReference reference = ContainerUtil.find(step.getReferences(), r -> r instanceof CucumberStepReference);
    return reference != null ? reference.resolve() : null;
  }

  public static @Nullable Integer getLineNumber(@NotNull PsiElement element) {
    PsiFile containingFile = element.getContainingFile();
    Project project = containingFile.getProject();
    PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
    Document document = psiDocumentManager.getDocument(containingFile);
    int textOffset = element.getTextOffset();
    if (document == null) {
      return null;
    }
    return document.getLineNumber(textOffset) + 1;
  }

  public static @Nullable CucumberStepReference getCucumberStepReference(@Nullable PsiElement element) {
    if (element == null) return null;
    for (PsiReference ref : element.getReferences()) {
      if (ref instanceof CucumberStepReference stepReference) {
        return stepReference;
      }
    }
    return null;
  }

  public static @NotNull List<AbstractStepDefinition> loadFrameworkSteps(@NotNull CucumberJvmExtensionPoint framework,
                                                                         @Nullable PsiFile featureFile,
                                                                         @NotNull Module module) {
    List<AbstractStepDefinition> result = framework.loadStepsFor(featureFile, module);
    return result != null ? result : Collections.emptyList();
  }
}
