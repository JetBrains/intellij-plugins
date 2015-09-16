package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors;
import com.jetbrains.lang.dart.psi.DartSymbolLiteralExpression;
import com.jetbrains.lang.dart.psi.DartTernaryExpression;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import org.dartlang.analysis.server.protocol.HighlightRegionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartColorAnnotator implements Annotator {

  private static final Key<Boolean> DART_SERVER_DATA_HANDLED = Key.create("DART_SERVER_DATA_HANDLED");

  @Contract("_, null -> false")
  public static boolean canBeAnalyzedByServer(@NotNull final Project project, @Nullable final VirtualFile file) {
    if (file == null || !file.isInLocalFileSystem()) return false;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) return false;

    // server can highlight files from Dart SDK, packages and from modules with enabled Dart support
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    if (fileIndex.isInLibraryClasses(file)) return true;

    final Module module = fileIndex.getModuleForFile(file);
    return module != null && DartSdkGlobalLibUtil.isDartSdkEnabled(module);
  }

  @Nullable
  private static String getHighlightType(@NotNull final String type) {
    if (type.equals(HighlightRegionType.ANNOTATION)) {
      // TODO only '@' remains highlighted as ANNOTATION because reference highlighting wins
      return DartSyntaxHighlighterColors.DART_ANNOTATION;
    }
    //if (type.equals(HighlightRegionType.BUILT_IN)) {
    //  return DartSyntaxHighlighterColors.DART_KEYWORD;
    //}
    if (type.equals(HighlightRegionType.CLASS)) {
      return DartSyntaxHighlighterColors.DART_CLASS;
    }
    if (type.equals(HighlightRegionType.CONSTRUCTOR)) {
      return DartSyntaxHighlighterColors.DART_CONSTRUCTOR;
    }

    if (type.equals(HighlightRegionType.DYNAMIC_LOCAL_VARIABLE_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION;
    }
    if (type.equals(HighlightRegionType.DYNAMIC_LOCAL_VARIABLE_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE;
    }
    if (type.equals(HighlightRegionType.DYNAMIC_PARAMETER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_DYNAMIC_PARAMETER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.DYNAMIC_PARAMETER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_DYNAMIC_PARAMETER_REFERENCE;
    }

    if (type.equals(HighlightRegionType.ENUM)) {
      return DartSyntaxHighlighterColors.DART_ENUM;
    }
    if (type.equals(HighlightRegionType.ENUM_CONSTANT)) {
      return DartSyntaxHighlighterColors.DART_ENUM_CONSTANT;
    }
    if (type.equals(HighlightRegionType.FUNCTION_TYPE_ALIAS)) {
      return DartSyntaxHighlighterColors.DART_FUNCTION_TYPE_ALIAS;
    }

    if (type.equals(HighlightRegionType.INSTANCE_FIELD_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_FIELD_DECLARATION;
    }
    if (type.equals(HighlightRegionType.INSTANCE_FIELD_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_FIELD_REFERENCE;
    }
    if (type.equals(HighlightRegionType.INSTANCE_GETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_GETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.INSTANCE_GETTER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_GETTER_REFERENCE;
    }
    if (type.equals(HighlightRegionType.INSTANCE_METHOD_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_METHOD_DECLARATION;
    }
    if (type.equals(HighlightRegionType.INSTANCE_METHOD_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_METHOD_REFERENCE;
    }
    if (type.equals(HighlightRegionType.INSTANCE_SETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_SETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.INSTANCE_SETTER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_SETTER_REFERENCE;
    }

    if (type.equals(HighlightRegionType.IMPORT_PREFIX)) {
      return DartSyntaxHighlighterColors.DART_IMPORT_PREFIX;
    }
    //if (type.equals(HighlightRegionType.KEYWORD)) {
    //  return DartSyntaxHighlighterColors.DART_KEYWORD;
    //}
    if (type.equals(HighlightRegionType.LABEL)) {
      return DartSyntaxHighlighterColors.DART_LABEL;
    }

    if (type.equals(HighlightRegionType.LOCAL_FUNCTION_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_LOCAL_FUNCTION_DECLARATION;
    }
    if (type.equals(HighlightRegionType.LOCAL_FUNCTION_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_LOCAL_FUNCTION_REFERENCE;
    }
    if (type.equals(HighlightRegionType.LOCAL_VARIABLE_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_DECLARATION;
    }
    if (type.equals(HighlightRegionType.LOCAL_VARIABLE_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_REFERENCE;
    }

    if (type.equals(HighlightRegionType.PARAMETER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_PARAMETER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.PARAMETER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_PARAMETER_REFERENCE;
    }

    if (type.equals(HighlightRegionType.STATIC_FIELD_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_STATIC_FIELD_DECLARATION;
    }
    if (type.equals(HighlightRegionType.STATIC_GETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_STATIC_GETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.STATIC_GETTER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_STATIC_GETTER_REFERENCE;
    }
    if (type.equals(HighlightRegionType.STATIC_METHOD_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_STATIC_METHOD_DECLARATION;
    }
    if (type.equals(HighlightRegionType.STATIC_METHOD_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_STATIC_METHOD_REFERENCE;
    }
    if (type.equals(HighlightRegionType.STATIC_SETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_STATIC_SETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.STATIC_SETTER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_STATIC_SETTER_REFERENCE;
    }

    if (type.equals(HighlightRegionType.TOP_LEVEL_FUNCTION_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_DECLARATION;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_FUNCTION_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_REFERENCE;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_GETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_GETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_GETTER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_GETTER_REFERENCE;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_SETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_SETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_SETTER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_SETTER_REFERENCE;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_VARIABLE_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_DECLARATION;
    }

    if (type.equals(HighlightRegionType.TYPE_NAME_DYNAMIC)) {
      return DartSyntaxHighlighterColors.DART_TYPE_NAME_DYNAMIC;
    }
    if (type.equals(HighlightRegionType.TYPE_PARAMETER)) {
      return DartSyntaxHighlighterColors.DART_TYPE_PARAMETER;
    }
    if (type.equals(HighlightRegionType.UNRESOLVED_INSTANCE_MEMBER_REFERENCE)) {
      return DartSyntaxHighlighterColors.DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE;
    }
    return null;
  }

  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder holder) {
    if (holder.isBatchMode()) return;

    final AnnotationSession session = holder.getCurrentAnnotationSession();
    if (session.getUserData(DART_SERVER_DATA_HANDLED) != Boolean.TRUE) {
      session.putUserData(DART_SERVER_DATA_HANDLED, Boolean.TRUE);

      final VirtualFile vFile = element.getContainingFile().getVirtualFile();
      final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
      if (canBeAnalyzedByServer(element.getProject(), vFile) && service.serverReadyForRequest(element.getProject())) {
        service.updateFilesContent();
        applyServerHighlighting(vFile, holder);
      }
    }

    if (DartTokenTypes.COLON == element.getNode().getElementType() && element.getParent() instanceof DartTernaryExpression) {
      createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.OPERATION_SIGN);
      return;
    }

    if (DartTokenTypesSets.BUILT_IN_IDENTIFIERS.contains(element.getNode().getElementType())) {
      if (element.getNode().getTreeParent().getElementType() != DartTokenTypes.ID) {
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_KEYWORD);
        return;
      }
    }

    // sync*, async* and yield*
    if (DartTokenTypes.MUL == element.getNode().getElementType()) {
      final ASTNode previous = element.getNode().getTreePrev();
      if (previous != null && (previous.getElementType() == DartTokenTypes.SYNC ||
                               previous.getElementType() == DartTokenTypes.ASYNC ||
                               previous.getElementType() == DartTokenTypes.YIELD)) {
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_KEYWORD);
      }
    }

    if (element.getNode().getElementType() == DartTokenTypes.REGULAR_STRING_PART) {
      highlightEscapeSequences(element, holder);
      return;
    }

    if (element instanceof DartSymbolLiteralExpression) {
      createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.SYMBOL_LITERAL);
      //noinspection UnnecessaryReturnStatement
      return;
    }
  }

  private static void applyServerHighlighting(@NotNull final VirtualFile file, @NotNull final AnnotationHolder holder) {
    for (DartAnalysisServerService.PluginHighlightRegion region : DartAnalysisServerService.getInstance().getHighlight(file)) {
      final String attributeKey = getHighlightType(region.getType());
      if (attributeKey != null) {
        final TextRange textRange = new TextRange(region.getOffset(), region.getOffset() + region.getLength());
        createInfoAnnotation(holder, textRange, attributeKey);
      }
    }
  }

  private static void createInfoAnnotation(final @NotNull AnnotationHolder holder,
                                           final @Nullable PsiElement element,
                                           final @NotNull String attributeKey) {
    if (element != null) {
      createInfoAnnotation(holder, element, TextAttributesKey.find(attributeKey));
    }
  }

  private static void createInfoAnnotation(final @NotNull AnnotationHolder holder,
                                           final @Nullable PsiElement element,
                                           final @Nullable TextAttributesKey attributeKey) {
    if (element != null && attributeKey != null) {
      holder.createInfoAnnotation(element, null).setTextAttributes(attributeKey);
    }
  }

  private static void createInfoAnnotation(final @NotNull AnnotationHolder holder,
                                           final @NotNull TextRange textRange,
                                           final @NotNull String attributeKey) {
    holder.createInfoAnnotation(textRange, null).setTextAttributes(TextAttributesKey.find(attributeKey));
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

    final List<Pair<TextRange, Boolean>> result = new ArrayList<Pair<TextRange, Boolean>>();

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
