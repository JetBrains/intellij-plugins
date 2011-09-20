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

import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.io.File;

public class JstdConfigFileCompletionContributor extends CompletionContributor {

  private static final String IDENTIFIER_END_PATTERN = ".-:";

  public JstdConfigFileCompletionContributor() {
    extend(CompletionType.BASIC, JstdConfigFileUtils.CONFIG_FILE_ELEMENT_PATTERN, new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getOriginalPosition();
        if (element == null) {
          element = parameters.getPosition();
        }
        int prefixLength = parameters.getOffset() - element.getTextRange().getStartOffset();
        String text = element.getText();
        if (JsPsiUtils.isElementOfType(element, YAMLTokenTypes.SCALAR_DSTRING)) {
          text = text.substring(1, text.length() - 1);
          prefixLength = Math.max(0, prefixLength - 1);
        }
        BipartiteString caretBipartiteElementText = splitByPrefixLength(text, prefixLength);
        boolean atFirstColumn = isAtStart(element, parameters.getOffset());

        addInnerSequencePathCompletionsIfNeeded(result, element, caretBipartiteElementText);
        addBasePathCompletionsIfNeeded(result, element, caretBipartiteElementText, atFirstColumn);
        addTopLevelKeysCompletionIfNeeded(result, element, caretBipartiteElementText, atFirstColumn);
      }
    });
  }

  private static boolean isAtStart(@NotNull PsiElement element, int caretOffset) {
    Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
    if (document != null) {
      int lineNumber = document.getLineNumber(caretOffset);
      int startOffset = document.getLineStartOffset(lineNumber);
      return startOffset == caretOffset;
    }
    return false;
  }

  public void beforeCompletion(@NotNull CompletionInitializationContext context) {
    boolean acceptPathSeparator = false;
    {
      final int offset = context.getEditor().getCaretModel().getOffset();
      PsiElement element = context.getFile().findElementAt(offset);
      if (element != null) {
        int prefixLength = offset - element.getTextRange().getStartOffset();
        BipartiteString caretBipartiteElementText = splitByPrefixLength(element.getText(), prefixLength);
        Character separator = extractDirectoryTrailingFileSeparator(caretBipartiteElementText);
        acceptPathSeparator = separator != null;
      }
    }
    final OffsetMap offsetMap = context.getOffsetMap();
    int idEnd = offsetMap.getOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET);
    final String text = context.getFile().getText();
    while (idEnd < text.length()) {
      final char ch = text.charAt(idEnd);
      if (acceptPathSeparator) {
        if (ch == JstdConfigFileUtils.UNIX_PATH_SEPARATOR || ch == JstdConfigFileUtils.WINDOWS_PATH_SEPARATOR) {
          idEnd++;
          break;
        }
      }
      boolean acceptedChar = Character.isJavaIdentifierPart(ch) || IDENTIFIER_END_PATTERN.indexOf(ch) >= 0;
      if (acceptedChar) {
        idEnd++;
      }
      else {
        break;
      }
    }
    offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, idEnd);
  }

  private static void addTopLevelKeysCompletionIfNeeded(@NotNull CompletionResultSet result,
                                                        @NotNull PsiElement element,
                                                        @NotNull BipartiteString caretBipartiteElementText,
                                                        boolean atFirstColumn) {
    YAMLDocument document = CastUtils.tryCast(element.getParent(), YAMLDocument.class);
    if (atFirstColumn && document == null) {
      document = JstdConfigFileUtils.getVerifiedHierarchyHead(
        element.getParent(),
        new Class[]{YAMLKeyValue.class},
        YAMLDocument.class
      );
    }
    if (document != null) {
      String prefix = caretBipartiteElementText.getPrefix();
      result = result.withPrefixMatcher(prefix);
      for (String key : JstdConfigFileUtils.VALID_TOP_LEVEL_KEYS) {
        if (key.startsWith(prefix)) {
          result.addElement(LookupItem.fromString(key + ":"));
        }
      }
    }
  }

  private static void addInnerSequencePathCompletionsIfNeeded(@NotNull CompletionResultSet result,
                                                              @NotNull PsiElement element,
                                                              @NotNull BipartiteString caretBipartiteElementText) {
    YAMLKeyValue keyValue = JstdConfigFileUtils.getVerifiedHierarchyHead(
      element.getParent(),
      new Class[]{
        YAMLSequence.class,
        YAMLCompoundValue.class
      },
      YAMLKeyValue.class
    );
    if (keyValue != null) {
      YAMLDocument yamlDocument = CastUtils.tryCast(keyValue.getParent(), YAMLDocument.class);
      if (yamlDocument != null && JstdConfigFileUtils.isTopLevelKeyWithInnerFileSequence(keyValue)) {
        BasePathInfo basePathInfo = new BasePathInfo(yamlDocument);
        VirtualFile basePath = basePathInfo.getBasePath();
        if (basePath != null) {
          addPathCompletions(result, caretBipartiteElementText, basePath);
        }
      }
    }
  }

  private static void addBasePathCompletionsIfNeeded(@NotNull CompletionResultSet result,
                                                     @NotNull PsiElement element,
                                                     @NotNull BipartiteString caretBipartiteElementText,
                                                     boolean atFirstColumn) {
    YAMLKeyValue keyValue = CastUtils.tryCast(element.getParent(), YAMLKeyValue.class);
    if (!atFirstColumn && keyValue != null) {
      YAMLDocument yamlDocument = CastUtils.tryCast(keyValue.getParent(), YAMLDocument.class);
      if (yamlDocument != null && BasePathInfo.isBasePathKey(keyValue)) {
        BasePathInfo basePathInfo = new BasePathInfo(yamlDocument);
        VirtualFile basePath = basePathInfo.getConfigDir();
        if (basePath != null) {
          addPathCompletions(result, caretBipartiteElementText, basePath);
        }
      }
    }
  }

  private static void addPathCompletions(CompletionResultSet result,
                                         @NotNull BipartiteString caretBipartiteElementText,
                                         @NotNull VirtualFile basePath) {
    ParentDirWithLastComponentPrefix parentWithLastComponentPrefix = findParentDirWithLastComponentPrefix(
      basePath, caretBipartiteElementText.getPrefix()
    );
    if (parentWithLastComponentPrefix != null) {
      result = result.withPrefixMatcher(parentWithLastComponentPrefix.getLastComponentPrefix());
      VirtualFile[] children = parentWithLastComponentPrefix.getParent().getChildren();
      Character dirSeparatorSuffix = extractDirectoryTrailingFileSeparator(caretBipartiteElementText);
      for (VirtualFile child : children) {
        String name = child.getName();
        if (child.isDirectory() && dirSeparatorSuffix != null) {
          name += dirSeparatorSuffix;
        }
        result.addElement(LookupItem.fromString(name));
      }
    }
  }

  @Nullable
  private static ParentDirWithLastComponentPrefix findParentDirWithLastComponentPrefix(@NotNull VirtualFile basePath,
                                                                                       @NotNull String pathBeforeCaret) {
    BipartiteString parentDirStrWithLastComponent = findParentDirStrWithLastComponentPrefix(pathBeforeCaret);
    {
      VirtualFile parentFile = basePath.findFileByRelativePath(FileUtil.toSystemIndependentName(parentDirStrWithLastComponent.getPrefix()));
      if (parentFile != null) {
        return new ParentDirWithLastComponentPrefix(parentFile, parentDirStrWithLastComponent.getSuffix());
      }
    }
    File absolutePath = new File(FileUtil.toSystemIndependentName(parentDirStrWithLastComponent.getPrefix()));
    if (absolutePath.isAbsolute()) {
      VirtualFile absolute = LocalFileSystem.getInstance().findFileByIoFile(absolutePath);
      if (absolute != null) {
        return new ParentDirWithLastComponentPrefix(absolute, parentDirStrWithLastComponent.getSuffix());
      }
    }
    return null;
  }

  private static BipartiteString findParentDirStrWithLastComponentPrefix(String pathBeforeCaret) {
    BipartiteString unixBipartiteString = splitByLastIndexOfSeparatorOccurrence(pathBeforeCaret, JstdConfigFileUtils.UNIX_PATH_SEPARATOR);
    BipartiteString winBipartiteString = splitByLastIndexOfSeparatorOccurrence(pathBeforeCaret, JstdConfigFileUtils.WINDOWS_PATH_SEPARATOR);
    if (unixBipartiteString.getSuffix().length() < winBipartiteString.getSuffix().length()) {
      return unixBipartiteString;
    } else {
      return winBipartiteString;
    }
  }

  private static Character extractPrevalentSeparator(String str) {
    boolean unix = str.indexOf(JstdConfigFileUtils.UNIX_PATH_SEPARATOR) >= 0;
    boolean windows = str.indexOf(JstdConfigFileUtils.WINDOWS_PATH_SEPARATOR) >= 0;
    if (unix && !windows) {
      return JstdConfigFileUtils.UNIX_PATH_SEPARATOR;
    }
    if (!unix && windows) {
      return JstdConfigFileUtils.WINDOWS_PATH_SEPARATOR;
    }
    return null;
  }

  @Nullable
  private static Character extractDirectoryTrailingFileSeparator(BipartiteString caretBipartiteElementText) {
    Character prefixPrevalentSeparator = extractPrevalentSeparator(caretBipartiteElementText.getWholeString());
    if (prefixPrevalentSeparator != null) {
      return prefixPrevalentSeparator;
    }
    Character suffixPrevalentSeparator = extractPrevalentSeparator(caretBipartiteElementText.getSuffix());
    if (suffixPrevalentSeparator != null) {
      return suffixPrevalentSeparator;
    }
    return null;
  }

  @NotNull
  private static BipartiteString splitByLastIndexOfSeparatorOccurrence(@NotNull String str, char separator) {
    int index = str.lastIndexOf(separator);
    if (index > 0) {
      return new BipartiteString(str.substring(0, index + 1), str.substring(index + 1));
    }
    return new BipartiteString("", str);
  }

  @NotNull
  private static BipartiteString splitByPrefixLength(@NotNull String str, int prefixLength) {
    assert prefixLength <= str.length();
    return new BipartiteString(str.substring(0, prefixLength), str.substring(prefixLength));
  }

  private static class ParentDirWithLastComponentPrefix {
    private VirtualFile myParent;
    private String myLastComponentPrefix;

    private ParentDirWithLastComponentPrefix(@NotNull VirtualFile parent, @NotNull String lastComponentPrefix) {
      myParent = parent;
      myLastComponentPrefix = lastComponentPrefix;
    }

    @NotNull
    public VirtualFile getParent() {
      return myParent;
    }

    @NotNull
    public String getLastComponentPrefix() {
      return myLastComponentPrefix;
    }
  }

  private static class BipartiteString {
    private final String myPrefix;
    private final String mySuffix;

    private BipartiteString(@NotNull String prefix, @NotNull String suffix) {
      myPrefix = prefix;
      mySuffix = suffix;
    }

    @NotNull
    public String getPrefix() {
      return myPrefix;
    }

    @NotNull
    public String getSuffix() {
      return mySuffix;
    }

    @NotNull
    public String getWholeString() {
      return myPrefix + mySuffix;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      BipartiteString that = (BipartiteString)o;

      return myPrefix.equals(that.myPrefix) && mySuffix.equals(that.mySuffix);
    }

    @Override
    public int hashCode() {
      int result = myPrefix.hashCode();
      result = 31 * result + mySuffix.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "prefix:'" + myPrefix + "'\', suffix='" + mySuffix + '\'';
    }
  }
}
