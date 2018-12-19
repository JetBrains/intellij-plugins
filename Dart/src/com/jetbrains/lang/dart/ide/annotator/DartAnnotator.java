// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.fixes.DartQuickFixSet;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors;
import com.jetbrains.lang.dart.psi.DartSymbolLiteralExpression;
import com.jetbrains.lang.dart.psi.DartTernaryExpression;
import com.jetbrains.lang.dart.sdk.DartSdk;
import gnu.trove.THashMap;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.HighlightRegionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartAnnotator implements Annotator {

  private static final Key<Boolean> DART_SERVER_DATA_HANDLED = Key.create("DART_SERVER_DATA_HANDLED");

  private static final Map<String, String> HIGHLIGHTING_TYPE_MAP = new THashMap<>();

  static {
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.ANNOTATION, DartSyntaxHighlighterColors.DART_ANNOTATION);
    // handled by DartAnnotator without server
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.BUILT_IN, DartSyntaxHighlighterColors.DART_KEYWORD);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.CLASS, DartSyntaxHighlighterColors.DART_CLASS);
    // handled by DartSyntaxHighlighter
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.COMMENT_BLOCK, DartSyntaxHighlighterColors.DART_BLOCK_COMMENT);
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.COMMENT_DOCUMENTATION, DartSyntaxHighlighterColors.DART_DOC_COMMENT);
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.COMMENT_DOCUMENTATION, DartSyntaxHighlighterColors.DART_LINE_COMMENT);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.CONSTRUCTOR, DartSyntaxHighlighterColors.DART_CONSTRUCTOR);
    // No need in special highlighting of the whole region. Individual child regions are highlighted.
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.DIRECTIVE, DartSyntaxHighlighterColors.);
    // HighlightRegionType.DYNAMIC_TYPE - Only for version 1 of highlight.
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.DYNAMIC_LOCAL_VARIABLE_DECLARATION,
                              DartSyntaxHighlighterColors.DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.DYNAMIC_LOCAL_VARIABLE_REFERENCE,
                              DartSyntaxHighlighterColors.DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.DYNAMIC_PARAMETER_DECLARATION,
                              DartSyntaxHighlighterColors.DART_DYNAMIC_PARAMETER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.DYNAMIC_PARAMETER_REFERENCE,
                              DartSyntaxHighlighterColors.DART_DYNAMIC_PARAMETER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.ENUM, DartSyntaxHighlighterColors.DART_ENUM);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.ENUM_CONSTANT, DartSyntaxHighlighterColors.DART_ENUM_CONSTANT);
    // HighlightRegionType.FIELD - Only for version 1 of highlight.
    // HighlightRegionType.FIELD_STATIC - Only for version 1 of highlight.
    // HighlightRegionType.FUNCTION - Only for version 1 of highlight.
    // HighlightRegionType.FUNCTION_DECLARATION - Only for version 1 of highlight.
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.FUNCTION_TYPE_ALIAS, DartSyntaxHighlighterColors.DART_FUNCTION_TYPE_ALIAS);
    // HighlightRegionType.GETTER_DECLARATION - Only for version 1 of highlight.
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.IDENTIFIER_DEFAULT, DartSyntaxHighlighterColors.DART_IDENTIFIER);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.IMPORT_PREFIX, DartSyntaxHighlighterColors.DART_IMPORT_PREFIX);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_FIELD_DECLARATION, DartSyntaxHighlighterColors.DART_INSTANCE_FIELD_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_FIELD_REFERENCE, DartSyntaxHighlighterColors.DART_INSTANCE_FIELD_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_GETTER_DECLARATION,
                              DartSyntaxHighlighterColors.DART_INSTANCE_GETTER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_GETTER_REFERENCE, DartSyntaxHighlighterColors.DART_INSTANCE_GETTER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_METHOD_DECLARATION,
                              DartSyntaxHighlighterColors.DART_INSTANCE_METHOD_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_METHOD_REFERENCE, DartSyntaxHighlighterColors.DART_INSTANCE_METHOD_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_SETTER_DECLARATION,
                              DartSyntaxHighlighterColors.DART_INSTANCE_SETTER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INSTANCE_SETTER_REFERENCE, DartSyntaxHighlighterColors.DART_INSTANCE_SETTER_REFERENCE);
    // handled by DartAnnotator without server
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.INVALID_STRING_ESCAPE, DartSyntaxHighlighterColors.DART_INVALID_STRING_ESCAPE);
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.KEYWORD, DartSyntaxHighlighterColors.DART_KEYWORD);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LABEL, DartSyntaxHighlighterColors.DART_LABEL);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LIBRARY_NAME, DartSyntaxHighlighterColors.DART_LIBRARY_NAME);
    // handled by DartSyntaxHighlighter
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LITERAL_BOOLEAN, DartSyntaxHighlighterColors.DART_KEYWORD);
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LITERAL_DOUBLE, DartSyntaxHighlighterColors.DART_NUMBER);
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LITERAL_INTEGER, DartSyntaxHighlighterColors.DART_NUMBER);
    // No need in special highlighting of the whole map/list literal.
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LITERAL_LIST, DartSyntaxHighlighterColors.);
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LITERAL_MAP, DartSyntaxHighlighterColors.);
    // handled by DartSyntaxHighlighter
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LITERAL_STRING, DartSyntaxHighlighterColors.DART_STRING);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LOCAL_FUNCTION_DECLARATION, DartSyntaxHighlighterColors.DART_LOCAL_FUNCTION_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LOCAL_FUNCTION_REFERENCE, DartSyntaxHighlighterColors.DART_LOCAL_FUNCTION_REFERENCE);
    // HighlightRegionType.LOCAL_VARIABLE - Only for version 1 of highlight.
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LOCAL_VARIABLE_DECLARATION, DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.LOCAL_VARIABLE_REFERENCE, DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_REFERENCE);
    // HighlightRegionType.METHOD - Only for version 1 of highlight.
    // HighlightRegionType.METHOD_DECLARATION - Only for version 1 of highlight.
    // HighlightRegionType.METHOD_DECLARATION_STATIC - Only for version 1 of highlight.
    // HighlightRegionType.METHOD_STATIC - Only for version 1 of highlight.
    // HighlightRegionType.PARAMETER - Only for version 1 of highlight.
    // HighlightRegionType.SETTER_DECLARATION - Only for version 1 of highlight.
    // HighlightRegionType.TOP_LEVEL_VARIABLE - Only for version 1 of highlight.
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.PARAMETER_DECLARATION, DartSyntaxHighlighterColors.DART_PARAMETER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.PARAMETER_REFERENCE, DartSyntaxHighlighterColors.DART_PARAMETER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_FIELD_DECLARATION, DartSyntaxHighlighterColors.DART_STATIC_FIELD_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_GETTER_DECLARATION, DartSyntaxHighlighterColors.DART_STATIC_GETTER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_GETTER_REFERENCE, DartSyntaxHighlighterColors.DART_STATIC_GETTER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_METHOD_DECLARATION, DartSyntaxHighlighterColors.DART_STATIC_METHOD_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_METHOD_REFERENCE, DartSyntaxHighlighterColors.DART_STATIC_METHOD_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_SETTER_DECLARATION, DartSyntaxHighlighterColors.DART_STATIC_SETTER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.STATIC_SETTER_REFERENCE, DartSyntaxHighlighterColors.DART_STATIC_SETTER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_FUNCTION_DECLARATION,
                              DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_FUNCTION_REFERENCE,
                              DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_GETTER_DECLARATION,
                              DartSyntaxHighlighterColors.DART_TOP_LEVEL_GETTER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_GETTER_REFERENCE, DartSyntaxHighlighterColors.DART_TOP_LEVEL_GETTER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_SETTER_DECLARATION,
                              DartSyntaxHighlighterColors.DART_TOP_LEVEL_SETTER_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_SETTER_REFERENCE, DartSyntaxHighlighterColors.DART_TOP_LEVEL_SETTER_REFERENCE);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TOP_LEVEL_VARIABLE_DECLARATION,
                              DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_DECLARATION);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TYPE_NAME_DYNAMIC, DartSyntaxHighlighterColors.DART_TYPE_NAME_DYNAMIC);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.TYPE_PARAMETER, DartSyntaxHighlighterColors.DART_TYPE_PARAMETER);
    HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.UNRESOLVED_INSTANCE_MEMBER_REFERENCE,
                              DartSyntaxHighlighterColors.DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE);
    // handled by DartAnnotator without server
    //HIGHLIGHTING_TYPE_MAP.put(HighlightRegionType.VALID_STRING_ESCAPE, DartSyntaxHighlighterColors.DART_VALID_STRING_ESCAPE);
  }

  @Contract("_, null -> false")
  private static boolean canBeAnalyzedByServer(@NotNull final Project project, @Nullable final VirtualFile file) {
    if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return false;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) return false;

    // server can highlight files from Dart SDK, packages and from modules with enabled Dart support
    return DartAnalysisServerService.getInstance(project).isInIncludedRoots(file);
  }

  public static boolean shouldIgnoreMessageFromDartAnalyzer(@NotNull final String filePath,
                                                            @NotNull final String analysisErrorFileSD) {
    // workaround for https://github.com/dart-lang/sdk/issues/25034
    if (!filePath.equals(FileUtil.toSystemIndependentName(analysisErrorFileSD))) return true;

    return false;
  }

  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder holder) {
    if (holder.isBatchMode()) return;

    final AnnotationSession session = holder.getCurrentAnnotationSession();
    if (session.getUserData(DART_SERVER_DATA_HANDLED) != Boolean.TRUE) {
      session.putUserData(DART_SERVER_DATA_HANDLED, Boolean.TRUE);

      final VirtualFile vFile = element.getContainingFile().getVirtualFile();
      final DartAnalysisServerService service = DartAnalysisServerService.getInstance(element.getProject());
      if (canBeAnalyzedByServer(element.getProject(), vFile) && service.serverReadyForRequest(element.getProject())) {
        service.updateFilesContent();

        if (ApplicationManager.getApplication().isUnitTestMode()) {
          service.waitForAnalysisToComplete_TESTS_ONLY(vFile);
        }

        applyServerHighlighting(vFile, holder);
      }
    }

    if (DartTokenTypes.COLON == element.getNode().getElementType() && element.getParent() instanceof DartTernaryExpression) {
      holder.createInfoAnnotation(element, null).setTextAttributes(DartSyntaxHighlighterColors.OPERATION_SIGN);
      return;
    }

    if (DartTokenTypesSets.BUILT_IN_IDENTIFIERS.contains(element.getNode().getElementType())) {
      if (element.getNode().getTreeParent().getElementType() != DartTokenTypes.ID) {
        holder.createInfoAnnotation(element, null).setTextAttributes(DartSyntaxHighlighterColors.KEYWORD);
        return;
      }
    }

    // sync*, async* and yield*
    if (DartTokenTypes.MUL == element.getNode().getElementType()) {
      final ASTNode previous = element.getNode().getTreePrev();
      if (previous != null && (previous.getElementType() == DartTokenTypes.SYNC ||
                               previous.getElementType() == DartTokenTypes.ASYNC ||
                               previous.getElementType() == DartTokenTypes.YIELD)) {
        holder.createInfoAnnotation(element, null).setTextAttributes(DartSyntaxHighlighterColors.KEYWORD);
      }
    }

    if (element.getNode().getElementType() == DartTokenTypes.REGULAR_STRING_PART) {
      highlightEscapeSequences(element, holder);
      return;
    }

    if (element instanceof DartSymbolLiteralExpression) {
      holder.createInfoAnnotation(element, null).setTextAttributes(DartSyntaxHighlighterColors.SYMBOL_LITERAL);
      //noinspection UnnecessaryReturnStatement
      return;
    }
  }

  private static void applyServerHighlighting(@NotNull final VirtualFile file, @NotNull final AnnotationHolder holder) {
    final PsiFile psiFile = holder.getCurrentAnnotationSession().getFile();

    final DartAnalysisServerService das = DartAnalysisServerService.getInstance(psiFile.getProject());
    for (DartServerData.DartError error : das.getErrors(file)) {
      if (shouldIgnoreMessageFromDartAnalyzer(file.getPath(), error.getAnalysisErrorFileSD())) continue;

      final Annotation annotation = createAnnotation(holder, error, psiFile.getTextLength());

      if (annotation != null) {
        final DartQuickFixSet quickFixSet =
          new DartQuickFixSet(psiFile.getManager(), file, error.getOffset(), error.getCode(), error.getSeverity());

        for (IntentionAction quickFix : quickFixSet.getQuickFixes()) {
          annotation.registerFix(quickFix);
        }

        if (error.getCode() != null) {
          annotation.setProblemGroup(new DartProblemGroup(error.getCode(), error.getSeverity()));
        }
      }
    }

    for (DartServerData.DartHighlightRegion region : das.getHighlight(file)) {
      final String attributeKey = HIGHLIGHTING_TYPE_MAP.get(region.getType());
      if (attributeKey != null) {
        final TextRange textRange = new TextRange(region.getOffset(), region.getOffset() + region.getLength());
        holder.createInfoAnnotation(textRange, null).setTextAttributes(TextAttributesKey.find(attributeKey));
      }
    }
  }

  @Nullable
  private static Annotation createAnnotation(@NotNull final AnnotationHolder holder,
                                             @NotNull final DartServerData.DartError error,
                                             final int fileTextLength) {
    int highlightingStart = error.getOffset();
    int highlightingEnd = error.getOffset() + error.getLength();
    if (highlightingEnd > fileTextLength) highlightingEnd = fileTextLength;
    if (highlightingStart > 0 && highlightingStart >= highlightingEnd) highlightingStart = highlightingEnd - 1;

    final TextRange textRange = new TextRange(highlightingStart, highlightingEnd);

    final String severity = error.getSeverity();
    final String message = StringUtil.notNullize(error.getMessage());

    final ProblemHighlightType specialHighlightType = getSpecialHighlightType(message);
    final Annotation annotation;

    if (AnalysisErrorSeverity.INFO.equals(severity) && specialHighlightType == null) {
      annotation = holder.createWeakWarningAnnotation(textRange, message);
      annotation.setTextAttributes(DartSyntaxHighlighterColors.HINT);
    }
    else if (AnalysisErrorSeverity.WARNING.equals(severity) ||
             (AnalysisErrorSeverity.INFO.equals(severity) && specialHighlightType != null)) {
      annotation = holder.createWarningAnnotation(textRange, message);
      annotation.setTextAttributes(DartSyntaxHighlighterColors.WARNING);
    }
    else if (AnalysisErrorSeverity.ERROR.equals(severity)) {
      annotation = holder.createErrorAnnotation(textRange, message);
      annotation.setTextAttributes(DartSyntaxHighlighterColors.ERROR);
    }
    else {
      annotation = null;
    }

    if (annotation != null && specialHighlightType != null) {
      annotation.setTextAttributes(null);
      annotation.setHighlightType(specialHighlightType);
    }

    return annotation;
  }

  @Nullable
  private static ProblemHighlightType getSpecialHighlightType(@NotNull final String errorMessage) {
    // see [Dart repo]/pkg/analyzer/lib/src/generated/error.dart
    // todo it is now possible to switch to checking error code instead of error message

    if (errorMessage.startsWith("Unused import") ||
        errorMessage.startsWith("Duplicate import") ||
        errorMessage.contains(" is not used") ||
        errorMessage.contains(" isn't used") ||
        errorMessage.startsWith("Dead code")) {
      return ProblemHighlightType.LIKE_UNUSED_SYMBOL;
    }

    if (errorMessage.contains(" is deprecated")) {
      return ProblemHighlightType.LIKE_DEPRECATED;
    }

    return null;
  }

  private static void highlightEscapeSequences(final PsiElement node, final AnnotationHolder holder) {
    final List<Pair<TextRange, Boolean>> escapeSequenceRangesAndValidity = getEscapeSequenceRangesAndValidity(node.getText());
    for (Pair<TextRange, Boolean> rangeAndValidity : escapeSequenceRangesAndValidity) {
      final TextRange range = rangeAndValidity.first.shiftRight(node.getTextRange().getStartOffset());
      final TextAttributesKey attribute =
        rangeAndValidity.second ? DartSyntaxHighlighterColors.VALID_STRING_ESCAPE : DartSyntaxHighlighterColors.INVALID_STRING_ESCAPE;
      if (rangeAndValidity.second) {
        holder.createInfoAnnotation(range, null).setTextAttributes(attribute);
      }
      else {
        holder.createErrorAnnotation(range, DartBundle.message("dart.color.settings.description.invalid.string.escape"))
          .setTextAttributes(attribute);
      }
    }
  }

  @NotNull
  private static List<Pair<TextRange, Boolean>> getEscapeSequenceRangesAndValidity(final @Nullable String text) {
    // \\xFF                 2 hex digits
    // \\uFFFF               4 hex digits
    // \\u{F} - \\u{FFFFFF}  from 1 up to 6 hex digits
    // \\.                   any char except 'x' and 'u'

    if (StringUtil.isEmpty(text)) return Collections.emptyList();

    final List<Pair<TextRange, Boolean>> result = new ArrayList<>();

    int currentIndex = -1;
    while ((currentIndex = text.indexOf('\\', currentIndex)) != -1) {
      final int startIndex = currentIndex;

      if (text.length() <= currentIndex + 1) {
        result.add(Pair.create(new TextRange(startIndex, text.length()), false));
        break;
      }

      final char nextChar = text.charAt(++currentIndex);

      if (nextChar == 'x') {
        if (text.length() <= currentIndex + 2) {
          result.add(Pair.create(new TextRange(startIndex, text.length()), false));
          break;
        }

        final char hexChar1 = text.charAt(++currentIndex);
        final char hexChar2 = text.charAt(++currentIndex);
        final boolean valid = StringUtil.isHexDigit(hexChar1) && StringUtil.isHexDigit(hexChar2);
        currentIndex++;
        result.add(Pair.create(new TextRange(startIndex, currentIndex), valid));
      }
      else if (nextChar == 'u') {
        if (text.length() <= currentIndex + 1) {
          result.add(Pair.create(new TextRange(startIndex, text.length()), false));
          break;
        }

        final char hexOrBraceChar1 = text.charAt(++currentIndex);

        if (hexOrBraceChar1 == '{') {
          currentIndex++;

          final int closingBraceIndex = text.indexOf('}', currentIndex);
          if (closingBraceIndex == -1) {
            result.add(Pair.create(new TextRange(startIndex, currentIndex), false));
          }
          else {
            final String textInBrackets = text.substring(currentIndex, closingBraceIndex);
            currentIndex = closingBraceIndex + 1;

            final boolean valid = textInBrackets.length() > 0 && textInBrackets.length() <= 6 && isHexString(textInBrackets);
            result.add(Pair.create(new TextRange(startIndex, currentIndex), valid));
          }
        }
        else {
          //noinspection UnnecessaryLocalVariable
          final char hexChar1 = hexOrBraceChar1;

          if (text.length() <= currentIndex + 3) {
            result.add(Pair.create(new TextRange(startIndex, text.length()), false));
            break;
          }

          final char hexChar2 = text.charAt(++currentIndex);
          final char hexChar3 = text.charAt(++currentIndex);
          final char hexChar4 = text.charAt(++currentIndex);
          final boolean valid = StringUtil.isHexDigit(hexChar1) &&
                                StringUtil.isHexDigit(hexChar2) &&
                                StringUtil.isHexDigit(hexChar3) &&
                                StringUtil.isHexDigit(hexChar4);
          currentIndex++;
          result.add(Pair.create(new TextRange(startIndex, currentIndex), valid));
        }
      }
      else {
        // not 'x' and not 'u', just any other single character escape
        currentIndex++;
        result.add(Pair.create(new TextRange(startIndex, currentIndex), true));
      }
    }

    return result;
  }

  private static boolean isHexString(final String text) {
    if (StringUtil.isEmpty(text)) return false;

    for (int i = 0; i < text.length(); i++) {
      if (!StringUtil.isHexDigit(text.charAt(i))) return false;
    }

    return true;
  }
}
