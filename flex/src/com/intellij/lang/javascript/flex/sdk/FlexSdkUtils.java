package com.intellij.lang.javascript.flex.sdk;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLineStateUtil;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.ComponentSet;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
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

public class FlexSdkUtils {

  static final String EXTERNAL_LIBRARY_PATH_ELEMENT = "<flex-config><compiler><external-library-path><path-element>";
  static final String LIBRARY_PATH_ELEMENT = "<flex-config><compiler><library-path><path-element>";
  static final String THEME_FILE_NAME = "<flex-config><compiler><theme><filename>";
  static final String LOCALE_ELEMENT = "<flex-config><compiler><locale><locale-element>";
  public static final String TARGET_PLAYER_ELEMENT = "<flex-config><target-player>";
  public static final String FILE_SPEC_ELEMENT = "<flex-config><file-specs><path-element>";
  public static final String OUTPUT_ELEMENT = "<flex-config><output>";

  static final String TARGET_PLAYER_MAJOR_VERSION_TOKEN = "{targetPlayerMajorVersion}";
  static final String TARGET_PLAYER_MINOR_VERSION_TOKEN = "{targetPlayerMinorVersion}";
  static final String LOCALE_TOKEN = "{locale}";
  public static final String ADL_RELATIVE_PATH =
    File.separatorChar + "bin" + File.separatorChar + "adl" + (SystemInfo.isWindows ? ".exe" : "");
  static final String AIR_RUNTIME_RELATIVE_PATH =
    File.separatorChar + "runtimes" + File.separatorChar + "air" + File.separatorChar +
    (SystemInfo.isWindows ? "win" : (SystemInfo.isLinux ? "linux" : "mac"));

  private static final Pattern XMX_PATTERN = Pattern.compile("(.* )?-Xmx([0-9]+)[mM]( .*)?");
  private static final Pattern PLAYER_FOLDER_PATTERN = Pattern.compile("\\d{1,2}(\\.\\d{1,2})?");

  private FlexSdkUtils() {
  }

  static void setupSdkPaths(final Sdk sdk) {
    SdkModificator modificator = sdk.getSdkModificator();
    setupSdkPaths(sdk.getHomeDirectory(), sdk.getSdkType(), modificator);
    modificator.commitChanges();
  }

  /**
   * @param sdkType if <code>null</code> then all SWCs from folders 'libs', 'libs/air', 'libs/mx' and 'libs/mobile' are added
   *                and also all available versions of playerglobal.swc files
   */
  public static void setupSdkPaths(final VirtualFile sdkRoot, @Nullable final SdkType sdkType, final SdkModificator sdkModificator) {
    if (sdkRoot == null || !sdkRoot.isValid()) {
      return;
    }
    PropertiesComponent.getInstance().setValue(FlexSdkType2.LAST_SELECTED_FLEX_SDK_HOME_KEY, sdkRoot.getPath());
    sdkRoot.refresh(false, true);


    sdkModificator.setVersionString(readFlexSdkVersion(sdkRoot));

    if (sdkType == null) {
      final VirtualFile playerDir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          final VirtualFile libsDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkRoot.getPath() + "/frameworks/libs");
          if (libsDir != null && libsDir.isDirectory()) {
            libsDir.refresh(false, true);
            return libsDir.findChild("player");
          }
          return null;
        }
      });

      if (playerDir != null) {
        processPlayerglobalSwcFiles(playerDir, new Processor<VirtualFile>() {
          public boolean process(final VirtualFile playerglobalSwcFile) {
            addSwcRoot(sdkModificator, playerglobalSwcFile);
            return true;
          }
        });

        final VirtualFile baseDir = playerDir.getParent().getParent();
        // let global lib be in the beginning of the list
        addSwcRoots(sdkModificator, baseDir, Collections.singletonList("libs/air/airglobal.swc"), false);
        addSwcRoots(sdkModificator, baseDir, Arrays.asList("libs", "libs/mx", "libs/air", "libs/mobile", "themes/Mobile"), true);
      }
    }
    else {
      final String configFileRelativePath = getBaseConfigFileRelPath((IFlexSdkType)sdkType);

      final VirtualFile configFile = sdkRoot.findFileByRelativePath(configFileRelativePath);
      if (configFile == null) {
        return;
      }
      try {
        setupSdkLibraries(sdkModificator, configFile);
      }
      catch (IOException e) {
        // ignore
      }
    }

    final VirtualFile projectsDir = VfsUtil.findRelativeFile("frameworks/projects", sdkRoot);
    if (projectsDir != null && projectsDir.isDirectory()) {
      findSourceRoots(projectsDir, sdkModificator);
    }
  }

  public static void processPlayerglobalSwcFiles(final VirtualFile playerDir, final Processor<VirtualFile> processor) {
    VirtualFile playerglobalSwcFile;
    for (final VirtualFile subDir : playerDir.getChildren()) {
      if (subDir.isDirectory() &&
          (playerglobalSwcFile = subDir.findChild("playerglobal.swc")) != null &&
          PLAYER_FOLDER_PATTERN.matcher(subDir.getName()).matches()) {
        if (!processor.process(playerglobalSwcFile)) {
          break;
        }
      }
    }
  }

  private static void findSourceRoots(final VirtualFile dir, final SdkModificator sdkModificator) {
    for (final VirtualFile child : dir.getChildren()) {
      if (child.isDirectory()) {
        if (child.getName().equals("src")) {
          sdkModificator.addRoot(child, OrderRootType.SOURCES);
        }
        else {
          findSourceRoots(child, sdkModificator);
        }
      }
    }
  }

  /**
   * @param configXmlFile <code>&lt;Flex SDK root&gt;/frameworks/flex-config.xml</code> or <code>&lt;Flex SDK root&gt;/frameworks/air-config.xml</code>
   */
  private static void setupSdkLibraries(final @NotNull SdkModificator sdkModificator, final @NotNull VirtualFile configXmlFile)
    throws IOException {
    final VirtualFile baseDir = configXmlFile.getParent();
    assert baseDir != null;
    final List<String> xmlElements =
      Arrays.asList(EXTERNAL_LIBRARY_PATH_ELEMENT, LIBRARY_PATH_ELEMENT, THEME_FILE_NAME, LOCALE_ELEMENT, TARGET_PLAYER_ELEMENT);
    final Map<String, List<String>> infoFromConfigXml = FlexUtils.findXMLElements(configXmlFile.getInputStream(), xmlElements);

    final List<String> libRelativePaths = getLibsRelativePaths(infoFromConfigXml);

    addSwcRoots(sdkModificator, baseDir, libRelativePaths, false);
  }

  private static void addSwcRoots(final SdkModificator sdkModificator,
                                  final VirtualFile baseDir,
                                  final List<String> libRelativePaths,
                                  final boolean skipAirglobalSwc) {
    for (String libRelativePath : libRelativePaths) {
      final VirtualFile libFileOrDir = baseDir.findFileByRelativePath(libRelativePath);
      if (libFileOrDir != null) {
        if (libFileOrDir.isDirectory()) {
          for (final VirtualFile libCandidate : libFileOrDir.getChildren()) {
            if (!libCandidate.isDirectory() && "swc".equalsIgnoreCase(libCandidate.getExtension())) {
              if (!skipAirglobalSwc || !libCandidate.getPath().endsWith("frameworks/libs/air/airglobal.swc")) {
                addSwcRoot(sdkModificator, libCandidate);
              }
            }
          }
        }
        else if ("swc".equalsIgnoreCase(libFileOrDir.getExtension())) {
          if (!skipAirglobalSwc || !libFileOrDir.getPath().endsWith("frameworks/libs/air/airglobal.swc")) {
            addSwcRoot(sdkModificator, libFileOrDir);
          }
        }
      }
    }
  }

  /**
   * Looks for {locale}, {targetPlayerMajorVersion} and {targetPlayerMinorVersion} information in <code>infoFromConfigXml</code>,
   * substitutes these tokens in &lt;external-library-path&gt; and &lt;library-path&gt; entries
   *
   * @return list of relative paths to Flex SDK libraries (may contain directories and particular swc files)
   */
  private static List<String> getLibsRelativePaths(final Map<String, List<String>> infoFromConfigXml) {
    final List<String> result = new ArrayList<String>();
    final List<String> locales = infoFromConfigXml.get(LOCALE_ELEMENT);
    final List<String> targetPlayers = infoFromConfigXml.get(TARGET_PLAYER_ELEMENT); // contains one element

    String targetPlayerMajorVersion = TARGET_PLAYER_MAJOR_VERSION_TOKEN;
    String targetPlayerMinorVersion = TARGET_PLAYER_MINOR_VERSION_TOKEN;
    if (!targetPlayers.isEmpty()) {
      final String targetPlayer = targetPlayers.iterator().next();
      final Pair<String, String> majorMinor = TargetPlayerUtils.getPlayerMajorMinorVersion(targetPlayer);
      targetPlayerMajorVersion = majorMinor.first;
      targetPlayerMinorVersion = majorMinor.second;
    }

    for (final Map.Entry<String, List<String>> entry : infoFromConfigXml.entrySet()) {
      final String key = entry.getKey();
      if (key.equals(EXTERNAL_LIBRARY_PATH_ELEMENT) || key.equals(LIBRARY_PATH_ELEMENT) || key.equals(THEME_FILE_NAME)) {
        for (String libPath : entry.getValue()) {
          libPath = libPath.replace(TARGET_PLAYER_MAJOR_VERSION_TOKEN, targetPlayerMajorVersion);
          libPath = libPath.replace(TARGET_PLAYER_MINOR_VERSION_TOKEN, targetPlayerMinorVersion);
          if (libPath.contains(LOCALE_TOKEN)) {
            for (final String locale : locales) {
              result.add(libPath.replace(LOCALE_TOKEN, locale));
            }
          }
          else {
            result.add(libPath);
          }
        }
      }
    }

    return result;
  }

  private static void addSwcRoot(final @NotNull SdkModificator sdkModificator, final @NotNull VirtualFile swcFile) {
    final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(swcFile);
    if (jarRoot != null) {
      sdkModificator.addRoot(jarRoot, OrderRootType.CLASSES);
    }
  }

  public static boolean isValidSdkRoot(final IFlexSdkType sdkType, final VirtualFile sdkRoot) {
    return sdkRoot != null && VfsUtil.findRelativeFile(getBaseConfigFileRelPath(sdkType), sdkRoot) != null;
  }

  @NotNull
  public static String readFlexSdkVersion(final VirtualFile flexSdkRoot) {
    return StringUtil.notNullize(doReadFlexSdkVersion(flexSdkRoot), FlexBundle.message("flex.sdk.version.unknown"));
  }
  
  @Nullable
  public static String doReadFlexSdkVersion(final VirtualFile flexSdkRoot) {
    if (flexSdkRoot == null) {
      return null;
    }
    final VirtualFile flexSdkDescriptionFile = flexSdkRoot.findChild("flex-sdk-description.xml");
    if (flexSdkDescriptionFile == null) {
      return null;
    }
    try {
      final String versionElement = "<flex-sdk-description><version>";
      final String buildElement = "<flex-sdk-description><build>";
      final Map<String, List<String>> versionInfo =
        FlexUtils.findXMLElements(flexSdkDescriptionFile.getInputStream(), Arrays.asList(versionElement, buildElement));
      return (versionInfo.get(versionElement).isEmpty()
              ? null
              : versionInfo.get(versionElement).get(0)) +
             (versionInfo.get(buildElement).isEmpty() ? "" : ("." + versionInfo.get(buildElement).get(0)));
    }
    catch (IOException e) {
      return null;
    }
  }

  @Nullable
  public static Sdk createOrGetSdk(final SdkType sdkType, final String path) {
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
    if (ApplicationManager.getApplication().isDispatchThread() || ApplicationManager.getApplication().isUnitTestMode()) {
      return doCreateSdk(sdkType, sdkHomePath);
    }
    else {
      final Ref<Sdk> sdkRef = new Ref<Sdk>();
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          sdkRef.set(doCreateSdk(sdkType, sdkHomePath));
        }
      }, ModalityState.defaultModalityState());
      return sdkRef.get();
    }
  }

  private static Sdk doCreateSdk(final SdkType sdkType, final @NotNull String sdkHomePath) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<Sdk>() {
      public Sdk compute() {
        final Sdk sdk = PeerFactory.getInstance().createProjectJdk(sdkType.suggestSdkName(null, sdkHomePath), "", sdkHomePath, sdkType);
        sdkType.setupSdkPaths(sdk);
        final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
        projectJdkTable.addJdk(sdk);
        return sdk;
      }
    });
  }

  public static boolean hasDependencyOnAir(final @NotNull Module module) {
    return hasDependencyOn(module, AirSdkType.getInstance(), "Maven: com.adobe.flex.framework:airframework:swc:");
  }

  public static boolean hasDependencyOnAirMobile(final @NotNull Module module) {
    return hasDependencyOn(module, AirMobileSdkType.getInstance(), "Maven: com.adobe.flex.framework:mobilecomponents:swc:");
  }

  private static boolean hasDependencyOn(final @NotNull Module module,
                                         final @NotNull SdkType sdkType,
                                         final @NotNull String mavenLibraryNameStart) {
    final Sdk sdk = FlexUtils.getSdkForActiveBC(module);
    if (sdk != null) {
      if (sdk.getSdkType() instanceof FlexmojosSdkType) {
        final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        for (OrderEntry orderEntry : rootManager.getOrderEntries()) {
          if (orderEntry instanceof LibraryOrderEntry) {
            final String libName = ((LibraryOrderEntry)orderEntry).getLibraryName();
            if (libName != null && (libName.startsWith(mavenLibraryNameStart))) {
              return true;
            }
          }
        }
      }
      else {
        return sdk.getSdkType() == sdkType;
      }
    }
    return false;
  }

  public static String getAirVersion(final Sdk sdk) {
    final String version = sdk == null ? null : sdk.getVersionString();
    if (version != null) {
      return getAirVersion(version);
    }

    return "1.5";
  }

  public static String getAirVersion(final String flexVersion) {
    // todo store adt -version

    if (flexVersion.startsWith("4.")) {
      if (flexVersion.startsWith("4.0")) {
        return "1.5.3";
      }
      if (flexVersion.startsWith("4.1")) {
        return "2.0";
      }
      if (flexVersion.startsWith("4.5")) {
        return "2.6";
      }

      return "3.1";
    }

    if (flexVersion.startsWith("3.")) {
      if (flexVersion.startsWith("3.0")) {
        return "1.0";
      }
      if (flexVersion.startsWith("3.1")) {
        return "1.1";
      }
      if (flexVersion.startsWith("3.2")) {
        return "1.5";
      }
      if (flexVersion.startsWith("3.3")) {
        return "1.5";
      }
      if (flexVersion.startsWith("3.4")) {
        return "1.5.2";
      }

      return "1.5.3";
    }

    return "3.1";
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
   * @return either path to a directory or to the zip file. It can't be used to construct command line. Use {@link #getAirRuntimeDirInfoForFlexmojosSdk(com.intellij.openapi.projectRoots.Sdk) instead}
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

  @Nullable
  public static String getAirRuntimePath(final @NotNull Sdk sdk) {
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
   *         The second object is <code>Boolean.TRUE</code> if this directory is temporary (contains zip file content) and must be deleted when AIR application terminates.
   */
  public static Pair<VirtualFile, Boolean> getAirRuntimeDirInfoForFlexmojosSdk(final @NotNull Sdk sdk) throws IOException {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    final String airRuntimePath = getAirRuntimePathForFlexmojosSdk(sdk);
    final VirtualFile airRuntime = LocalFileSystem.getInstance().findFileByPath(airRuntimePath);
    if (airRuntime == null) {
      throw new IOException("Can't find AIR Runtime at " + airRuntimePath);
    }

    if (airRuntime.isDirectory()) {
      return new Pair<VirtualFile, Boolean>(airRuntime, Boolean.FALSE);
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
        return new Pair<VirtualFile, Boolean>(tempDir, Boolean.TRUE);
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

  @NotNull
  private static VirtualFile unzip(final String zipFilePath, final String outputDirPath) throws IOException {
    final Ref<IOException> ioExceptionRef = new Ref<IOException>();
    final VirtualFile dir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
      public VirtualFile compute() {
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
      }
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

  public static List<String> getCommandLineForSdkTool(final @NotNull Project project,
                                                      final @NotNull Sdk sdk,
                                                      final @Nullable String additionalClasspath,
                                                      final @NotNull String mainClass,
                                                      final @Nullable String jarName) {
    final FlexmojosSdkAdditionalData data = sdk.getSdkType() instanceof FlexmojosSdkType
                                            ? (FlexmojosSdkAdditionalData)sdk.getSdkAdditionalData() : null;
    return getCommandLineForSdkTool(project, sdk.getHomePath(), data, additionalClasspath, mainClass, jarName);
  }

  /**
   * @param mainClass used in case of Flexmojos SDK, also used for ordinary Flex/AIR SDK if <code>jarName</code> is <code>null</code>
   * @param jarName   if not <code>null</code> - this parameter used in case of Flex/AIR SDK; always ignored in case of Flexmojos SDK
   */
  public static List<String> getCommandLineForSdkTool(final @NotNull Project project,
                                                      final @NotNull String sdkHome,
                                                      final @Nullable FlexmojosSdkAdditionalData flexmojosSdkAdditionalData,
                                                      final @Nullable String additionalClasspath,
                                                      final @NotNull String mainClass,
                                                      final @Nullable String jarName) {
    final boolean isFlexmojos = flexmojosSdkAdditionalData != null;
    String javaHome = SystemProperties.getJavaHome();
    boolean customJavaHomeSet = false;
    String additionalJavaArgs = null;
    int heapSizeMbFromJvmConfig = 0;
    String classpath = additionalClasspath;

    if (isFlexmojos) {
      classpath =
        (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) +
        FileUtil.toSystemDependentName(StringUtil.join(flexmojosSdkAdditionalData.getFlexCompilerClasspath(), File.pathSeparator));
    }
    else {
      FileInputStream inputStream = null;

      try {
        inputStream = new FileInputStream(FileUtil.toSystemDependentName(sdkHome + "/bin/jvm.config"));

        final Properties properties = new Properties();
        properties.load(inputStream);

        final String configuredJavaHome = properties.getProperty("java.home");
        if (configuredJavaHome != null && configuredJavaHome.trim().length() > 0) {
          javaHome = configuredJavaHome;
          customJavaHomeSet = true;
        }

        final String javaArgs = properties.getProperty("java.args");
        if (javaArgs != null && javaArgs.trim().length() > 0) {
          additionalJavaArgs = javaArgs;
          final Matcher matcher = XMX_PATTERN.matcher(javaArgs);
          if (matcher.matches()) {
            try {
              heapSizeMbFromJvmConfig = Integer.parseInt(matcher.group(2));
            }
            catch (NumberFormatException e) {/*ignore*/}
          }
        }

        final String classpathFromJvmConfig = properties.getProperty("java.class.path");
        if (classpathFromJvmConfig != null && classpathFromJvmConfig.trim().length() > 0) {
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
      isFlexmojos ? null : ("-Dapplication.home=" + FileUtil.toSystemDependentName(sdkHome));

    final String d32 = (!customJavaHomeSet && SystemInfo.isMac && is64BitJava(javaExecutable)) ? "-d32" : null;

    final List<String> result = new ArrayList<String>();

    result.add(javaExecutable);
    if (StringUtil.isNotEmpty(d32)) result.add(d32);
    if (StringUtil.isNotEmpty(applicationHomeParam)) result.add(applicationHomeParam);
    if (StringUtil.isNotEmpty(additionalJavaArgs)) result.addAll(StringUtil.split(additionalJavaArgs, " "));

    final String vmOptions = FlexCompilerProjectConfiguration.getInstance(project).VM_OPTIONS;
    if (StringUtil.isNotEmpty(vmOptions)) result.addAll(StringUtil.split(vmOptions, " "));

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
      result.add(FileUtil.toSystemDependentName(sdkHome + "/lib/" + jarName));
    }

    return result;
  }

  private static boolean is64BitJava(final String javaExecutable) {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(javaExecutable);
    commandLine.addParameter("-version");
    try {
      final Ref<Boolean> is64Bit = new Ref<Boolean>(false);
      final OSProcessHandler handler = JavaCommandLineStateUtil.startProcess(commandLine);
      handler.addProcessListener(new ProcessAdapter() {
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          final String text = event.getText();
          if (!text.contains(javaExecutable) &&
              text.contains("64-Bit")) { // first condition is to make sure that "64-Bit" is not in java[.exe] path which is also printed
            is64Bit.set(true);
          }
        }
      });
      handler.startNotify();
      handler.waitFor(3000);
      if (!handler.isProcessTerminated()) {
        handler.destroyProcess();
      }
      return is64Bit.get();
    }
    catch (ExecutionException e) {/*ignore*/}

    return false;
  }

  public static String getBaseConfigFileRelPath(final IFlexSdkType sdkType) {
    return "frameworks/" + getBaseConfigFileName(sdkType);
  }

  public static String getBaseConfigFileName(final IFlexSdkType sdkType) {
    switch (sdkType.getSubtype()) {
      case Flex:
        return "flex-config.xml";
      case AIR:
        return "air-config.xml";
      case AIRMobile:
        return "airmobile-config.xml";
      case Flexmojos:
      default:
        assert false : sdkType.getSubtype();
        return null;
    }
  }

  public static void openModuleConfigurable(final Module module) {
    final ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject());
    ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), projectStructureConfigurable, new Runnable() {
      public void run() {
        projectStructureConfigurable.select(module.getName(), ClasspathEditor.NAME, true);
      }
    });
  }

  /**
   * @param sdkVersion
   * @param bc
   * @param processor  (namespace, relative path with no leading slash)
   */
  public static void processStandardNamespaces(String sdkVersion, FlexIdeBuildConfiguration bc, PairConsumer<String, String> processor) {
    if (bc.isPureAs()) return;

    if (StringUtil.compareVersionNumbers(sdkVersion, "4") < 0) {
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
      return sdks.toArray(new Sdk[sdks.size()]);
    }
  }

  public static List<Sdk> getFlexAndFlexmojosSdks() {
    return ContainerUtil.filter(getAllSdks(), new Condition<Sdk>() {
      public boolean value(final Sdk sdk) {
        return sdk.getSdkType() instanceof FlexSdkType2 || sdk.getSdkType() instanceof FlexmojosSdkType;
      }
    });
  }

  public static List<Sdk> getFlexSdks() {
    return ContainerUtil.filter(getAllSdks(), new Condition<Sdk>() {
      public boolean value(final Sdk sdk) {
        return sdk.getSdkType() instanceof FlexSdkType2;
      }
    });
  }
}
