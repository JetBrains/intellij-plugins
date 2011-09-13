/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

import java.util.List;
import java.util.Set;

public class JstdConfigFileAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    YAMLFile yamlFile = CastUtils.tryCast(element, YAMLFile.class);
    if (yamlFile != null) {
      annotateFile(yamlFile, holder);
    }
  }

  public static void annotateFile(@NotNull YAMLFile yamlFile, @NotNull AnnotationHolder holder) {
    List<YAMLDocument> documents = yamlFile.getDocuments();
    boolean annotated = false;
    for (YAMLDocument document : documents) {
      if (annotated) {
        holder.createErrorAnnotation(document, "JsTestDriver Configuration File must have only one document");
      } else {
        annotateDocument(document, holder);
      }
      annotated = true;
    }
  }

  private static void annotateDocument(YAMLDocument yamlDocument, final AnnotationHolder holder) {
    final Document document = PsiDocumentManager.getInstance(yamlDocument.getProject()).getDocument(yamlDocument.getContainingFile());
    if (document == null) {
      return;
    }
    List<Group> groups = buildGroups(yamlDocument, document);
    if (groups == null) {
      return;
    }

    final Set<String> visitedKeys = Sets.newHashSet();
    for (Group group : groups) {
      YAMLKeyValue keyValue = group.getKeyValue();
      if (keyValue != null) {
        PsiElement keyElement = keyValue.getKey();
        String keyStr = keyValue.getKeyText();
        if (!JstdConfigFileUtils.VALID_TOP_LEVEL_KEYS.contains(keyStr)) {
          holder.createErrorAnnotation(keyElement, "Unexpected key '" + keyStr + "'");
        }
        if (!visitedKeys.add(keyStr)) {
          holder.createErrorAnnotation(keyElement, "Duplicated '" + keyStr + "' key");
        }
        if (JstdConfigFileUtils.KEYS_WITH_INNER_SEQUENCE.contains(keyStr)) {
          annotateSequenceContent(keyValue, holder, document);
        }
      } else {
        PsiElement element = group.getUnexpectedElement();
        if (element instanceof ASTNode) {
          ASTNode astNode = (ASTNode) element;
          if (astNode.getElementType() != YAMLTokenTypes.EOL && astNode.getElementType() != YAMLTokenTypes.INDENT) {
            holder.createErrorAnnotation(astNode, "Unexpected element '" + astNode.getText() + "'");
          }
        } else {
          holder.createErrorAnnotation(element, "Unexpected element '" + element.getText() + "'");
        }
      }
    }
  }

  private static void annotateSequenceContent(YAMLKeyValue keyValue, final AnnotationHolder holder, @NotNull final Document document) {
    YAMLCompoundValue compoundValue = CastUtils.tryCast(keyValue.getValue(), YAMLCompoundValue.class);
    if (compoundValue == null) {
      holder.createErrorAnnotation(keyValue, "YAML sequence is expected here");
      return;
    }
    ASTNode firstIndent = CastUtils.tryCast(compoundValue.getPrevSibling(), ASTNode.class);
    if (firstIndent == null || firstIndent.getElementType() != YAMLTokenTypes.INDENT) {
      int offset = compoundValue.getTextRange().getStartOffset();
      holder.createErrorAnnotation(TextRange.create(offset, offset), "Indent is expected here");
      return;
    }
    final String indent = StringUtil.notNullize(firstIndent.getText());
    compoundValue.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        final YAMLSequence sequence = CastUtils.tryCast(element, YAMLSequence.class);
        if (sequence != null) {
          if (!isOneLineText(sequence, document)) {
            holder.createErrorAnnotation(sequence, "Unexpected multiline path");
          }
          return;
        }
        ASTNode astNode = CastUtils.tryCast(element, ASTNode.class);
        boolean error = true;
        if (astNode != null) {
          IElementType type = astNode.getElementType();
          if (type == YAMLTokenTypes.INDENT && !indent.equals(astNode.getText())) {
            holder.createErrorAnnotation(astNode, "All indents should be equal-sized");
          }
          error = type != YAMLTokenTypes.INDENT && type != YAMLTokenTypes.EOL && type != YAMLTokenTypes.WHITESPACE;
        }
        if (error) {
          holder.createErrorAnnotation(element, "YAML sequence is expected here");
        }
      }
    });
  }

  private static boolean isOneLineText(@NotNull YAMLSequence sequence, @NotNull final Document document) {
    final Ref<Integer> startLineNumberRef = Ref.create(null);
    final Ref<Integer> endLineNumberRef = Ref.create(null);
    sequence.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        if (JsPsiUtils.isElementOfType(element, YAMLTokenTypes.TEXT)) {
          int startLine = document.getLineNumber(element.getTextRange().getStartOffset());
          int endLine = document.getLineNumber(element.getTextRange().getEndOffset());
          if (startLineNumberRef.isNull()) {
            startLineNumberRef.set(startLine);
          }
          endLineNumberRef.set(endLine);
        }
      }
    });
    Integer startLineNumber = startLineNumberRef.get();
    Integer endLineNumber = endLineNumberRef.get();
    return startLineNumber != null && startLineNumber.equals(endLineNumber);
  }

  @Nullable
  private static List<Group> buildGroups(@NotNull YAMLDocument yamlDocument,
                                         @NotNull final Document document) {
    final List<Group> groups = Lists.newArrayList();
    final Ref<Integer> endLineOfPreviousKeyValueRef = Ref.create(-1);
    yamlDocument.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        int line = document.getLineNumber(element.getTextRange().getStartOffset());
        if (line == endLineOfPreviousKeyValueRef.get()) {
          return;
        }
        if (element instanceof YAMLKeyValue) {
          YAMLKeyValue yamlKeyValue = (YAMLKeyValue) element;
          int endLine = document.getLineNumber(yamlKeyValue.getTextRange().getEndOffset());
          endLineOfPreviousKeyValueRef.set(endLine);
          groups.add(new Group(yamlKeyValue, null));
        } else {
          groups.add(new Group(null, element));
        }
      }
    });
    return groups;
  }

  private static class Group {
    private final YAMLKeyValue myKeyValue;
    private final PsiElement myUnexpectedElement;

    private Group(YAMLKeyValue keyValue, PsiElement unexpectedElement) {
      myKeyValue = keyValue;
      myUnexpectedElement = unexpectedElement;
    }

    public YAMLKeyValue getKeyValue() {
      return myKeyValue;
    }

    public PsiElement getUnexpectedElement() {
      return myUnexpectedElement;
    }
  }

}
