package com.google.jstestdriver.idea.config;

import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
class BasePathInfo {

  static final String BASE_PATH_KEY = "basepath";

  private final YAMLDocument myYAMLDocument;
  private final YAMLKeyValue myKeyValue;
  private final DocumentFragment myDocumentFragment;
  private Ref<VirtualFile> myBasePath;
  private Ref<VirtualFile> myConfigDir;

  BasePathInfo(@NotNull YAMLDocument yamlDocument) {
    myYAMLDocument = yamlDocument;
    Pair<YAMLKeyValue, DocumentFragment> basePathPair = extractBasePathPair(yamlDocument);
    if (basePathPair == null) {
      myKeyValue = null;
      myDocumentFragment = null;
    } else {
      myKeyValue = basePathPair.getFirst();
      myDocumentFragment = basePathPair.getSecond();
    }
  }

  @Nullable
  public YAMLKeyValue getKeyValue() {
    return myKeyValue;
  }

  @Nullable
  public DocumentFragment getValueAsDocumentFragment() {
    return myDocumentFragment;
  }

  @Nullable
  public VirtualFile getConfigDir() {
    if (myConfigDir == null) {
      VirtualFile configFile = myYAMLDocument.getContainingFile().getOriginalFile().getVirtualFile();
      myConfigDir = Ref.create(configFile != null ? configFile.getParent() : null);
    }
    return myConfigDir.get();
  }

  @Nullable
  public VirtualFile findFile(@NotNull String path) {
    return findFile(getBasePath(), path);
  }

  @Nullable
  public static VirtualFile findFile(@Nullable VirtualFile basePath, @NotNull String path) {
    File file = new File(path);
    if (file.isAbsolute()) {
      VirtualFile absoluteBasePath = LocalFileSystem.getInstance().findFileByIoFile(file);
      if (absoluteBasePath != null && absoluteBasePath.exists()) {
        return absoluteBasePath;
      }
    }
    if (basePath != null) {
      VirtualFile relativeBasePath = basePath.findFileByRelativePath(path);
      if (relativeBasePath != null && relativeBasePath.exists()) {
        return relativeBasePath;
      }
    }
    return null;
  }

  public static boolean isBasePathKey(@NotNull YAMLKeyValue keyValue) {
    return BASE_PATH_KEY.equals(keyValue.getKeyText());
  }

  @Nullable
  public VirtualFile getBasePath() {
    if (myBasePath == null) {
      myBasePath = Ref.create(calculateBasePath());
    }
    return myBasePath.get();
  }

  private VirtualFile calculateBasePath() {
    VirtualFile configDir = getConfigDir();
    if (myKeyValue == null) {
      return configDir;
    }
    if (myDocumentFragment == null) {
      return null;
    }
    String pathStr = myDocumentFragment.getDocument().getText(myDocumentFragment.getTextRange());
    VirtualFile basePath = findFile(getConfigDir(), pathStr);
    if (basePath != null && basePath.isDirectory()) {
      return basePath;
    }
    return null;
  }

  @Nullable
  private static Pair<YAMLKeyValue, DocumentFragment> extractBasePathPair(@NotNull YAMLDocument yamlDocument) {
    final Ref<Pair<YAMLKeyValue, DocumentFragment>> result = Ref.create(null);
    final YAMLValue value = yamlDocument.getTopLevelValue();
    if (value instanceof YAMLMapping) {
      for (YAMLKeyValue keyValue : ((YAMLMapping)value).getKeyValues()) {
        if (keyValue != null && isBasePathKey(keyValue) && result.isNull()) {
          DocumentFragment valueFragment = JstdConfigFileUtils.extractValueAsDocumentFragment(keyValue);
          result.set(Pair.create(keyValue, valueFragment));
        }
      }
    }
    return result.get();
  }

}
