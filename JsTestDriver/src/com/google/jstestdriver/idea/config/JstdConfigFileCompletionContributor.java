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
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.VirtualFilePattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.util.Arrays;
import java.util.Comparator;

public class JstdConfigFileCompletionContributor extends CompletionContributor {

  private static final char UNIX_PATH_SEPARATOR = '/';
  private static final char WINDOWS_PATH_SEPARATOR = '\\';
  private static final String IDENTIFIER_END_PATTERN = ".-:";

  public JstdConfigFileCompletionContributor() {
    ElementPattern<PsiElement> place = PlatformPatterns.psiElement().inVirtualFile(
      new VirtualFilePattern().ofType(JstdConfigFileType.INSTANCE)
    );
    extend(CompletionType.BASIC, place, new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getOriginalPosition();
        if (element == null) {
          element = parameters.getPosition();
        }
        int prefixLength = parameters.getOffset() - element.getTextRange().getStartOffset();
        BipartiteString caretBipartiteElementText = splitByPrefixLength(element.getText(), prefixLength);
        addPathCompletionsIfNeeded(result, element, caretBipartiteElementText);
        addTopLevelKeysCompletionIfNeeded(result, element, caretBipartiteElementText);
      }
    });
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
        if (ch == UNIX_PATH_SEPARATOR || ch == WINDOWS_PATH_SEPARATOR) {
          idEnd++;
          break;
        }
      }
      boolean acceptedChar = Character.isJavaIdentifierPart(ch) || IDENTIFIER_END_PATTERN.indexOf(ch) >= 0;
      if (acceptedChar) {
        idEnd++;
      } else {
        break;
      }
    }
    offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, idEnd);
  }

  private static void addTopLevelKeysCompletionIfNeeded(@NotNull CompletionResultSet result,
                                                        @NotNull PsiElement element,
                                                        @NotNull BipartiteString caretBipartiteElementText) {
    YAMLDocument document = JstdConfigFileUtils.getVerifiedHierarchyHead(
      element.getParent(),
      new Class[]{YAMLKeyValue.class},
      YAMLDocument.class
    );
    if (document == null) {
      document = JstdConfigFileUtils.getVerifiedHierarchyHead(
        element.getParent(),
        new Class[]{},
        YAMLDocument.class
      );
    }
    if (document != null) {
      addTopLevelKeysCompletions(result, caretBipartiteElementText.getPrefix());
    }
  }

  private static void addPathCompletionsIfNeeded(@NotNull CompletionResultSet result,
                                                 @NotNull PsiElement element,
                                                 @NotNull BipartiteString caretBipartiteElementText) {
    YAMLDocument document = JstdConfigFileUtils.getVerifiedHierarchyHead(
      element.getParent(),
      new Class[]{
        YAMLSequence.class,
        YAMLCompoundValue.class,
        YAMLKeyValue.class
      },
      YAMLDocument.class
    );
    if (document != null) {
      VirtualFile basePath = JstdConfigFileUtils.extractBasePath(document);
      if (basePath != null) {
        addPathCompletions(result, caretBipartiteElementText, basePath);
      }
    }
  }

  private static void addPathCompletions(CompletionResultSet result,
                                         @NotNull BipartiteString caretBipartiteElementText,
                                         @NotNull VirtualFile basePath) {
    ParentDirWithLastComponentPrefix parentWithLastComponentPrefix = findParentWithLastComponentPrefix(
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
  private static ParentDirWithLastComponentPrefix findParentWithLastComponentPrefix(@NotNull VirtualFile basePath,
                                                                                    @NotNull String pathBeforeCaret) {
    BipartiteString[] allSplits = new BipartiteString[]{
      splitByLastIndexOfSeparatorOccurrence(pathBeforeCaret, UNIX_PATH_SEPARATOR),
      splitByLastIndexOfSeparatorOccurrence(pathBeforeCaret, WINDOWS_PATH_SEPARATOR)
    };
    Arrays.sort(allSplits, new Comparator<BipartiteString>() {
      @Override
      public int compare(BipartiteString o1, BipartiteString o2) {
        return o1.getSuffix().length() - o2.getSuffix().length();
      }
    });
    for (BipartiteString bipartite : allSplits) {
      VirtualFile parentFile = basePath.findFileByRelativePath(FileUtil.toSystemIndependentName(bipartite.getPrefix()));
      if (parentFile != null) {
        return new ParentDirWithLastComponentPrefix(parentFile, bipartite.getSuffix());
      }
    }
    return null;
  }

  private static Character extractPrevalentSeparator(String str) {
    boolean unix = str.indexOf(UNIX_PATH_SEPARATOR) >= 0;
    boolean windows = str.indexOf(WINDOWS_PATH_SEPARATOR) >= 0;
    if (unix && !windows) {
      return UNIX_PATH_SEPARATOR;
    }
    if (!unix && windows) {
      return WINDOWS_PATH_SEPARATOR;
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

  private static void addTopLevelKeysCompletions(CompletionResultSet result, String prefix) {
    result = result.withPrefixMatcher(prefix);
    for (String key : JstdConfigFileUtils.VALID_TOP_LEVEL_KEYS) {
      if (key.startsWith(prefix)) {
        result.addElement(LookupItem.fromString(key + ":"));
      }
    }
  }

  @NotNull
  private static BipartiteString splitByLastIndexOfSeparatorOccurrence(@NotNull String str, char separator) {
    int index = str.lastIndexOf(separator);
    if (index > 0) {
      return new BipartiteString(str.substring(0, index), str.substring(index + 1));
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
    public String toString() {
      return "prefix:'" + myPrefix + "'\', suffix='" + mySuffix + '\'';
    }
  }
}
