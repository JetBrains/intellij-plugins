// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.PathUtilRt;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class InfoFromConfigFile {
  public static final InfoFromConfigFile DEFAULT = new InfoFromConfigFile(null, null, null, null, null);

  private static final Map<String, Pair<Long, InfoFromConfigFile>> ourCache = new HashMap<>();

  private final @Nullable File myConfigFile;
  private final @Nullable String myMainClassPath;
  private boolean myMainClassInitialized = false;
  private @Nullable String myMainClass;
  private final @Nullable String myOutputFileName;
  private final @Nullable String myOutputFolderPath;
  private final @Nullable String myTargetPlayer;

  private InfoFromConfigFile(final @Nullable File configFile,
                             final @Nullable String mainClassPath,
                             final @Nullable String outputFileName,
                             final @Nullable String outputFolderPath,
                             final @Nullable String targetPlayer) {
    myConfigFile = configFile;
    myMainClassPath = mainClassPath;
    myOutputFileName = outputFileName;
    myOutputFolderPath = outputFolderPath;
    myTargetPlayer = targetPlayer;
  }

  @Nullable
  public String getMainClass(final JpsModule module) {
    if (!myMainClassInitialized && myConfigFile != null && myConfigFile.isFile()) {
      myMainClass = myMainClassPath == null ? null : getMainClassByPath(module, myMainClassPath, myConfigFile.getParent());
    }
    myMainClassInitialized = true;
    return myMainClass;
  }

  @Nullable
  public String getOutputFileName() {
    return myOutputFileName;
  }

  @Nullable
  public String getOutputFolderPath() {
    return myOutputFolderPath;
  }

  @Nullable
  public String getTargetPlayer() {
    return myTargetPlayer;
  }

  private static String getMainClassByPath(final JpsModule module, final String mainClassPath, final String baseDir) {
    if (mainClassPath.isEmpty()) return "unknown";

    File mainClassFile = new File(mainClassPath);
    if (!mainClassFile.isFile()) {
      mainClassFile = new File(baseDir + File.pathSeparator + mainClassPath);
    }
    if (!mainClassFile.isFile()) {
      return FileUtilRt.getNameWithoutExtension(FileUtilRt.getNameWithoutExtension(PathUtilRt.getFileName(mainClassPath)));
    }

    String mainClassCanonicalPath;
    try {
      mainClassCanonicalPath = FileUtil.toSystemIndependentName(mainClassFile.getCanonicalPath());
    }
    catch (IOException e) {
      mainClassCanonicalPath = FileUtil.toSystemIndependentName(mainClassFile.getPath());
    }

    for (JpsModuleSourceRoot sourceRoot : module.getSourceRoots()) {
      final String sourcePath = JpsPathUtil.urlToPath(sourceRoot.getUrl());
      if (FileUtil.isAncestor(sourcePath, mainClassCanonicalPath, true)) {
        final String relativePath = FileUtil.getRelativePath(sourcePath, mainClassCanonicalPath, '/');
        if (relativePath != null) {
          return FileUtilRt.getNameWithoutExtension(relativePath).replace("/", ".");
        }
      }
    }
    return FileUtilRt.getNameWithoutExtension(mainClassCanonicalPath);
  }

  @NotNull
  public static InfoFromConfigFile getInfoFromConfigFile(final String configFilePath) {
    final File configFile = configFilePath.isEmpty() ? null : new File(configFilePath);
    if (configFile == null || !configFile.isFile()) {
      ourCache.remove(configFilePath);
      return DEFAULT;
    }

    String canonicalPath;
    try {
      canonicalPath = configFile.getCanonicalPath();
    }
    catch (IOException e) {
      canonicalPath = configFile.getPath();
    }

    Pair<Long, InfoFromConfigFile> data = ourCache.get(canonicalPath);

    final Long currentTimestamp = configFile.lastModified();
    final Long cachedTimestamp = Pair.getFirst(data);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      ourCache.remove(canonicalPath);

      String mainClassPath = null;
      String outputPath = null;
      String targetPlayer = null;

      try {
        final Element rootElement = JDOMUtil.load(configFile);
        final Element fileSpecsElement = rootElement.getChild("file-specs", rootElement.getNamespace());
        mainClassPath = fileSpecsElement == null ? null
                                                 : fileSpecsElement.getChildText("path-element", rootElement.getNamespace());
        outputPath = rootElement.getChildText("output", rootElement.getNamespace());

        if (outputPath != null && !FileUtil.isAbsolute(outputPath)) {
          try {
            outputPath = FileUtil.toSystemIndependentName(new File(configFile.getParent(), outputPath).getCanonicalPath());
          }
          catch (IOException e) {
            outputPath = FileUtil.toSystemIndependentName(new File(configFile.getParent(), outputPath).getAbsolutePath());
          }
        }

        targetPlayer = rootElement.getChildText("target-player", rootElement.getNamespace());
      }
      catch (IOException | JDOMException ignore) {/*ignore*/ }

      final String outputFileName = outputPath == null ? null : PathUtilRt.getFileName(outputPath);
      final String outputFolderPath = outputPath == null ? null : PathUtilRt.getParentPath(outputPath);

      data = Pair.create(currentTimestamp,
                         new InfoFromConfigFile(configFile, mainClassPath, outputFileName, outputFolderPath, targetPlayer));
      ourCache.put(canonicalPath, data);
    }

    return data.second;
  }
}
