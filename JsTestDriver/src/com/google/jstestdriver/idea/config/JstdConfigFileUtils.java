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
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JstdConfigFileUtils {

  private static final String BASE_PATH_KEY = "basepath";
  public static final Set<String> KEYS_WITH_INNER_SEQUENCE = new HashSet<String>(Arrays.asList("load", "test", "exclude", "serve"));
  public static final Set<String> VALID_TOP_LEVEL_KEYS = new HashSet<String>(Arrays.asList("server", "plugin", "timeout", "proxy"));

  static {
    VALID_TOP_LEVEL_KEYS.add(BASE_PATH_KEY);
    VALID_TOP_LEVEL_KEYS.addAll(KEYS_WITH_INNER_SEQUENCE);
  }

  private JstdConfigFileUtils() {}

  @Nullable
  public static String extractBasePathAsRawString(@NotNull YAMLDocument document) {
    List<YAMLPsiElement> children = document.getYAMLElements();
    for (YAMLPsiElement child : children) {
      if (child instanceof YAMLKeyValue) {
        YAMLKeyValue keyValue = (YAMLKeyValue) child;
        if (BASE_PATH_KEY.equals(keyValue.getKeyText())) {
          return keyValue.getValueText();
        }
      }
    }
    return null;
  }

  @Nullable
  private static VirtualFile getConfigDir(@NotNull YAMLDocument document) {
    VirtualFile configVF = document.getContainingFile().getOriginalFile().getVirtualFile();
    return configVF == null ? null : configVF.getParent();
  }

  @Nullable
  public static VirtualFile extractBasePath(@NotNull YAMLDocument document) {
    VirtualFile initialBasePath = getConfigDir(document);
    String basePathStr = extractBasePathAsRawString(document);
    if (basePathStr != null) {
      if (initialBasePath != null) {
        VirtualFile vf = initialBasePath.findFileByRelativePath(basePathStr);
        if (vf != null) {
          return vf;
        }
      }
      File file = new File(basePathStr);
      if (file.isAbsolute() && file.exists()) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (vf != null) {
          return vf;
        }
      }
      return null;
    }
    return initialBasePath;
  }

  public static <T extends PsiElement, K> K getVerifiedHierarchyHead(PsiElement psiElement, Class<?>[] hierarchyClasses, Class<K> headHierarchyClass) {
    for (Class<?> clazz : hierarchyClasses) {
      if (!clazz.isInstance(psiElement)) {
        return null;
      }
      psiElement = psiElement.getParent();
    }
    return CastUtils.tryCast(psiElement, headHierarchyClass);
  }

  public static boolean isJstdConfigFile(@NotNull VirtualFile virtualFile) {
    if (!virtualFile.isInLocalFileSystem()) {
      return false;
    }
    FileType fileType = FileTypeManager.getInstance().getFileTypeByFile(virtualFile);
    return fileType == JstdConfigFileType.INSTANCE;
  }

  public static boolean isJstdConfigFileByPsiElement(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file != null) {
      VirtualFile virtualFile = file.getVirtualFile();
      if (virtualFile != null) {
        return isJstdConfigFile(virtualFile);
      }
    }
    return false;
  }

}
