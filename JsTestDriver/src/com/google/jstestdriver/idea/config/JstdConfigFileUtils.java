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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

public class JstdConfigFileUtils {

  private static final String BASE_PATH = "basepath";

  private JstdConfigFileUtils() {}

  public static final Set<String> VALID_TOP_LEVEL_KEYS = new HashSet<String>(Arrays.asList("load", "test", "exclude",
      "server", "plugin", "serve", "timeout", BASE_PATH, "proxy"));

  static VirtualFile extractBasePath(YAMLDocument document) {
    VirtualFile configVF = document.getContainingFile().getOriginalFile().getVirtualFile();
    VirtualFile defaultBasePathVF = null;
    if (configVF != null) {
      defaultBasePathVF = configVF.getParent();
    }
    List<YAMLPsiElement> children = document.getYAMLElements();
    for (YAMLPsiElement child : children) {
      if (child instanceof YAMLKeyValue) {
        YAMLKeyValue keyValue = (YAMLKeyValue) child;
        if (BASE_PATH.equals(keyValue.getKeyText())) {
          String basePath = keyValue.getValueText();
          if (defaultBasePathVF != null) {
            VirtualFile vf = defaultBasePathVF.findFileByRelativePath(basePath);
            if (vf != null) {
              return vf;
            }
          }
          File file = new File(basePath);
          if (file.exists()) {
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
            if (vf != null) {
              return vf;
            }
          }
        }
      }
    }
    return defaultBasePathVF;
  }

  static <T extends PsiElement, K> K getVerifiedHierarchyHead(PsiElement psiElement, Class<?>[] hierarchyClasses, Class<K> headHierarchyClass) {
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
    return fileType == JstdConfigFileType.INSTANCE || fileType == OtherConfigFileType.INSTANCE;
  }

}
