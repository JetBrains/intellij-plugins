package com.jetbrains.lang.dart.ide.completion;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartYamlFileTypeFactory;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.psi.DartUriElement;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.apache.commons.lang3.StringUtils;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;
import static com.intellij.patterns.StandardPatterns.or;

public class DartServerCompletionContributor extends CompletionContributor {
  public DartServerCompletionContributor() {
    extend(CompletionType.BASIC,
           or(psiElement().withLanguage(DartLanguage.INSTANCE),
              psiElement().inFile(psiFile().withLanguage(HTMLLanguage.INSTANCE)),
              psiElement().inFile(psiFile().withName(DartYamlFileTypeFactory.DOT_ANALYSIS_OPTIONS))),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull final CompletionParameters parameters,
                                           @NotNull final ProcessingContext context,
                                           @NotNull final CompletionResultSet originalResultSet) {
               VirtualFile file = DartResolveUtil.getRealVirtualFile(parameters.getOriginalFile());
               if (file instanceof VirtualFileWindow) {
                 file = ((VirtualFileWindow)file).getDelegate();
               }

               if (file == null) return;

               final Project project = parameters.getOriginalFile().getProject();

               if (file.getFileType() == HtmlFileType.INSTANCE && PubspecYamlUtil.findPubspecYamlFile(project, file) == null) {
                 return;
               }

               final DartSdk sdk = DartSdk.getDartSdk(project);
               if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) return;

               final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
               das.updateFilesContent();

               final String completionId =
                 das.completion_getSuggestions(file, parameters.getOffset());
               if (completionId == null) return;

               final String specialPrefix = getSpecialPrefix(parameters);
               final CompletionResultSet resultSet = specialPrefix != null
                                                     ? originalResultSet.withPrefixMatcher(specialPrefix)
                                                     : originalResultSet;

               das.addCompletions(completionId, suggestion -> {
                 final LookupElement lookupElement = createLookupElement(project, suggestion);
                 resultSet.addElement(lookupElement);
               });
             }
           });
  }

  @Nullable
  private static String getSpecialPrefix(@NotNull final CompletionParameters parameters) {
    final PsiElement psiElement = parameters.getOriginalPosition();
    if (psiElement == null) return null;

    final PsiElement parent = psiElement.getParent();
    if (parent instanceof DartStringLiteralExpression) {
      final PsiElement parentParent = parent.getParent();
      if (parentParent instanceof DartUriElement) {
        final int uriStringOffset = ((DartUriElement)parentParent).getUriStringAndItsRange().second.getStartOffset();
        if (parameters.getOffset() >= parentParent.getTextRange().getStartOffset() + uriStringOffset) {
          return parentParent.getText().substring(uriStringOffset, parameters.getOffset() - parentParent.getTextRange().getStartOffset());
        }
      }
      else {
        return getIdentifierPrefix(parameters);
      }
    }

    final IElementType elementType = psiElement.getNode().getElementType();
    if (elementType == XmlTokenType.XML_DATA_CHARACTERS || elementType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
      return getIdentifierPrefix(parameters);
    }

    return null;
  }

  @Nullable
  private static String getIdentifierPrefix(@NotNull final CompletionParameters parameters) {
    final PsiElement element = parameters.getOriginalPosition();
    if (element == null) return null;

    final int offset = parameters.getOffset();
    final TextRange range = element.getTextRange();
    if (offset < range.getStartOffset() || offset > range.getEndOffset()) return null;

    return getIdentifierInTheEndOfThisString(element.getText().substring(0, parameters.getOffset() - range.getStartOffset()));
  }

  /**
   * If <code>text</code> ends with any valid identifier - this identifier is returned, otherwise empty string is returned.
   * For example if text="foo.bar123" then "bar123" is returned; if text="foo." then empty string is returned.
   */
  @NotNull
  @VisibleForTesting
  public static String getIdentifierInTheEndOfThisString(@NotNull final String text) {
    int prefixStartIndex = text.length();
    for (int i = text.length() - 1; i >= 0; i--) {
      char ch = text.charAt(i);
      if (!Character.isJavaIdentifierPart(ch)) {
        return text.substring(prefixStartIndex);
      }

      if (Character.isJavaIdentifierStart(ch)) {
        prefixStartIndex = i;
      }
    }

    return text.substring(prefixStartIndex);
  }

  @Override
  public void beforeCompletion(@NotNull final CompletionInitializationContext context) {
    final PsiElement psiElement = context.getFile().findElementAt(context.getStartOffset());
    final PsiElement parent = psiElement != null ? psiElement.getParent() : null;
    final PsiElement parentParent = parent instanceof DartStringLiteralExpression ? parent.getParent() : null;
    if (parentParent instanceof DartUriElement) {
      final Pair<String, TextRange> uriAndRange = ((DartUriElement)parentParent).getUriStringAndItsRange();
      context.setReplacementOffset(parentParent.getTextRange().getStartOffset() + uriAndRange.second.getEndOffset());
    }
  }

  private static Icon applyOverlay(Icon base, boolean condition, Icon overlay) {
    if (condition) {
      return new LayeredIcon(base, overlay);
    }
    return base;
  }

  private static Icon applyVisibility(Icon base, boolean isPrivate) {
    RowIcon result = new RowIcon(2);
    result.setIcon(base, 0);
    Icon visibility = isPrivate ? PlatformIcons.PRIVATE_ICON : PlatformIcons.PUBLIC_ICON;
    result.setIcon(visibility, 1);
    return result;
  }

  private static LookupElement createLookupElement(@NotNull final Project project, @NotNull final CompletionSuggestion suggestion) {
    final Element element = suggestion.getElement();
    final Location location = element == null ? null : element.getLocation();
    final DartLookupObject lookupObject = new DartLookupObject(project, location, suggestion.getRelevance());

    final String lookupString = suggestion.getCompletion();
    LookupElementBuilder lookup = LookupElementBuilder.create(lookupObject, lookupString);

    // keywords are bold
    if (suggestion.getKind().equals(CompletionSuggestionKind.KEYWORD)) {
      lookup = lookup.bold();
    }

    final int dotIndex = lookupString.indexOf('.');
    if (dotIndex > 0 && dotIndex < lookupString.length() - 1 &&
        StringUtil.isJavaIdentifier(lookupString.substring(0, dotIndex)) &&
        StringUtil.isJavaIdentifier(lookupString.substring(dotIndex + 1))) {
      // 'path.Context' should match 'Conte' prefix
      lookup = lookup.withLookupString(lookupString.substring(dotIndex + 1));
    }

    boolean shouldSetSelection = true;
    if (element != null) {
      // @deprecated
      if (element.isDeprecated()) {
        lookup = lookup.strikeout();
      }
      // append type parameters
      final String typeParameters = element.getTypeParameters();
      if (typeParameters != null) {
        lookup = lookup.appendTailText(typeParameters, false);
      }
      // append parameters
      final String parameters = element.getParameters();
      if (parameters != null) {
        lookup = lookup.appendTailText(parameters, false);
      }
      // append return type
      final String returnType = element.getReturnType();
      if (!StringUtils.isEmpty(returnType)) {
        lookup = lookup.withTypeText(returnType, true);
      }
      // icon
      Icon icon = getBaseImage(element);
      if (icon != null) {
        icon = applyVisibility(icon, element.isPrivate());
        icon = applyOverlay(icon, element.isFinal(), AllIcons.Nodes.FinalMark);
        icon = applyOverlay(icon, element.isConst(), AllIcons.Nodes.FinalMark);
        lookup = lookup.withIcon(icon);
      }
      // Prepare for typing arguments, if any.
      if (CompletionSuggestionKind.INVOCATION.equals(suggestion.getKind())) {
        shouldSetSelection = false;
        final List<String> parameterNames = suggestion.getParameterNames();
        if (parameterNames != null) {
          lookup = lookup.withInsertHandler(new InsertHandler<LookupElement>() {
            @Override
            public void handleInsert(InsertionContext context, LookupElement item) {
              if (parameterNames.isEmpty()) {
                ParenthesesInsertHandler.NO_PARAMETERS.handleInsert(context, item);
              }
              else {
                ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(context, item);
                // Show parameters popup.
                final Editor editor = context.getEditor();
                final PsiElement psiElement = lookupObject.getElement();
                AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, psiElement);
              }
            }
          });
        }
      }
    }

    // Use selection offset / length.
    if (shouldSetSelection) {
      lookup = lookup.withInsertHandler(new InsertHandler<LookupElement>() {
        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
          final Editor editor = context.getEditor();
          final int startOffset = context.getStartOffset() + suggestion.getSelectionOffset();
          final int endOffset = startOffset + suggestion.getSelectionLength();
          editor.getCaretModel().moveToOffset(startOffset);
          if (endOffset > startOffset) {
            editor.getSelectionModel().setSelection(startOffset, endOffset);
          }
        }
      });
    }

    return lookup;
  }

  private static Icon getBaseImage(Element element) {
    final String elementKind = element.getKind();
    if (elementKind.equals(ElementKind.CLASS) || elementKind.equals(ElementKind.CLASS_TYPE_ALIAS)) {
      if (element.isAbstract()) {
        return AllIcons.Nodes.AbstractClass;
      }
      return AllIcons.Nodes.Class;
    }
    else if (elementKind.equals(ElementKind.ENUM)) {
      return AllIcons.Nodes.Enum;
    }
    else if (elementKind.equals(ElementKind.ENUM_CONSTANT) || elementKind.equals(ElementKind.FIELD)) {
      return AllIcons.Nodes.Field;
    }
    else if (elementKind.equals(ElementKind.COMPILATION_UNIT)) {
      return PlatformIcons.FILE_ICON;
    }
    else if (elementKind.equals(ElementKind.CONSTRUCTOR)) {
      return AllIcons.Nodes.ClassInitializer;
    }
    else if (elementKind.equals(ElementKind.GETTER)) {
      return element.isTopLevelOrStatic() ? AllIcons.Nodes.PropertyReadStatic : AllIcons.Nodes.PropertyRead;
    }
    else if (elementKind.equals(ElementKind.SETTER)) {
      return element.isTopLevelOrStatic() ? AllIcons.Nodes.PropertyWriteStatic : AllIcons.Nodes.PropertyWrite;
    }
    else if (elementKind.equals(ElementKind.METHOD)) {
      if (element.isAbstract()) {
        return AllIcons.Nodes.AbstractMethod;
      }
      return AllIcons.Nodes.Method;
    }
    else if (elementKind.equals(ElementKind.FUNCTION)) {
      return AllIcons.Nodes.Function;
    }
    else if (elementKind.equals(ElementKind.FUNCTION_TYPE_ALIAS)) {
      return AllIcons.Nodes.Annotationtype;
    }
    else if (elementKind.equals(ElementKind.TOP_LEVEL_VARIABLE)) {
      return AllIcons.Nodes.Variable;
    }
    else {
      return null;
    }
  }
}
