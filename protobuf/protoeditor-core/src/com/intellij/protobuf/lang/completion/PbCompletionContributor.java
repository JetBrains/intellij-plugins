/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.protobuf.lang.util.BuiltInType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/** Provides editor completions for protobuf files. */
public class PbCompletionContributor extends CompletionContributor {

  public PbCompletionContributor() {
    // Keywords for builtin types.
    extend(
        CompletionType.BASIC,
        // This searches up ancestors to find a PbTypeName. For builtin types, the PSI tree
        // typically looks like Identifier ^ SymbolPath ^ TypeName (^ is parent).
        // However, one can't extend a builtin type, and cannot use it in a service method.
        // Also avoid filling in when there are "." qualifiers.
        psiElement()
            .withParent(
                psiElement(ProtoSymbolPath.class)
                    .withParent(
                        psiElement(PbTypeName.class)
                            .andNot(psiElement().withParent(PbExtendDefinition.class))
                            .andNot(psiElement().withParent(PbServiceMethodType.class))))
            .andNot(psiElement().afterLeaf(".")),
        new BuiltinTypesProvider());

    // Keywords that are valid to start a top-level entry.
    extend(
        CompletionType.BASIC,
        psiElement().withParent(PsiErrorElement.class).withSuperParent(2, PbFile.class),
        new TopLevelStartKeywords());

    // Keywords that are valid to start non-top-level entries (like a message entry).
    extend(
        CompletionType.BASIC,
        // Before completing a non-field statement, the element is considered part of a SimpleField.
        psiElement().inside(psiElement(PbSimpleField.class)).andNot(psiElement().afterLeaf(".")),
        new NonTopLevelKeywords());

    // Boolean values
    extend(
        CompletionType.BASIC,
        psiElement()
            .withParent(psiElement(PbIdentifierValue.class).withParent(PbOptionExpression.class)),
        new BooleanKeywords());
  }

  /**
   * Custom autocomplete popup display events.
   *
   * @param position element that the caret is currently positioned in
   * @param typeChar the typed character
   * @return <code>true</code> if autocomplete popup should be displayed
   */
  @Override
  public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
    // Display the popup if the user types '/' in an ImportName.
    return typeChar == '/'
        && position.getParent() instanceof PbStringPart
        && PsiTreeUtil.getParentOfType(position, PbImportName.class) != null;
  }

  private static class BuiltinTypesProvider extends CompletionProvider<CompletionParameters> {

    private static List<LookupElement> createBuiltinTypeBuildersWithSpace() {
      return BuiltInType.getTypes()
        .stream()
        .map(BuiltInType::getName)
        .map(PbCompletionContributor::lookupElementWithSpace)
        .collect(Collectors.toList());
    }

    // TODO(volkman): don't suggest float, double, or bytes for map fields.
    private static List<LookupElement> createBuiltinTypeBuilders() {
      return BuiltInType.getTypes()
        .stream()
        .map(BuiltInType::getName)
        .map(LookupElementBuilder::create)
        .collect(Collectors.toList());
    }

    @Override
    protected void addCompletions(
        @NotNull CompletionParameters parameters,
        @NotNull ProcessingContext processingContext,
        @NotNull CompletionResultSet result) {
      PsiElement element = parameters.getPosition();
      PbField fieldParent = PsiTreeUtil.getParentOfType(element, PbField.class);
      // If this is for a simple field, go ahead and add a space. Otherwise this could be
      // something more complex like a PbMapField, where the type is usually followed by
      // ", " or ">" instead.
      if (fieldParent instanceof PbSimpleField) {
        result.addAllElements(createBuiltinTypeBuildersWithSpace());
        return;
      }
      result.addAllElements(createBuiltinTypeBuilders());
    }
  }

  /** Keywords that are valid to start a "TopLevelEntry". */
  private static class TopLevelStartKeywords extends CompletionProvider<CompletionParameters> {

    private static List<LookupElement> createTopLevelEntryStart() {
      return Stream.of("message", "enum", "service", "extend", "import", "package", "option")
          .map(PbCompletionContributor::lookupElementWithSpace)
          .collect(Collectors.toList());
    }

    @Override
    protected void addCompletions(
        @NotNull CompletionParameters completionParameters,
        @NotNull ProcessingContext processingContext,
        @NotNull CompletionResultSet result) {
      result.addAllElements(createTopLevelEntryStart());
    }
  }

  /** Keywords that are valid to start non-top-level statements like "MessageEntry". */
  private static class NonTopLevelKeywords extends CompletionProvider<CompletionParameters> {

    private static List<LookupElement> createMessageEntryStart() {
      return Stream.of("message", "enum", "extensions", "reserved", "extend", "option", "oneof")
        .map(PbCompletionContributor::lookupElementWithSpace)
        .collect(Collectors.toList());
    }
    // Probably don't want to follow "map" with a space. We could fill in the "<>" later.
    private static List<LookupElement> createMessageEntryStartNoSpace() {
      return Stream.of("map").map(LookupElementBuilder::create).collect(Collectors.toList());
    }

    private static List<LookupElement> createProto2FieldLabels() {
      return Stream.of("optional", "required", "repeated")
        .map(PbCompletionContributor::lookupElementWithSpace)
        .collect(Collectors.toList());
    }

    private static List<LookupElement> createProto3FieldLabels() {
      return Stream.of("optional", "repeated")
        .map(PbCompletionContributor::lookupElementWithSpace)
        .collect(Collectors.toList());
    }

    private static LookupElement createGroupKeyWord() { return lookupElementWithSpace("group"); }

    @Override
    protected void addCompletions(
        @NotNull CompletionParameters parameters,
        @NotNull ProcessingContext processingContext,
        @NotNull CompletionResultSet result) {
      PsiElement element = parameters.getPosition();
      PbSimpleField simpleField = PsiTreeUtil.getParentOfType(element, PbSimpleField.class);
      if (simpleField == null) {
        return;
      }
      SyntaxLevel syntaxLevel = simpleField.getPbFile().getSyntaxLevel();
      // Start of "simple field" (or message entry, before it's determined to be something else).
      if (PbPsiUtil.isFirstInside(element, simpleField)) {
        PbStatementOwner statementOwner =
            PsiTreeUtil.getParentOfType(element, PbStatementOwner.class);
        if (statementOwner instanceof PbMessageType) {
          result.addAllElements(createMessageEntryStart());
          result.addAllElements(createMessageEntryStartNoSpace());
        }
        switch (syntaxLevel) {
          case PROTO2:
            result.addAllElements(createProto2FieldLabels());
            break;
          case PROTO3:
            result.addAllElements(createProto3FieldLabels());
            break;
        }
      } else {
        // In proto2, we can have a "group" right after the field label.
        if (syntaxLevel == SyntaxLevel.PROTO2) {
          List<PbElement> fieldElements =
              PsiTreeUtil.getChildrenOfTypeAsList(simpleField, PbElement.class);
          if (fieldElements.size() == 2) {
            if (fieldElements.get(0) instanceof PbFieldLabel) {
              result.addElement(createGroupKeyWord());
            }
          }
        }
      }
    }
  }

  /** Boolean value keywords */
  private static class BooleanKeywords extends CompletionProvider<CompletionParameters> {

    private static List<LookupElement> createBooleanValues() {
      return Stream.of("true", "false").map(LookupElementBuilder::create).collect(Collectors.toList());
    }

    @Override
    protected void addCompletions(
        @NotNull CompletionParameters parameters,
        @NotNull ProcessingContext processingContext,
        @NotNull CompletionResultSet result) {

      PsiElement element = parameters.getPosition();
      PbOptionExpression option = PsiTreeUtil.getParentOfType(element, PbOptionExpression.class);
      if (option == null) {
        return;
      }
      BuiltInType builtInType = option.getOptionName().getBuiltInType();
      if (builtInType != null && "bool".equals(builtInType.getName())) {
        result.addAllElements(createBooleanValues());
      }
    }
  }

  private static LookupElement lookupElementWithSpace(String keyword) {
    return LookupElementBuilder.create(keyword).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
  }
}
