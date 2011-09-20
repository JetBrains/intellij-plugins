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
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

import java.io.File;
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

  private static void annotateDocument(@NotNull YAMLDocument yamlDocument, @NotNull final AnnotationHolder holder) {
    List<Group> groups = buildGroups(yamlDocument);
    if (groups == null) {
      return;
    }

    BasePathInfo basePathInfo = new BasePathInfo(yamlDocument);
    annotateBasePath(basePathInfo, holder);
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
        } else if (JstdConfigFileUtils.isTopLevelKeyWithInnerFileSequence(keyValue)) {
          annotateKeyValueWithInnerFileSequence(keyValue, holder, basePathInfo.getBasePath());
        }
      } else {
        PsiElement element = group.getUnexpectedElement();
        if (!JsPsiUtils.isElementOfType(element, YAMLTokenTypes.EOL, YAMLTokenTypes.INDENT)) {
          holder.createErrorAnnotation(element, "Unexpected element '" + element.getText() + "'");
        }
      }
    }
  }

  private static void annotateBasePath(@NotNull BasePathInfo basePathInfo,
                                       @NotNull AnnotationHolder holder) {
    YAMLKeyValue keyValue = basePathInfo.getKeyValue();
    if (keyValue != null) {
      DocumentFragment documentFragment = basePathInfo.getValueAsDocumentFragment();
      if (documentFragment == null) {
        int offset = keyValue.getTextRange().getEndOffset();
        holder.createErrorAnnotation(TextRange.create(offset - 1, offset), "path is unspecified");
      } else {
        VirtualFile configDir = basePathInfo.getConfigDir();
        if (configDir != null) {
          annotatePath(configDir, documentFragment, holder, false, true);
        }
      }
    }
  }

  private static void annotateKeyValueWithInnerFileSequence(@NotNull YAMLKeyValue keyValue,
                                                            @NotNull final AnnotationHolder holder,
                                                            @Nullable final VirtualFile basePath) {
    YAMLCompoundValue compoundValue = CastUtils.tryCast(keyValue.getValue(), YAMLCompoundValue.class);
    if (compoundValue == null) {
      holder.createErrorAnnotation(keyValue, "YAML sequence was expected here");
      return;
    }
    PsiElement firstIndentElement = compoundValue.getPrevSibling();
    if (firstIndentElement == null || !JsPsiUtils.isElementOfType(firstIndentElement, YAMLTokenTypes.INDENT)) {
      int offset = compoundValue.getTextRange().getStartOffset();
      holder.createErrorAnnotation(TextRange.create(offset, offset), "Indent was expected here");
      return;
    }
    final String firstIndent = StringUtil.notNullize(firstIndentElement.getText());
    compoundValue.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        final YAMLSequence sequence = CastUtils.tryCast(element, YAMLSequence.class);
        if (sequence != null) {
          annotateFileSequence(sequence, holder, basePath);
          return;
        }
        boolean indentType = JsPsiUtils.isElementOfType(element, YAMLTokenTypes.INDENT);
        boolean whitespaceType = JsPsiUtils.isElementOfType(element, YAMLTokenTypes.EOL, YAMLTokenTypes.WHITESPACE);
        if (indentType || whitespaceType) {
          if (indentType && !firstIndent.equals(element.getText())) {
            holder.createErrorAnnotation(element, "All indents should be equal-sized");
          }
        } else {
          holder.createErrorAnnotation(element, "YAML sequence was expected here");
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
      DocumentFragment documentFragment = sequenceTextFragment.toDocumentFragment();
      if (documentFragment != null) {
        annotatePath(basePath, documentFragment, holder, true, false);
      }
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

  private static void annotatePath(@NotNull VirtualFile basePath,
                                      @NotNull DocumentFragment pathAsDocumentFragment,
                                      @NotNull final AnnotationHolder holder,
                                      boolean tolerateRemoteLocations,
                                      boolean expectDirectory) {
    String pathStr = pathAsDocumentFragment.getDocument().getText(pathAsDocumentFragment.getTextRange());
    pathStr = StringUtil.unquoteString(pathStr);
    if (tolerateRemoteLocations && (pathStr.startsWith("http:") || pathStr.startsWith("https:"))) {
      return;
    }
    if (StringUtil.isEmptyOrSpaces(pathStr)) {
      holder.createErrorAnnotation(pathAsDocumentFragment.getTextRange(), "Malformed path");
      return;
    }
    int documentOffset = pathAsDocumentFragment.getTextRange().getStartOffset();
    List<String> components = JstdConfigFileUtils.convertPathToComponentList(pathStr);
    VirtualFile current = basePath;
    if (!components.isEmpty()) {
      String first = components.get(0);
      if (first.length() + 1 <= pathStr.length()) {
        first = pathStr.substring(0, first.length() + 1);
      }
      if (!first.isEmpty()) {
        VirtualFile initial = BasePathInfo.findFile(basePath, first);
        if (initial != null) {
          current = initial;
          components = components.subList(1, components.size());
          documentOffset += first.length();
        }
      }
    }
    File currentFile = new File(current.getPath());
    for (String component : components) {
      if (component.contains("*")) {
        return;
      }
      if (component.isEmpty()) {
        holder.createErrorAnnotation(TextRange.create(documentOffset, documentOffset + 1), "Malformed path");
        return;
      }
      File next = new File(currentFile, component);
      if (!next.exists()) {
        holder.createErrorAnnotation(TextRange.create(documentOffset, documentOffset + component.length()), "No such file or directory '" + component + "'");
        return;
      }
      documentOffset += component.length() + 1;
      currentFile = next;
    }
    if (expectDirectory) {
      if (!current.isDirectory()) {
        holder.createErrorAnnotation(pathAsDocumentFragment.getTextRange(), "A directory is expected");
      }
    } else {
      if (currentFile.isDirectory()) {
        holder.createErrorAnnotation(pathAsDocumentFragment.getTextRange(), "A file is expected");
      }
    }
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
        int startLineNumber = JstdConfigFileUtils.getStartLineNumber(document, element);
        if (previousKeyValueEndLineNumberRef.get() < startLineNumber) {
          if (element instanceof YAMLKeyValue) {
            YAMLKeyValue yamlKeyValue = (YAMLKeyValue)element;
            previousKeyValueEndLineNumberRef.set(JstdConfigFileUtils.getEndLineNumber(document, yamlKeyValue));
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
