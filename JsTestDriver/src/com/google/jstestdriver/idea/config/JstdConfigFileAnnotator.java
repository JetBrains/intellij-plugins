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

import com.google.common.collect.Sets;
import com.google.jstestdriver.idea.util.PsiElementFragment;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class JstdConfigFileAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    YAMLFile yamlFile = ObjectUtils.tryCast(element, YAMLFile.class);
    if (yamlFile != null && JstdConfigFileUtils.isJstdConfigFile(yamlFile)) {
      annotateFile(yamlFile, holder);
    }
  }

  public static void annotateFile(@NotNull YAMLFile yamlFile, @NotNull AnnotationHolder holder) {
    List<YAMLDocument> yamlDocuments = yamlFile.getDocuments();
    boolean annotated = false;
    for (YAMLDocument yamlDocument : yamlDocuments) {
      if (annotated) {
        holder.createErrorAnnotation(yamlDocument, "JsTestDriver configuration file must have only one document");
      }
      else {
        annotateDocument(yamlDocument, holder);
      }
      annotated = true;
    }
  }

  private static void annotateDocument(@NotNull YAMLDocument yamlDocument, @NotNull final AnnotationHolder holder) {
    final YAMLValue value = yamlDocument.getTopLevelValue();
    if (!(value instanceof YAMLMapping)) {
      holder.createErrorAnnotation(yamlDocument, "Expected mapping");
      return;
    }
    final Collection<YAMLKeyValue> keyValues = ((YAMLMapping)value).getKeyValues();
    markStrangeSymbols(yamlDocument, holder);

    BasePathInfo basePathInfo = new BasePathInfo(yamlDocument);
    annotateBasePath(basePathInfo, holder);
    final Set<String> visitedKeys = Sets.newHashSet();
    for (YAMLKeyValue keyValue : keyValues) {
      String keyText = keyValue.getKeyText();
      if (keyValue.getKey() == null) {
        holder.createErrorAnnotation(keyValue.getFirstChild(), "Expected key");
        continue;
      }
      if (!JstdConfigFileUtils.isTopLevelKey(keyValue)) {
        holder.createErrorAnnotation(keyValue.getKey(), "Unexpected key '" + keyText + "'");
      }
      else if (!visitedKeys.add(keyText)) {
        holder.createErrorAnnotation(keyValue.getKey(), "Duplicated '" + keyText + "' key");
      }
      else if (JstdConfigFileUtils.isTopLevelKeyWithInnerFileSequence(keyValue)) {
        annotateKeyValueWithInnerFileSequence(keyValue, holder, basePathInfo.getBasePath());
      }
    }
    if (!visitedKeys.contains("test")) {
      Annotation annotation = holder.createWeakWarningAnnotation(yamlDocument, "JsTestDriver configuration file should have 'test:' section");
      annotation.registerFix(new AddTestSectionAction());
    }
  }

  private static void markStrangeSymbols(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    TextRange textRange = psiElement.getTextRange();
    int startOffset = textRange.getStartOffset();
    String text = psiElement.getText();
    int specialCharactersStartOffset = -1;
    int specialCharactersEndOffset = -1;
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '\t') {
        int offset = startOffset + i;
        holder.createErrorAnnotation(new TextRange(offset, offset + 1), "Tab character is not allowed");
      }
      else if (ch > 127) {
        if (specialCharactersStartOffset == -1) {
          specialCharactersStartOffset = startOffset + i;
        }
        specialCharactersEndOffset = startOffset + i;
      }
      if (specialCharactersEndOffset != -1 && specialCharactersEndOffset != startOffset + i) {
        holder.createErrorAnnotation(new TextRange(specialCharactersStartOffset, specialCharactersEndOffset + 1), "Special characters are not allowed");
        specialCharactersStartOffset = -1;
        specialCharactersEndOffset = -1;
      }
    }
    if (specialCharactersStartOffset != -1) {
      holder.createErrorAnnotation(new TextRange(specialCharactersStartOffset, specialCharactersEndOffset + 1), "Special characters are not allowed");
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
      }
      else {
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
    YAMLValue value = keyValue.getValue();
    if (!(value instanceof YAMLSequence)) {
      holder.createErrorAnnotation(keyValue, "File sequence was expected here");
      return;
    }
    final String indent = StringUtil.repeatSymbol(' ', YAMLUtil.getIndentInThisLine(value));
    for (YAMLSequenceItem item : ((YAMLSequence)value).getItems()) {
      annotateFileSequence(item, holder, basePath, indent);
      
    }
    
    //final String firstIndent = toIndentString(sequence.getPrevSibling());
    //sequence.acceptChildren(new PsiElementVisitor() {
    //  @Override
    //  public void visitElement(PsiElement element) {
    //    final YAMLSequenceItem sequence = ObjectUtils.tryCast(element, YAMLSequenceItem.class);
    //    if (sequence != null) {
    //      return;
    //    }
    //    boolean accepted = JsPsiUtils.isElementOfType(
    //      element,
    //      YAMLTokenTypes.EOL, YAMLTokenTypes.WHITESPACE, YAMLTokenTypes.COMMENT, YAMLTokenTypes.INDENT
    //    );
    //    accepted = accepted || element instanceof PsiWhiteSpace;
    //    if (!accepted) {
    //      holder.createErrorAnnotation(element, "YAML sequence was expected here");
    //    }
    //  }
    //});
  }

  private static void checkSequenceIndent(@NotNull YAMLSequenceItem sequence,
                                          @NotNull AnnotationHolder holder,
                                          @NotNull String expectedIndent) {
    PsiElement prevSibling = sequence.getPrevSibling();
    if (prevSibling != null) {
      String indent = toIndentString(prevSibling);
      if (!expectedIndent.equals(indent)) {
        PsiElement errorElement = sequence;
        if (JsPsiUtils.isElementOfType(prevSibling, YAMLTokenTypes.INDENT)) {
          errorElement = prevSibling;
        } else {
          PsiElement firstElement = sequence.getFirstChild();
          if (firstElement != null && JsPsiUtils.isElementOfType(firstElement, YAMLTokenTypes.SEQUENCE_MARKER)) {
            errorElement = firstElement;
          }
        }
        holder.createErrorAnnotation(errorElement, "All indents should be equal-sized");
      }
    }
  }

  @NotNull
  private static String toIndentString(@Nullable PsiElement indentElement) {
    if (indentElement != null && JsPsiUtils.isElementOfType(indentElement, YAMLTokenTypes.INDENT)) {
      return StringUtil.notNullize(indentElement.getText());
    }
    return "";
  }

  private static void annotateFileSequence(@NotNull YAMLSequenceItem sequence,
                                           @NotNull final AnnotationHolder holder,
                                           @Nullable final VirtualFile basePath,
                                           @NotNull final String expectedIndent) {
    checkSequenceIndent(sequence, holder, expectedIndent);
    final YAMLValue value = sequence.getValue();
    if (value == null) {
      holder.createErrorAnnotation(sequence, "Sequence item is empty");
      return;
    }
    if (value instanceof YAMLSequence) {
      for (YAMLSequenceItem item : ((YAMLSequence)value).getItems()) {
        annotateFileSequence(item, holder, basePath, expectedIndent);
      }
    }
    if (value instanceof YAMLScalar && ((YAMLScalar)value).isMultiline()) {
      holder.createErrorAnnotation(sequence, "Unexpected multiline path");
      return;
    }
    PsiElementFragment<YAMLSequenceItem> sequenceTextFragment = JstdConfigFileUtils.buildSequenceTextFragment(sequence);
    if (basePath != null && sequenceTextFragment != null) {
      DocumentFragment documentFragment = sequenceTextFragment.toDocumentFragment();
      if (documentFragment != null) {
        annotatePath(basePath, documentFragment, holder, true, false);
      }
    }
  }
  
  private static void annotatePath(@NotNull VirtualFile basePath,
                                   @NotNull DocumentFragment pathAsDocumentFragment,
                                   @NotNull final AnnotationHolder holder,
                                   boolean tolerateRemoteLocations,
                                   boolean expectDirectory) {
    String pathStr = pathAsDocumentFragment.getDocument().getText(pathAsDocumentFragment.getTextRange());
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
        holder.createErrorAnnotation(TextRange.create(documentOffset, documentOffset + component.length()),
                                     "No such file or directory '" + component + "'");
        return;
      }
      documentOffset += component.length() + 1;
      currentFile = next;
    }
    if (expectDirectory) {
      if (!current.isDirectory()) {
        holder.createErrorAnnotation(pathAsDocumentFragment.getTextRange(), "A directory is expected");
      }
    }
    else {
      if (currentFile.isDirectory()) {
        holder.createErrorAnnotation(pathAsDocumentFragment.getTextRange(), "A file is expected");
      }
    }
  }

}
