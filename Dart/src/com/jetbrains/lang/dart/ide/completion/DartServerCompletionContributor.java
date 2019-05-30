// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartYamlFileTypeFactory;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.apache.commons.lang3.StringUtils;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
               final PsiFile originalFile = parameters.getOriginalFile();
               final Project project = originalFile.getProject();

               if (originalResultSet.getPrefixMatcher().getPrefix().isEmpty() &&
                   isRightAfterBadIdentifier(parameters.getEditor().getDocument().getImmutableCharSequence(), parameters.getOffset())) {
                 return;
               }

               final CompletionSorter sorter = createSorter(parameters, originalResultSet.getPrefixMatcher());
               final String uriPrefix = getPrefixIfCompletingUri(parameters);
               final CompletionResultSet resultSet = uriPrefix != null
                                                     ? originalResultSet.withRelevanceSorter(sorter).withPrefixMatcher(uriPrefix)
                                                     : originalResultSet.withRelevanceSorter(sorter);

               // If completion is requested in "Evaluate Expression" dialog or when editing a breakpoint condition, use runtime completion.
               if (originalFile instanceof DartExpressionCodeFragment) {
                 appendRuntimeCompletion(parameters, resultSet);
                 return;
               }

               VirtualFile file = DartResolveUtil.getRealVirtualFile(originalFile);
               if (file instanceof VirtualFileWindow) {
                 file = ((VirtualFileWindow)file).getDelegate();
               }

               if (file == null) return;

               if (file.getFileType() == HtmlFileType.INSTANCE &&
                   PubspecYamlUtil.findPubspecYamlFile(project, file) == null &&
                   !Registry.is("dart.projects.without.pubspec", false)) {
                 return;
               }

               final DartSdk sdk = DartSdk.getDartSdk(project);
               if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) return;

               final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
               das.updateFilesContent();

               final int offset = InjectedLanguageManager.getInstance(project).injectedToHost(originalFile, parameters.getOffset());
               final String completionId = das.completion_getSuggestions(file, offset);
               if (completionId == null) return;

               final VirtualFile targetFile = file;
               das.addCompletions(file, completionId, (replacementOffset, replacementLength, suggestion) -> {
                 final CompletionResultSet updatedResultSet;
                 if (uriPrefix != null) {
                   updatedResultSet = resultSet;
                 }
                 else {
                   final String specialPrefix = getPrefixForSpecialCases(parameters, replacementOffset);
                   if (specialPrefix != null) {
                     updatedResultSet = resultSet.withPrefixMatcher(specialPrefix);
                   }
                   else {
                     updatedResultSet = resultSet;
                   }
                 }

                 LookupElementBuilder lookupElement = null;

                 for (DartCompletionExtension extension : DartCompletionExtension.getExtensions()) {
                   lookupElement = extension.createLookupElement(project, suggestion);
                   if (lookupElement != null) break;
                 }

                 if (lookupElement == null) {
                   lookupElement = createLookupElement(project, suggestion);
                 }

                 updatedResultSet.addElement(lookupElement);
               }, (includedSet, includedKinds, includedRelevanceTags) -> {
                 final AvailableSuggestionSet suggestionSet = das.getAvailableSuggestionSet(includedSet.getId());
                 if (suggestionSet == null) {
                   return;
                 }

                 for (AvailableSuggestion suggestion : suggestionSet.getItems()) {
                   final String kind = suggestion.getElement().getKind();
                   if (!includedKinds.contains(kind)) {
                     continue;
                   }

                   CompletionSuggestion completionSuggestion =
                     createCompletionSuggestionFromAvailableSuggestion(suggestion, includedSet.getRelevance(), includedRelevanceTags);
                   String displayUri = includedSet.getDisplayUri() != null ? includedSet.getDisplayUri() : suggestionSet.getUri();
                   LookupElementBuilder lookupElement =
                     createLookupElement(project, completionSuggestion, suggestionSet.getId(), targetFile, true, displayUri);

                   resultSet.addElement(lookupElement);
                 }
               });
             }
           });
  }

  private static boolean isRightAfterBadIdentifier(@NotNull CharSequence text, int offset) {
    if (offset == 0 || offset >= text.length()) return false;

    int currentOffset = offset - 1;
    if (!Character.isJavaIdentifierPart(text.charAt(currentOffset))) return false;

    while (currentOffset > 0 && Character.isJavaIdentifierPart(text.charAt(currentOffset - 1))) {
      currentOffset--;
    }

    return !Character.isJavaIdentifierStart(text.charAt(currentOffset));
  }

  private static void appendRuntimeCompletion(@NotNull final CompletionParameters parameters,
                                              @NotNull final CompletionResultSet resultSet) {
    final PsiFile originalFile = parameters.getOriginalFile();
    final Project project = originalFile.getProject();
    final PsiElement contextElement = originalFile.getContext();
    if (contextElement == null) {
      return;
    }

    final VirtualFile contextFile = contextElement.getContainingFile().getVirtualFile();
    final int contextOffset = contextElement.getTextOffset();

    final Document dummyDocument = FileDocumentManager.getInstance().getDocument(originalFile.getVirtualFile());
    if (dummyDocument == null) {
      return;
    }

    final String code = dummyDocument.getText();
    final int codeOffset = parameters.getOffset();

    final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
    final RuntimeCompletionResult completionResult =
      das.execution_getSuggestions(code, codeOffset,
                                   contextFile, contextOffset,
                                   Collections.emptyList(), Collections.emptyList());
    if (completionResult != null && completionResult.suggestions != null) {
      for (CompletionSuggestion suggestion : completionResult.suggestions) {
        LookupElementBuilder lookupElement = createLookupElement(project, suggestion);
        resultSet.addElement(lookupElement);
      }
    }
  }

  private static CompletionSorter createSorter(@NotNull final CompletionParameters parameters, @NotNull final PrefixMatcher prefixMatcher) {
    final LookupElementWeigher dartWeigher = new LookupElementWeigher("dartRelevance", true, false) {
      @Override
      public Integer weigh(@NotNull LookupElement element) {
        final Object lookupObject = element.getObject();
        return lookupObject instanceof DartLookupObject ? ((DartLookupObject)lookupObject).getRelevance() : 0;
      }
    };

    final CompletionSorter defaultSorter = CompletionSorter.defaultSorter(parameters, prefixMatcher);
    return defaultSorter.weighBefore("liftShorter", dartWeigher);
  }

  @Nullable
  private static String getPrefixIfCompletingUri(@NotNull final CompletionParameters parameters) {
    final PsiElement psiElement = parameters.getOriginalPosition();
    final PsiElement parent = psiElement != null ? psiElement.getParent() : null;
    final PsiElement parentParent = parent instanceof DartStringLiteralExpression ? parent.getParent() : null;
    if (parentParent instanceof DartUriElement) {
      final int uriStringOffset = ((DartUriElement)parentParent).getUriStringAndItsRange().second.getStartOffset();
      if (parameters.getOffset() >= parentParent.getTextRange().getStartOffset() + uriStringOffset) {
        return parentParent.getText().substring(uriStringOffset, parameters.getOffset() - parentParent.getTextRange().getStartOffset());
      }
    }
    return null;
  }

  /**
   * Handles completion provided by angular_analyzer_plugin in HTML files and inside string literals;
   * our PSI doesn't allow to calculate prefix in such cases
   */
  @Nullable
  private static String getPrefixForSpecialCases(@NotNull final CompletionParameters parameters, final int replacementOffset) {
    final PsiElement psiElement = parameters.getOriginalPosition();
    if (psiElement == null) return null;

    final PsiElement parent = psiElement.getParent();
    final Language language = psiElement.getContainingFile().getLanguage();
    if (parent instanceof DartStringLiteralExpression || language.isKindOf(XMLLanguage.INSTANCE)) {
      return getPrefixUsingServerData(parameters, replacementOffset);
    }

    return null;
  }

  @Nullable
  private static String getPrefixUsingServerData(@NotNull final CompletionParameters parameters, final int replacementOffset) {
    PsiElement element = parameters.getOriginalPosition();
    if (element == null) return null;

    final InjectedLanguageManager manager = InjectedLanguageManager.getInstance(element.getProject());
    final PsiFile injectedContext = parameters.getOriginalFile();

    final int completionOffset = manager.injectedToHost(injectedContext, parameters.getOffset());
    final TextRange range = manager.injectedToHost(injectedContext, element.getTextRange());

    if (completionOffset < range.getStartOffset() || completionOffset > range.getEndOffset()) return null; // shouldn't happen
    if (replacementOffset > completionOffset) return null; // shouldn't happen

    while (element != null) {
      final int elementStartOffset = manager.injectedToHost(injectedContext, element.getTextRange().getStartOffset());
      if (elementStartOffset <= replacementOffset) {
        break; // that's good, we can use this element to calculate prefix
      }
      element = element.getParent();
    }

    if (element != null) {
      final int startOffset = manager.injectedToHost(injectedContext, element.getTextRange().getStartOffset());
      return element.getText().substring(replacementOffset - startOffset, completionOffset - startOffset);
    }

    return null;
  }

  @Override
  public void beforeCompletion(@NotNull final CompletionInitializationContext context) {
    final PsiElement psiElement = context.getFile().findElementAt(context.getStartOffset());
    final PsiElement parent = psiElement != null ? psiElement.getParent() : null;
    if (parent instanceof DartStringLiteralExpression) {
      final PsiElement parentParent = parent.getParent();
      if (parentParent instanceof DartUriElement) {
        final Pair<String, TextRange> uriAndRange = ((DartUriElement)parentParent).getUriStringAndItsRange();
        context.setReplacementOffset(parentParent.getTextRange().getStartOffset() + uriAndRange.second.getEndOffset());
      }
      else {
        // If replacement context is not set explicitly then com.intellij.codeInsight.completion.CompletionProgressIndicator#duringCompletion
        // implementation looks for the reference at caret and on Tab replaces the whole reference.
        // angular_analyzer_plugin provides angular-specific completion inside Dart string literals. Without the following hack Tab replaces
        // too much useful text. This hack is not ideal though as it may leave a piece of tail not replaced.
        // TODO: use replacementLength received from the server
        context.setReplacementOffset(context.getReplacementOffset());
      }
    }
    else {
      PsiReference reference = context.getFile().findReferenceAt(context.getStartOffset());
      if (reference instanceof PsiMultiReference && ((PsiMultiReference)reference).getReferences().length > 0) {
        reference.getRangeInElement(); // to ensure that references are sorted by range
        reference = ((PsiMultiReference)reference).getReferences()[0];
      }
      if (reference instanceof DartNewExpression ||
          reference instanceof DartParenthesizedExpression ||
          reference instanceof DartListLiteralExpression ||
          reference instanceof DartSetOrMapLiteralExpression) {
        // historically DartNewExpression is a reference; it can appear here only in situation like new Foo(o.<caret>);
        // without the following hack closing paren is replaced on Tab. We won't get here if at least one symbol after dot typed.
        context.setReplacementOffset(context.getStartOffset());
      }
      if (reference instanceof DartReferenceExpression) {
        final PsiElement firstChild = ((DartReferenceExpression)reference).getFirstChild();
        final PsiElement lastChild = ((DartReferenceExpression)reference).getLastChild();
        if (firstChild != lastChild &&
            lastChild instanceof PsiErrorElement &&
            context.getStartOffset() <= firstChild.getTextRange().getEndOffset()) {
          context.setReplacementOffset(firstChild.getTextRange().getEndOffset());
        }
      }
    }
  }

  private static Icon applyOverlay(Icon base, boolean condition, Icon overlay) {
    if (condition) {
      return new LayeredIcon(base, overlay);
    }
    return base;
  }

  @NotNull
  public static LookupElementBuilder createLookupElement(@NotNull final Project project, @NotNull final CompletionSuggestion suggestion) {
    return createLookupElement(project, suggestion, null, null, false, null);
  }

  @NotNull
  public static LookupElementBuilder createLookupElement(@NotNull final Project project,
                                                         @NotNull final CompletionSuggestion suggestion,
                                                         final Integer suggestionSetId,
                                                         final VirtualFile file,
                                                         final boolean isNotYetImported,
                                                         @Nullable final String displayUri) {
    final Element element = suggestion.getElement();
    final Location location = element == null ? null : element.getLocation();
    final DartLookupObject lookupObject = new DartLookupObject(project, location, suggestion.getRelevance());

    final String lookupString = suggestion.getCompletion();
    LookupElementBuilder lookup = LookupElementBuilder.create(lookupObject, lookupString);

    if (suggestion.getDisplayText() != null) {
      lookup = lookup.withPresentableText(suggestion.getDisplayText());
    }

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

      if (StringUtil.isEmpty(suggestion.getDisplayText())) {
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
      }

      // append return type
      final String returnType = element.getReturnType();
      if (!StringUtils.isEmpty(returnType)) {
        lookup = lookup.withTypeText(returnType, true);
      }

      // If this is a class or similar global symbol, try to show which package it's coming from.
      if (!StringUtils.isEmpty(displayUri)) {
        String packageInfo = "(" + displayUri + ")";
        lookup = lookup.withTypeText(StringUtils.isEmpty(returnType) ? packageInfo : returnType + " " + packageInfo, true);
      }

      // icon
      Icon icon = getBaseImage(element);
      if (icon != null) {
        if (suggestion.getKind().equals(CompletionSuggestionKind.OVERRIDE)) {
          icon = new RowIcon(icon, AllIcons.Gutter.OverridingMethod);
        }
        else {
          icon = new RowIcon(icon, element.isPrivate() ? PlatformIcons.PRIVATE_ICON : PlatformIcons.PUBLIC_ICON);
          icon = applyOverlay(icon, element.isFinal(), AllIcons.Nodes.FinalMark);
          icon = applyOverlay(icon, element.isConst(), AllIcons.Nodes.FinalMark);
        }

        lookup = lookup.withIcon(icon);
      }

      // Prepare for typing arguments, if any.
      if (CompletionSuggestionKind.INVOCATION.equals(suggestion.getKind()) && suggestion.getParameterNames() != null) {
        shouldSetSelection = false;
        lookup = lookup.withInsertHandler((context, item) -> handleFunctionInvocationInsertion(context, item, suggestion));
      }
    }

    if (isNotYetImported) {
      lookup = lookup.withInsertHandler((context, item) -> {
        final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
        final GetCompletionDetailsResult result =
          das.completion_getSuggestionDetails(file, suggestionSetId, suggestion.getCompletion(), context.getStartOffset());
        if (result == null) {
          return;
        }

        context.getDocument().replaceString(context.getStartOffset(), context.getTailOffset(), result.completion);

        @Nullable final SourceChange change = result.change;
        if (change == null) {
          return;
        }

        try {
          AssistUtils.applySourceChange(project, change, true);
        }
        catch (DartSourceEditException e) {
          CommonRefactoringUtil.showErrorHint(project, context.getEditor(), e.getMessage(), CommonBundle.getErrorTitle(), null);
          return;
        }

        if (element != null && ElementKind.FUNCTION.equals(element.getKind()) && suggestion.getParameterNames() != null) {
          handleFunctionInvocationInsertion(context, item, suggestion);
        }
      });
    }
    else if (shouldSetSelection) {
      // Use selection offset / length.
      lookup = lookup.withInsertHandler((context, item) -> {
        final Editor editor = context.getEditor();
        final int startOffset = context.getStartOffset() + suggestion.getSelectionOffset();
        final int endOffset = startOffset + suggestion.getSelectionLength();
        editor.getCaretModel().moveToOffset(startOffset);
        if (endOffset > startOffset) {
          editor.getSelectionModel().setSelection(startOffset, endOffset);
        }
      });
    }

    return lookup;
  }

  private static void handleFunctionInvocationInsertion(@NotNull InsertionContext context,
                                                        @NotNull LookupElement item,
                                                        @NotNull CompletionSuggestion suggestion) {
    List<String> parameterNames = suggestion.getParameterNames();
    if (parameterNames == null) {
      return;
    }

    // like in JavaCompletionUtil.insertParentheses()
    final boolean needRightParenth = CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET ||
                                     parameterNames.isEmpty() && context.getCompletionChar() != '(';

    boolean hasParameters = !parameterNames.isEmpty();
    final ParenthesesInsertHandler<LookupElement> handler =
      ParenthesesInsertHandler.getInstance(hasParameters, false, false, needRightParenth, false);
    handler.handleInsert(context, item);

    final Editor editor = context.getEditor();
    if (hasParameters && DartCodeInsightSettings.getInstance().INSERT_DEFAULT_ARG_VALUES) {
      final String argumentListString = suggestion.getDefaultArgumentListString();
      if (argumentListString != null) {
        final Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();

        // At this point caret is expected to be right after the opening paren.
        // But if user was completing using Tab over the existing method call with arguments then old arguments are still there,
        // if so, skip inserting argumentListString

        final CharSequence text = document.getCharsSequence();
        if (text.charAt(offset - 1) == '(' && text.charAt(offset) == ')') {
          document.insertString(offset, argumentListString);

          PsiDocumentManager.getInstance(context.getProject()).commitDocument(document);

          final TemplateBuilderImpl builder =
            (TemplateBuilderImpl)TemplateBuilderFactory.getInstance().createTemplateBuilder(context.getFile());

          final int[] ranges = suggestion.getDefaultArgumentListTextRanges();
          // Only proceed if ranges are provided and well-formed.
          if (ranges != null && (ranges.length & 1) == 0) {
            int index = 0;
            while (index < ranges.length) {
              final int start = ranges[index];
              final int length = ranges[index + 1];
              final String arg = argumentListString.substring(start, start + length);
              final TextExpression expression = new TextExpression(arg);
              final TextRange range = new TextRange(offset + start, offset + start + length);

              index += 2;
              builder.replaceRange(range, "group_" + (index - 1), expression, true);
            }

            builder.run(editor, true);
          }
        }
      }
    }

    Object itemObj = item.getObject();
    if (itemObj instanceof DartLookupObject) {
      AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, ((DartLookupObject)itemObj).findPsiElement());
    }
  }


  @NotNull
  private static CompletionSuggestion createCompletionSuggestionFromAvailableSuggestion(@NotNull AvailableSuggestion suggestion,
                                                                                        int suggestionSetRelevance,
                                                                                        @NotNull Map<String, IncludedSuggestionRelevanceTag> includedSuggestionRelevanceTags) {
    int relevanceBoost = 0;
    List<String> relevanceTags = suggestion.getRelevanceTags();
    if (relevanceTags != null) {
      for (String tag : relevanceTags) {
        IncludedSuggestionRelevanceTag relevanceTag = includedSuggestionRelevanceTags.get(tag);
        if (relevanceTag != null) {
          relevanceBoost = Math.max(relevanceBoost, relevanceTag.getRelevanceBoost());
        }
      }
    }

    Element element = suggestion.getElement();
    return new CompletionSuggestion(
      "UNKNOWN", // we don't have info about CompletionSuggestionKind
      suggestionSetRelevance + relevanceBoost,
      suggestion.getLabel(),
      null,
      0,
      0,
      element.isDeprecated(),
      false,
      suggestion.getDocSummary(),
      suggestion.getDocComplete(),
      null,
      suggestion.getDefaultArgumentListString(),
      suggestion.getDefaultArgumentListTextRanges(),
      element,
      element.getReturnType(),
      suggestion.getParameterNames(),
      null,
      null,
      null,
      null,
      null);
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
    else if (elementKind.equals(ElementKind.MIXIN)) {
      return AllIcons.Nodes.AbstractClass;
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
