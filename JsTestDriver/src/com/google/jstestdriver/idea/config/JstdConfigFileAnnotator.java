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
import com.google.jstestdriver.idea.util.PsiElementFragment;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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
      }
      else {
        annotateDocument(document, holder);
      }
      annotated = true;
    }
  }

  private static void annotateDocument(@NotNull YAMLDocument yamlDocument, @NotNull final AnnotationHolder holder) {
    List<Group> groups = buildGroups(yamlDocument);
    if (groups == null) {
      return;
    }

    VirtualFile basePath = JstdConfigFileUtils.extractBasePath(yamlDocument);
    final Set<String> visitedKeys = Sets.newHashSet();
    for (Group group : groups) {
      YAMLKeyValue keyValue = group.getKeyValue();
      if (keyValue != null) {
        PsiElement keyElement = keyValue.getKey();
        String keyStr = keyValue.getKeyText();
        if (!JstdConfigFileUtils.isTopLevelKey(keyValue)) {
          holder.createErrorAnnotation(keyElement, "Unexpected key '" + keyStr + "'");
        }
        if (!visitedKeys.add(keyStr)) {
          holder.createErrorAnnotation(keyElement, "Duplicated '" + keyStr + "' key");
        }
        if (JstdConfigFileUtils.isBasePathKey(keyValue)) {
          annotateBasePath(yamlDocument, keyValue, holder);
        }
        else if (JstdConfigFileUtils.isKeyWithInnerFileSequence(keyValue)) {
          annotateKeyValueWithInnerFileSequence(keyValue, holder, basePath);
        }
      }
      else {
        PsiElement element = group.getUnexpectedElement();
        if (!JsPsiUtils.isElementOfType(element, YAMLTokenTypes.EOL, YAMLTokenTypes.INDENT)) {
          holder.createErrorAnnotation(element, "Unexpected element '" + element.getText() + "'");
        }
      }
    }
  }

  private static void annotateBasePath(@NotNull YAMLDocument yamlDocument,
                                       @NotNull YAMLKeyValue basePathKeyValue,
                                       @NotNull AnnotationHolder holder) {
    VirtualFile configDir = JstdConfigFileUtils.getConfigDir(basePathKeyValue);
    PsiElement content = basePathKeyValue.getValue();
    if (configDir != null && content != null) {
      final PsiElementFragment<? extends PsiElement> basePathValueFragment;
      if (JsPsiUtils.isElementOfType(content, YAMLTokenTypes.SCALAR_DSTRING)) {
        TextRange textRange = TextRange.create(1, content.getTextLength() - 1);
        basePathValueFragment = PsiElementFragment.create(content, textRange);
      } else {
        if (content.getTextLength() == 0) {
          int start = basePathKeyValue.getKey().getTextRange().getEndOffset();
          holder.createErrorAnnotation(TextRange.create(start - 1, start), "path is unspecified");
          return;
        }
        Document document = JsPsiUtils.getDocument(yamlDocument);
        if (document == null) {
          return;
        }
        int startLine = getEndLineNumber(document, content);
        int documentStartOffset = content.getTextRange().getStartOffset();
        int documentEndOffset = document.getLineStartOffset(startLine + 1) - 1;
        int yamlDocumentStartOffset = yamlDocument.getTextRange().getStartOffset();
        TextRange textRangeInYamlDocument =
          TextRange.create(documentStartOffset - yamlDocumentStartOffset, documentEndOffset - yamlDocumentStartOffset);
        basePathValueFragment = PsiElementFragment.create(yamlDocument, textRangeInYamlDocument);
      }
      annotatePath(configDir, basePathValueFragment, holder, false, true);
    }
  }

  private static void markAllNextSiblingsAsUnexpected(@NotNull PsiElement lastValidElement, @NotNull AnnotationHolder holder) {
    PsiElement next = lastValidElement.getNextSibling();
    while (next != null) {
      holder.createErrorAnnotation(next, "Unexpected element");
      next = next.getNextSibling();
    }
  }

  private static void annotateKeyValueWithInnerFileSequence(@NotNull YAMLKeyValue keyValue,
                                                            @NotNull final AnnotationHolder holder,
                                                            @Nullable final VirtualFile basePath) {
    YAMLCompoundValue compoundValue = CastUtils.tryCast(keyValue.getValue(), YAMLCompoundValue.class);
    if (compoundValue == null) {
      holder.createErrorAnnotation(keyValue, "YAML sequence is expected here");
      return;
    }
    PsiElement firstIndent = compoundValue.getPrevSibling();
    if (firstIndent == null || !JsPsiUtils.isElementOfType(firstIndent, YAMLTokenTypes.INDENT)) {
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
          annotateFileSequence(sequence, holder, basePath);
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

  private static void annotateFileSequence(@NotNull YAMLSequence sequence,
                                           @NotNull AnnotationHolder holder,
                                           @Nullable VirtualFile basePath) {
    if (!isOneLineText(sequence)) {
      holder.createErrorAnnotation(sequence, "Unexpected multiline path");
      return;
    }
    PsiElementFragment<YAMLSequence> sequenceTextFragment = JstdConfigFileUtils.buildSequenceTextFragment(sequence);
    if (basePath != null && sequenceTextFragment != null) {
      annotatePath(basePath, sequenceTextFragment, holder, true, false);
    }
  }

  private static boolean isOneLineText(@NotNull YAMLSequence sequence) {
    PsiElementFragment<YAMLSequence> textSequenceFragment = JstdConfigFileUtils.buildSequenceTextFragment(sequence);
    if (textSequenceFragment != null) {
      DocumentFragment textFragment = textSequenceFragment.toDocumentFragment();
      if (textFragment != null) {
        Document document = textFragment.getDocument();
        TextRange textRange = textFragment.getTextRange();
        int startLine = document.getLineNumber(textRange.getStartOffset());
        int endLine = document.getLineNumber(textRange.getEndOffset());
        return startLine == endLine;
      }
    }
    return false;
  }

  private static boolean annotatePath(@NotNull VirtualFile basePath,
                                      @NotNull PsiElementFragment<? extends PsiElement> pathFragment,
                                      @NotNull final AnnotationHolder holder,
                                      boolean tolerateRemoteLocations,
                                      boolean directoryIsExpected) {
    String pathStr = pathFragment.getText();
    if (tolerateRemoteLocations && (pathStr.startsWith("http:") || pathStr.startsWith("https:"))) {
      return true;
    }
    if (StringUtil.isEmptyOrSpaces(pathStr)) {
      holder.createErrorAnnotation(pathFragment.getDocumentTextRange(), "Invalid path");
      return false;
    }
    int documentOffset = pathFragment.getDocumentTextRange().getStartOffset();
    List<String> components = JstdConfigFileUtils.convertPathToComponentList(pathStr);
    VirtualFile current = basePath;
    if (!components.isEmpty()) {
      String first = components.get(0);
      if (first.isEmpty() && !pathStr.isEmpty()) {
        first = pathStr.substring(0, 1);
      }
      if (!first.isEmpty()) {
        VirtualFile initial = LocalFileSystem.getInstance().findFileByPath(first);
        if (initial != null) {
          current = initial;
          components = components.subList(1, components.size());
          documentOffset += first.length() + 1;
        }
      }
    }
    if (directoryIsExpected && !components.isEmpty() && components.get(components.size() - 1).isEmpty()) {
      components = components.subList(0, components.size() - 1);
    }
    for (String component : components) {
      if (component.contains("*")) {
        return true;
      }
      if (component.isEmpty()) {
        holder.createErrorAnnotation(TextRange.create(documentOffset, documentOffset + 1), "Empty name");
        break;
      }
      VirtualFile next = current.findFileByRelativePath(component);
      if (next == null) {
        holder.createErrorAnnotation(TextRange.create(documentOffset, documentOffset + component.length()), "No such element");
        break;
      }
      documentOffset += component.length() + 1;
      current = next;
    }
    if (directoryIsExpected) {
      if (!current.isDirectory()) {
        holder.createErrorAnnotation(pathFragment.getDocumentTextRange(), "Not a directory");
      }
    } else {
      if (current.isDirectory()) {
        holder.createErrorAnnotation(pathFragment.getDocumentTextRange(), "Not a file");
      }
    }
    return true;
  }

  @Nullable
  private static List<Group> buildGroups(@NotNull YAMLDocument yamlDocument) {
    final Document document = JsPsiUtils.getDocument(yamlDocument);
    if (document == null) {
      return null;
    }
    final List<Group> groups = Lists.newArrayList();
    final Ref<Integer> previousKeyValueEndLineNumberRef = Ref.create(-1);
    yamlDocument.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        int startLineNumber = getStartLineNumber(document, element);
        if (previousKeyValueEndLineNumberRef.get() < startLineNumber) {
          if (element instanceof YAMLKeyValue) {
            YAMLKeyValue yamlKeyValue = (YAMLKeyValue)element;
            previousKeyValueEndLineNumberRef.set(getEndLineNumber(document, yamlKeyValue));
            groups.add(new Group(yamlKeyValue, null));
          }
          else {
            groups.add(new Group(null, element));
          }
        }
      }
    });
    return groups;
  }

  private static int getStartLineNumber(@NotNull Document document, @NotNull PsiElement element) {
    return document.getLineNumber(element.getTextRange().getStartOffset());
  }

  private static int getEndLineNumber(@NotNull Document document, @NotNull PsiElement element) {
    return document.getLineNumber(element.getTextRange().getEndOffset());
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
