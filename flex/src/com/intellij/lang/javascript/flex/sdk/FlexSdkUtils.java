package com.intellij.lang.javascript.flex.sdk;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLineStateUtil;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.lang.javascript.flex.FlexUtils;
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
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
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

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexSdkUtils {
  public static final String SDK_TOOLS_ENCODING = "UTF-8";

  public static final String ADL_RELATIVE_PATH =
    File.separatorChar + "bin" + File.separatorChar + "adl" + (SystemInfo.isWindows ? ".exe" : "");

  static final String AIR_RUNTIME_RELATIVE_PATH =
    File.separatorChar + "runtimes" + File.separatorChar + "air" + File.separatorChar +
    (SystemInfo.isWindows ? "win" : (SystemInfo.isLinux ? "linux" : "mac"));

  private static final Pattern XMX_PATTERN = Pattern.compile("(.* )?-Xmx([0-9]+)[mM]( .*)?");
  private static final Pattern PLAYER_FOLDER_PATTERN = Pattern.compile("\\d{1,2}(\\.\\d{1,2})?");

  private FlexSdkUtils() {
  }

  public static void processPlayerglobalSwcFiles(final VirtualFile playerDir, final Processor<VirtualFile> processor) {
    VirtualFile playerglobalSwcFile;
    VirtualFile[] children = playerDir.getChildren();
    VirtualFile[] sorted = Arrays.copyOf(children, children.length);
    Arrays.sort(sorted, new Comparator<VirtualFile>() {
      @Override
      public int compare(final VirtualFile o1, final VirtualFile o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
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
      final List<String> majorMinor = versionInfo.get(versionElement);
      final List<String> revision = versionInfo.get(buildElement);
      return majorMinor.isEmpty() ? null
                                  : majorMinor.get(0) + (revision.isEmpty() ? "" : ("." + revision.get(0)));
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
        final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
        final String sdkName = SdkConfigurationUtil.createUniqueSdkName(sdkType, sdkHomePath, projectJdkTable.getSdksOfType(sdkType));
        final Sdk sdk = PeerFactory.getInstance().createProjectJdk(sdkName, "", sdkHomePath, sdkType);
        sdkType.setupSdkPaths(sdk);
        projectJdkTable.addJdk(sdk);
        return sdk;
      }
    });
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

  /**
   * @param mainClass used in case of Flexmojos SDK, also used for ordinary Flex SDK if <code>jarName</code> is <code>null</code>
   * @param jarName   if not <code>null</code> - this parameter used in case of Flex SDK; always ignored in case of Flexmojos SDK
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
      classpath =
        (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) +
        FileUtil.toSystemDependentName(StringUtil.join(flexmojosSdkData.getFlexCompilerClasspath(), File.pathSeparator));
    }
    else {
      FileInputStream inputStream = null;

      try {
        inputStream = new FileInputStream(FileUtil.toSystemDependentName(sdk.getHomePath() + "/bin/jvm.config"));

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
      isFlexmojos ? null : ("-Dapplication.home=" + FileUtil.toSystemDependentName(sdk.getHomePath()));

    final String d32 = (!customJavaHomeSet && SystemInfo.isMac && is64BitJava(javaExecutable)) ? "-d32" : null;

    final List<String> result = new ArrayList<String>();

    result.add(javaExecutable);
    if (StringUtil.isNotEmpty(d32)) result.add(d32);
    if (StringUtil.isNotEmpty(applicationHomeParam)) result.add(applicationHomeParam);
    if (StringUtil.isNotEmpty(additionalJavaArgs)) result.addAll(StringUtil.split(additionalJavaArgs, " "));

    final String vmOptions = FlexCompilerProjectConfiguration.getInstance(project).VM_OPTIONS;
    if (StringUtil.isNotEmpty(vmOptions)) result.addAll(StringUtil.split(vmOptions, " "));

    if (additionalJavaArgs == null || !additionalJavaArgs.contains("file.encoding")) {
      result.add("-Dfile.encoding=" + SDK_TOOLS_ENCODING);
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

  public static InputStreamReader createInputStreamReader(final InputStream inputStream) {
    try {
      return new InputStreamReader(inputStream, SDK_TOOLS_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      return new InputStreamReader(inputStream);
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
   * @param processor (namespace, relative path with no leading slash)
   */
  public static void processStandardNamespaces(FlexIdeBuildConfiguration bc, PairConsumer<String, String> processor) {
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

  @Nullable
  public static Sdk findFlexOrFlexmojosSdk(final String name) {
    return ContainerUtil.find(getFlexAndFlexmojosSdks(), new Condition<Sdk>() {
      public boolean value(final Sdk sdk) {
        return name.equals(sdk.getName());
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
