// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.sdk;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.ComponentSet;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PairConsumer;
import com.intellij.util.Processor;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlexSdkUtils {

  public static final String ADL_RELATIVE_PATH =
    File.separatorChar + "bin" + File.separatorChar + "adl" + (SystemInfo.isWindows ? ".exe" : "");

  static final String AIR_RUNTIME_RELATIVE_PATH =
    File.separatorChar + "runtimes" + File.separatorChar + "air" + File.separatorChar +
    (SystemInfo.isWindows ? "win" : (SystemInfo.isLinux ? "linux" : "mac"));

  private static final Pattern PLAYER_FOLDER_PATTERN = Pattern.compile("\\d{1,2}(\\.\\d{1,2})?");

  private FlexSdkUtils() {
  }

  public static void processPlayerglobalSwcFiles(final VirtualFile playerDir, final Processor<? super VirtualFile> processor) {
    VirtualFile playerglobalSwcFile;
    VirtualFile[] children = playerDir.getChildren();
    VirtualFile[] sorted = children.clone();
    Arrays.sort(sorted, Comparator.comparing(VirtualFile::getName));
    for (final VirtualFile subDir : sorted) {
      if (subDir.isDirectory() &&
          (playerglobalSwcFile = subDir.findChild("playerglobal.swc")) != null &&
          PLAYER_FOLDER_PATTERN.matcher(subDir.getName()).matches()) {
        if (!processor.process(playerglobalSwcFile)) {
          break;
        }
      }
    }
  }

  public static @Nullable String doReadFlexSdkVersion(final VirtualFile sdkRoot) {
    return doReadSdkVersion(sdkRoot, false);
  }

  public static @Nullable String doReadAirSdkVersion(final VirtualFile sdkRoot) {
    return doReadSdkVersion(sdkRoot, true);
  }

  private static @Nullable String doReadSdkVersion(final VirtualFile sdkRoot, final boolean airSdk) {
    if (sdkRoot == null) {
      return null;
    }
    final VirtualFile flexSdkDescriptionFile = sdkRoot.findChild(airSdk ? "air-sdk-description.xml" : "flex-sdk-description.xml");
    if (flexSdkDescriptionFile == null) {
      return null;
    }
    try {
      final String versionElement = airSdk ? "<air-sdk-description><version>" : "<flex-sdk-description><version>";
      final String buildElement = airSdk ? "<air-sdk-description><build>" : "<flex-sdk-description><build>";
      final Map<String, List<String>> versionInfo =
        FlexUtils.findXMLElements(flexSdkDescriptionFile.getInputStream(), Arrays.asList(versionElement, buildElement));
      final List<String> majorMinor = versionInfo.get(versionElement);
      final List<String> revision = versionInfo.get(buildElement);
      return majorMinor.isEmpty() ? null
                                  : majorMinor.get(0) + (revision.isEmpty() ? "" : ("." + revision.get(0)));
    }
    catch (IOException e) {
      return null;
    }
  }

  public static @Nullable Sdk createOrGetSdk(final SdkType sdkType, final String path) {
    // todo work with sdk modifiable model if Project Structure is open!
    final VirtualFile sdkHome = path == null ? null : LocalFileSystem.getInstance().findFileByPath(path);
    if (sdkHome == null) return null;
    final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
    for (final Sdk flexSdk : projectJdkTable.getSdksOfType(sdkType)) {
      final String existingHome = flexSdk.getHomePath();
      if (existingHome != null && sdkHome.getPath().equals(FileUtil.toSystemIndependentName(existingHome))) {
        if (sdkType instanceof FlexmojosSdkType) {
          final SdkAdditionalData data = flexSdk.getSdkAdditionalData();
          if (data == null || ((FlexmojosSdkAdditionalData)data).getFlexCompilerClasspath().isEmpty()) {
            sdkType.setupSdkPaths(flexSdk);
          }
        }
        return flexSdk;
      }
    }
    return createSdk(sdkType, sdkHome.getPath());
  }

  private static Sdk createSdk(final SdkType sdkType, final @NotNull String sdkHomePath) {
    return WriteAction.computeAndWait(() -> {
      final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
      final String sdkName = SdkConfigurationUtil.createUniqueSdkName(sdkType, sdkHomePath, projectJdkTable.getSdksOfType(sdkType));
      final Sdk sdk = projectJdkTable.createSdk(sdkName, sdkType);
      SdkModificator sdkModificator = sdk.getSdkModificator();
      sdkModificator.setVersionString("");
      sdkModificator.setHomePath(sdkHomePath);
      sdkModificator.commitChanges();
      sdkType.setupSdkPaths(sdk);
      projectJdkTable.addJdk(sdk);
      return sdk;
    });
  }

  public static int getFlexSdkRevision(final String sdkVersion) {
    // "4.5.0.17689"
    // "4.5.0 build 17689"
    try {
      final int index = Math.max(sdkVersion.lastIndexOf('.'), sdkVersion.lastIndexOf(' '));
      return Integer.parseInt(sdkVersion.substring(index + 1));
    }
    catch (NumberFormatException ignore) {/*ignore*/}

    return 0;
  }

  public static String getAdlPath(final @NotNull Sdk sdk) {
    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        return ((FlexmojosSdkAdditionalData)data).getAdlPath();
      }
    }
    return sdk.getHomePath() + ADL_RELATIVE_PATH;
  }

  /**
   * @return either path to a directory or to the zip file. It can't be used to construct command line. Use {@link #getAirRuntimeDirInfoForFlexmojosSdk(Sdk) instead}
   */
  public static String getAirRuntimePathForFlexmojosSdk(final @NotNull Sdk sdk) {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        return ((FlexmojosSdkAdditionalData)data).getAirRuntimePath();
      }
    }
    return sdk.getHomePath() + AIR_RUNTIME_RELATIVE_PATH;
  }

  public static @Nullable String getAirRuntimePath(final @NotNull Sdk sdk) {
    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        return ((FlexmojosSdkAdditionalData)data).getAirRuntimePath();
      }
    }
    else {
      return sdk.getHomePath() + AIR_RUNTIME_RELATIVE_PATH;
    }

    return null;
  }

  /**
   * This method unzips AIR Runtime to temporary directory if AIR Runtime is set as a zip file.<br>
   * <b>Caller is responsible to delete temporary files</b> when AIR application terminates.
   *
   * @return the first object is a directory containing AIR Runtime.<br>
   *         The second object is {@code Boolean.TRUE} if this directory is temporary (contains zip file content) and must be deleted when AIR application terminates.
   */
  public static Pair<VirtualFile, Boolean> getAirRuntimeDirInfoForFlexmojosSdk(final @NotNull Sdk sdk) throws IOException {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    final String airRuntimePath = getAirRuntimePathForFlexmojosSdk(sdk);
    final VirtualFile airRuntime = LocalFileSystem.getInstance().findFileByPath(airRuntimePath);
    if (airRuntime == null) {
      throw new IOException("Can't find AIR Runtime at " + airRuntimePath);
    }

    if (airRuntime.isDirectory()) {
      return Pair.create(airRuntime, Boolean.FALSE);
    }
    else {
      final String systemTempDirPath = FileUtil.getTempDirectory();
      final File systemTempDir = new File(systemTempDirPath);
      if (!systemTempDir.exists() || !systemTempDir.isDirectory()) {
        throw new IOException("Temp directory doesn't exist: " + systemTempDirPath);
      }

      final String tempDirPath = findUniqueTempDirName(systemTempDir);

      try {
        final VirtualFile tempDir = unzip(airRuntime.getPath(), tempDirPath);
        return Pair.create(tempDir, Boolean.TRUE);
      }
      catch (IOException e) {
        throw new IOException(
          MessageFormat.format("Can''t unzip file ''{0}'' to ''{1}'': {2}", FileUtil.toSystemDependentName(airRuntime.getPath()),
                               tempDirPath, e.getMessage()));
      }
    }
  }

  private static String findUniqueTempDirName(final File systemTempDir) {
    String tempDirName;
    final String unzipDirNameBase = "intellij_air_runtime_";
    final String[] children = systemTempDir.list();
    for (int i = 1; ; i++) {
      tempDirName = unzipDirNameBase + i;
      if (!ArrayUtil.contains(tempDirName, children)) break;
    }
    return systemTempDir.getPath() + File.separatorChar + tempDirName;
  }

  private static @NotNull VirtualFile unzip(final String zipFilePath, final String outputDirPath) throws IOException {
    final Ref<IOException> ioExceptionRef = new Ref<>();
    final VirtualFile dir = ApplicationManager.getApplication().runWriteAction((NullableComputable<VirtualFile>)() -> {
      try {
        ZipUtil.extract(new File(zipFilePath), new File(outputDirPath), null);
        final VirtualFile tempDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputDirPath);
        assert tempDir != null;
        return tempDir;
      }
      catch (IOException e) {
        ioExceptionRef.set(e);
      }
      return null;
    });

    if (!ioExceptionRef.isNull()) {
      throw ioExceptionRef.get();
    }
    else {
      assert dir != null;
      return dir;
    }
  }

  public static boolean isFlex2Sdk(final @Nullable Sdk flexSdk) {
    final String version = flexSdk == null ? null : flexSdk.getVersionString();
    return version != null && (version.startsWith("2.") || version.startsWith("3.0 Moxie"));
  }

  public static boolean isFlex3_0Sdk(final @Nullable Sdk flexSdk) {
    final String version = flexSdk == null ? null : flexSdk.getVersionString();
    return version != null && version.startsWith("3.0");
  }

  public static boolean isFlex4Sdk(final @Nullable Sdk flexSdk) {
    final String version = flexSdk == null ? null : flexSdk.getVersionString();
    return version != null && version.startsWith("4.");
  }

  /**
   * @param mainClass used in case of Flexmojos SDK, also used for ordinary Flex SDK if {@code jarName} is {@code null}
   * @param jarName   if not {@code null} - this parameter used in case of Flex SDK; always ignored in case of Flexmojos SDK
   */
  public static List<String> getCommandLineForSdkTool(final @NotNull Project project,
                                                      final @NotNull Sdk sdk,
                                                      final @Nullable String additionalClasspath,
                                                      final @NotNull String mainClass,
                                                      final @Nullable String jarName) {
    String javaHome = SystemProperties.getJavaHome();
    boolean customJavaHomeSet = false;
    String additionalJavaArgs = null;
    int heapSizeMbFromJvmConfig = 0;
    String classpath = additionalClasspath;

    final boolean isFlexmojos = sdk.getSdkType() == FlexmojosSdkType.getInstance();
    final FlexmojosSdkAdditionalData flexmojosSdkData = isFlexmojos ? (FlexmojosSdkAdditionalData)sdk.getSdkAdditionalData() : null;

    if (isFlexmojos) {
      classpath = (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) +
                  FileUtil.toSystemDependentName(StringUtil.join(flexmojosSdkData.getFlexCompilerClasspath(), File.pathSeparator));
    }
    else {
      FileInputStream inputStream = null;

      try {
        inputStream = new FileInputStream(FileUtil.toSystemDependentName(sdk.getHomePath() + "/bin/jvm.config"));

        final Properties properties = new Properties();
        properties.load(inputStream);

        final String configuredJavaHome = properties.getProperty("java.home");
        if (configuredJavaHome != null && !configuredJavaHome.trim().isEmpty()) {
          javaHome = configuredJavaHome;
          customJavaHomeSet = true;
        }

        final String javaArgs = properties.getProperty("java.args");
        if (javaArgs != null && !javaArgs.trim().isEmpty()) {
          additionalJavaArgs = javaArgs;
          final Matcher matcher = FlexCommonUtils.XMX_PATTERN.matcher(javaArgs);
          if (matcher.matches()) {
            try {
              heapSizeMbFromJvmConfig = Integer.parseInt(matcher.group(2));
            }
            catch (NumberFormatException e) {/*ignore*/}
          }
        }

        final String classpathFromJvmConfig = properties.getProperty("java.class.path");
        if (classpathFromJvmConfig != null && !classpathFromJvmConfig.trim().isEmpty()) {
          classpath = (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) + classpathFromJvmConfig;
        }
        //jvm.config also has properties which are not handled here: 'env' and 'java.library.path'; though not sure that there's any sense in them
      }
      catch (IOException e) {
        // not a big problem, will use default settings
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (IOException e1) {/*ignore*/}
        }
      }
    }

    final String javaExecutable = FileUtil.toSystemDependentName((javaHome + "/bin/java" + (SystemInfo.isWindows ? ".exe" : "")));
    final String applicationHomeParam =
      isFlexmojos ? null : ("-Dapplication.home=" + FileUtil.toSystemDependentName(sdk.getHomePath()));

    final String d32 = FlexCommonUtils.getD32IfNeeded(customJavaHomeSet, javaHome);

    final List<String> result = new ArrayList<>();

    result.add(javaExecutable);
    if (StringUtil.isNotEmpty(d32)) result.add(d32);
    if (StringUtil.isNotEmpty(applicationHomeParam)) result.add(applicationHomeParam);
    if (StringUtil.isNotEmpty(additionalJavaArgs)) result.addAll(StringUtil.split(additionalJavaArgs, " "));

    final String vmOptions = FlexCompilerProjectConfiguration.getInstance(project).VM_OPTIONS;
    if (StringUtil.isNotEmpty(vmOptions)) result.addAll(StringUtil.split(vmOptions, " "));

    if (additionalJavaArgs == null || !additionalJavaArgs.contains("file.encoding")) {
      result.add("-Dfile.encoding=" + FlexCommonUtils.SDK_TOOLS_ENCODING);
    }

    result.add("-Djava.awt.headless=true");
    result.add("-Duser.language=en");
    result.add("-Duser.region=en");

    final int heapSizeMb = FlexCompilerProjectConfiguration.getInstance(project).HEAP_SIZE_MB;
    if (heapSizeMb > heapSizeMbFromJvmConfig) {
      result.add("-Xmx" + heapSizeMb + "m");
    }

    if (StringUtil.isNotEmpty(classpath)) {
      result.add("-classpath");
      result.add(classpath);
    }

    if (isFlexmojos || jarName == null) {
      result.add(mainClass);
    }
    else {
      result.add("-jar");
      result.add(FileUtil.toSystemDependentName(sdk.getHomePath() + "/lib/" + jarName));
    }

    return result;
  }

  public static void openModuleConfigurable(final Module module) {
    final ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject());
    ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), projectStructureConfigurable, () -> projectStructureConfigurable
      .select(module.getName(), ClasspathEditor.getName(), true));
  }

  /**
   * @param processor (namespace, relative path with no leading slash)
   */
  public static void processStandardNamespaces(FlexBuildConfiguration bc, PairConsumer<? super String, ? super String> processor) {
    final Sdk sdk = bc.getSdk();
    if (bc.isPureAs() || sdk == null || sdk.getSdkType() == FlexmojosSdkType.getInstance()) return;

    if (StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") < 0) {
      processor.consume("http://www.adobe.com/2006/mxml", "frameworks/mxml-manifest.xml");
    }
    else {
      processor.consume("http://ns.adobe.com/mxml/2009", "frameworks/mxml-2009-manifest.xml");

      if (bc.getTargetPlatform() == TargetPlatform.Mobile ||
          bc.getDependencies().getComponentSet() == ComponentSet.SparkAndMx ||
          bc.getDependencies().getComponentSet() == ComponentSet.SparkOnly) {
        processor.consume("library://ns.adobe.com/flex/spark", "frameworks/spark-manifest.xml");
      }

      if (bc.getTargetPlatform() != TargetPlatform.Mobile) {
        if (bc.getDependencies().getComponentSet() == ComponentSet.SparkAndMx ||
            bc.getDependencies().getComponentSet() == ComponentSet.MxOnly) {
          processor.consume("library://ns.adobe.com/flex/mx", "frameworks/mx-manifest.xml");
        }

        processor.consume("http://www.adobe.com/2006/mxml", "frameworks/mxml-manifest.xml");
      }
    }
  }

  public static Sdk[] getAllSdks() {
    final FlexProjectConfigurationEditor currentEditor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    if (currentEditor == null) {
      return ProjectJdkTable.getInstance().getAllJdks();
    }
    else {
      final Collection<Sdk> sdks =
        ProjectStructureConfigurable.getInstance(currentEditor.getProject()).getProjectJdksModel().getProjectSdks().values();
      return sdks.toArray(new Sdk[0]);
    }
  }

  public static List<Sdk> getFlexAndFlexmojosSdks() {
    return ContainerUtil.filter(getAllSdks(),
                                sdk -> sdk.getSdkType() instanceof FlexSdkType2 || sdk.getSdkType() instanceof FlexmojosSdkType);
  }

  public static @Nullable Sdk findFlexOrFlexmojosSdk(final String name) {
    return ContainerUtil.find(getFlexAndFlexmojosSdks(), sdk -> name.equals(sdk.getName()));
  }

  public static List<Sdk> getFlexSdks() {
    return ContainerUtil.filter(getAllSdks(), sdk -> sdk.getSdkType() instanceof FlexSdkType2);
  }

  public static boolean isAirSdkWithoutFlex(final @Nullable Sdk sdk) {
    final String version = sdk == null ? null : sdk.getVersionString();
    return version != null && version.startsWith(FlexCommonUtils.AIR_SDK_VERSION_PREFIX);
  }

  /**
   * Returned string is equal to folder name in which playerglobal.swc resides
   */
  public static String getTargetPlayer(final @Nullable String playerVersionInAnyForm, final String sdkHome) {
    String targetPlayer = null;
    final String[] targetPlayers = FlexCommonUtils.getTargetPlayers(sdkHome);
    if (playerVersionInAnyForm != null) {
      final Trinity<String,String,String> majorMinorRevision = FlexCommonUtils.getMajorMinorRevisionVersion(playerVersionInAnyForm);
      if (ArrayUtil.contains(majorMinorRevision.first, targetPlayers)) {
        targetPlayer = majorMinorRevision.first;
      }
      else if (ArrayUtil.contains(majorMinorRevision.first + "." + majorMinorRevision.second, targetPlayers)) {
        targetPlayer = majorMinorRevision.first + "." + majorMinorRevision.second;
      }
    }

    return targetPlayer != null ? targetPlayer : FlexCommonUtils.getMaximumVersion(targetPlayers);
  }
}
