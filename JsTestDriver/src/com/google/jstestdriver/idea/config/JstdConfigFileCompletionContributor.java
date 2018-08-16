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

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

import java.io.File;
import java.util.List;

public class JstdConfigFileCompletionContributor extends CompletionContributor {

  private static final String IDENTIFIER_END_PATTERN = ".-:";

  public JstdConfigFileCompletionContributor() {
    extend(CompletionType.BASIC, JstdConfigFileUtils.CONFIG_FILE_ELEMENT_PATTERN, new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        UnquotedText text = new UnquotedText(parameters.getPosition());
        int prefixLength = Math.max(0, parameters.getOffset() - text.getUnquotedDocumentTextRange().getStartOffset());
        BipartiteString caretBipartiteElementText = splitByPrefixLength(text.getUnquotedText(), prefixLength);
        boolean topLevelKeyCompletion = isTopLevelKeyCompletion(parameters);

        addInnerSequencePathCompletionsIfNeeded(parameters, result, caretBipartiteElementText);
        if (topLevelKeyCompletion) {
          addTopLevelKeysCompletionIfNeeded(parameters, result, caretBipartiteElementText);
        } else {
          addBasePathCompletionsIfNeeded(parameters, result, caretBipartiteElementText);
        }
      }
    });
  }

  private static boolean isTopLevelKeyCompletion(@NotNull CompletionParameters parameters) {
    PsiElement psiElement = parameters.getPosition();
    Document document = JsPsiUtils.getDocument(parameters.getOriginalFile());
    if (document != null) {
      TextRange textRange = psiElement.getTextRange();
      int startLine = document.getLineNumber(textRange.getStartOffset());
      int startOffset = document.getLineStartOffset(startLine);
      return startOffset == textRange.getStartOffset();
    }
    return false;
  }

  @Override
  public void beforeCompletion(@NotNull CompletionInitializationContext context) {
    boolean acceptPathSeparator = false;
    final int offset = context.getEditor().getCaretModel().getOffset();
    final PsiElement element = context.getFile().findElementAt(offset);
    if (element != null) {
      if (element.getNode().getElementType() == YAMLTokenTypes.COLON) {
        return;
      }
      int prefixLength = offset - element.getTextRange().getStartOffset();
      BipartiteString caretBipartiteElementText = splitByPrefixLength(element.getText(), prefixLength);
      Character separator = extractDirectoryTrailingFileSeparator(caretBipartiteElementText);
      acceptPathSeparator = separator != null;
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

  private static void addTopLevelKeysCompletionIfNeeded(@NotNull CompletionParameters parameters,
                                                        @NotNull CompletionResultSet result,
                                                        @NotNull BipartiteString caretBipartiteElementText) {
    PsiElement element = parameters.getPosition();
    YAMLDocument yamlDocument = ObjectUtils.tryCast(element.getParent(), YAMLDocument.class);
    if (yamlDocument == null) {
      yamlDocument = JsPsiUtils.getVerifiedHierarchyHead(
        element.getParent(),
        new Class[]{YAMLKeyValue.class},
        YAMLDocument.class
      );
    }
    if (yamlDocument != null) {
      String prefix = caretBipartiteElementText.getPrefix();
      result = result.withPrefixMatcher(prefix);
      for (String key : JstdConfigFileUtils.VALID_TOP_LEVEL_KEYS) {
        if (key.startsWith(prefix)) {
          result.addElement(LookupElementBuilder.create(key + ":"));
        }
      }
    }
  }

  private static void addInnerSequencePathCompletionsIfNeeded(@NotNull CompletionParameters parameters,
                                                              @NotNull CompletionResultSet result,
                                                              @NotNull BipartiteString caretBipartiteElementText) {
    PsiElement element = parameters.getPosition();
    YAMLKeyValue keyValue = JsPsiUtils.getVerifiedHierarchyHead(
      element.getParent(),
      new Class[]{
        YAMLSequenceItem.class,
        YAMLCompoundValue.class
      },
      YAMLKeyValue.class
    );
    BasePathInfo basePathInfo = newBasePathInfo(parameters);
    boolean keyMatched = keyValue != null && JstdConfigFileUtils.isTopLevelKeyWithInnerFileSequence(keyValue);
    if (basePathInfo != null && keyMatched) {
      VirtualFile basePath = basePathInfo.getBasePath();
      if (basePath != null && keyValue.getParent() instanceof YAMLDocument) {
        addPathCompletions(result, caretBipartiteElementText, basePath, false);
      }
    }
  }

  @Nullable
  private static BasePathInfo newBasePathInfo(@NotNull CompletionParameters parameters) {
    YAMLFile yamlFile = ObjectUtils.tryCast(parameters.getOriginalFile(), YAMLFile.class);
    if (yamlFile != null) {
      List<YAMLDocument> yamlDocuments = yamlFile.getDocuments();
      if (!yamlDocuments.isEmpty()) {
        return new BasePathInfo(yamlDocuments.get(0));
      }
    }
    return null;
  }

  private static void addBasePathCompletionsIfNeeded(@NotNull CompletionParameters parameters,
                                                     @NotNull CompletionResultSet result,
                                                     @NotNull BipartiteString caretBipartiteElementText) {
    YAMLKeyValue keyValue = ObjectUtils.tryCast(parameters.getPosition().getParent(), YAMLKeyValue.class);
    if (keyValue != null) {
      if (keyValue.getParent() instanceof YAMLDocument && BasePathInfo.isBasePathKey(keyValue)) {
        BasePathInfo basePathInfo = newBasePathInfo(parameters);
        if (basePathInfo != null) {
          VirtualFile configDir = basePathInfo.getConfigDir();
          if (configDir != null) {
            addPathCompletions(result, caretBipartiteElementText, configDir, true);
          }
        }
      }
    }
  }

  private static void addPathCompletions(@NotNull CompletionResultSet result,
                                         @NotNull BipartiteString caretBipartiteElementText,
                                         @NotNull VirtualFile basePath,
                                         boolean directoryExpected) {
    ParentDirWithLastComponentPrefix parentWithLastComponentPrefix = findParentDirWithLastComponentPrefix(
      basePath, caretBipartiteElementText.getPrefix()
    );
    if (parentWithLastComponentPrefix != null) {
      PrefixMatcher matcher = new PlainPrefixMatcher(parentWithLastComponentPrefix.getLastComponentPrefix());
      result = result.withPrefixMatcher(matcher);
      VirtualFile parentFile = parentWithLastComponentPrefix.getParent();
      VirtualFile[] children = parentFile.getChildren();
      Character dirSeparatorSuffix = extractDirectoryTrailingFileSeparator(caretBipartiteElementText);
      if (parentFile.isDirectory()) {
        result.addElement(LookupElementBuilder.create(".."));
      }
      for (VirtualFile child : children) {
        if (child.isDirectory() || !directoryExpected) {
          String name = child.getName();
          if (child.isDirectory() && dirSeparatorSuffix != null) {
            name += dirSeparatorSuffix;
          }
          result.addElement(LookupElementBuilder.create(name));
        }
      }
    }
  }

  @Nullable
  private static ParentDirWithLastComponentPrefix findParentDirWithLastComponentPrefix(@NotNull VirtualFile basePath,
                                                                                       @NotNull String pathBeforeCaret) {
    BipartiteString parentDirStrWithLastComponent = findParentDirStrWithLastComponentPrefix(pathBeforeCaret);
    String parentDirPath = FileUtil.toSystemIndependentName(parentDirStrWithLastComponent.getPrefix());
    {
      VirtualFile parentFile = basePath.findFileByRelativePath(parentDirPath);
      if (parentFile != null) {
        return new ParentDirWithLastComponentPrefix(parentFile, parentDirStrWithLastComponent.getSuffix());
      }
    }
    File absolutePath = new File(parentDirPath);
    if (absolutePath.isAbsolute()) {
      VirtualFile absolute = LocalFileSystem.getInstance().findFileByIoFile(absolutePath);
      if (absolute != null) {
        return new ParentDirWithLastComponentPrefix(absolute, parentDirStrWithLastComponent.getSuffix());
      }
    }
    return null;
  }

  private static BipartiteString findParentDirStrWithLastComponentPrefix(String pathBeforeCaret) {
    BipartiteString unixBipartiteString = splitByLastIndexOfSeparatorOccurrence(
      pathBeforeCaret, JstdConfigFileUtils.UNIX_PATH_SEPARATOR
    );
    BipartiteString winBipartiteString = splitByLastIndexOfSeparatorOccurrence(
      pathBeforeCaret, JstdConfigFileUtils.WINDOWS_PATH_SEPARATOR
    );
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
    if (index >= 0) {
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
    private final VirtualFile myParent;
    private final String myLastComponentPrefix;

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
