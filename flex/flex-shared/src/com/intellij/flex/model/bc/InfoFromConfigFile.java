package com.intellij.flex.model.bc;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtilRt;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.util.JpsPathUtil;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class InfoFromConfigFile {

  public static InfoFromConfigFile DEFAULT = new InfoFromConfigFile(null, null, null, null, null);

  private static final Map<String, Pair<Long, InfoFromConfigFile>> ourCache = new THashMap<String, Pair<Long, InfoFromConfigFile>>();

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
      return FileUtil.getNameWithoutExtension(FileUtil.getNameWithoutExtension(PathUtilRt.getFileName(mainClassPath)));
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
          return FileUtil.getNameWithoutExtension(relativePath).replace("/", ".");
        }
      }
    }
    return FileUtil.getNameWithoutExtension(mainClassCanonicalPath);
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
    final Long cachedTimestamp = data == null ? null : data.first;

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      ourCache.remove(canonicalPath);

      String mainClassPath = null;
      String outputPath = null;
      String targetPlayer = null;

      try {
        final Document document = JDOMUtil.loadDocument(configFile);
        final Element rootElement = document.getRootElement();

        if (rootElement != null) {
          final Element fileSpecsElement = rootElement.getChild("file-specs", rootElement.getNamespace());
          mainClassPath = fileSpecsElement == null ? null
                                                   : fileSpecsElement.getChildTextNormalize("path-element", rootElement.getNamespace());
          outputPath = rootElement.getChildTextNormalize("output", rootElement.getNamespace());

          if (!FileUtil.isAbsolute(outputPath)) {
            try {
              outputPath = FileUtil.toSystemIndependentName(new File(configFile.getParent(), outputPath).getCanonicalPath());
            }
            catch (IOException e) {
              outputPath = FileUtil.toSystemIndependentName(new File(configFile.getParent(), outputPath).getAbsolutePath());
            }
          }

          targetPlayer = rootElement.getChildTextNormalize("target-player", rootElement.getNamespace());
        }
      }
      catch (IOException ignore) {/*ignore*/ }
      catch (JDOMException ignore) {/*ignore*/}

      final String outputFileName = outputPath == null ? null : PathUtilRt.getFileName(outputPath);
      final String outputFolderPath = outputPath == null ? null : PathUtilRt.getParentPath(outputPath);

      data = Pair.create(currentTimestamp,
                         new InfoFromConfigFile(configFile, mainClassPath, outputFileName, outputFolderPath, targetPlayer));
      ourCache.put(canonicalPath, data);
    }

    return data.second;
  }
}
