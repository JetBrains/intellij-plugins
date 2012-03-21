package com.google.jstestdriver.idea.execution;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.jstestdriver.JsTestDriverServer;
import com.google.jstestdriver.idea.TestRunner;
import com.google.jstestdriver.idea.config.JstdConfigFileType;
import com.google.jstestdriver.idea.execution.generator.JstdConfigGenerator;
import com.google.jstestdriver.idea.execution.settings.JstdConfigType;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.server.ui.ToolPanel;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.util.PathUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.google.common.collect.Lists.transform;
import static java.io.File.pathSeparator;

public class JstdClientCommandLineBuilder {

  private static final Logger log = Logger.getInstance(TestRunnerState.class.getCanonicalName());

  public static final JstdClientCommandLineBuilder INSTANCE = new JstdClientCommandLineBuilder();

  private static final Function<File, String> GET_ABSOLUTE_PATH = new Function<File, String>() {
    @Override
    public String apply(File file) {
      return file.getAbsolutePath();
    }
  };

  private JstdClientCommandLineBuilder() {
  }

  public static GeneralCommandLine buildCommandLine(JstdRunSettings runSettings,
                                                    int testResultPort,
                                                    List<VirtualFile> configVirtualFiles,
                                                    @Nullable String coverageFilePath) {
    final Map<TestRunner.ParameterKey, String> parameters = Maps.newHashMap();
    final String serverURL = runSettings.getServerType() == ServerType.INTERNAL ?
        "http://localhost:" + ToolPanel.serverPort :
        runSettings.getServerAddress();
    parameters.put(TestRunner.ParameterKey.SERVER_URL, serverURL);
    parameters.put(TestRunner.ParameterKey.PORT, String.valueOf(testResultPort));
    String joinedConfigPaths = joinConfigs(configVirtualFiles);
    parameters.put(TestRunner.ParameterKey.CONFIG_FILE, joinedConfigPaths);
    if (runSettings.getTestType() == TestType.TEST_CASE) {
      parameters.put(TestRunner.ParameterKey.TEST_CASE, runSettings.getTestCaseName());
    }
    if (runSettings.getTestType() == TestType.TEST_METHOD) {
      parameters.put(TestRunner.ParameterKey.TEST_CASE, runSettings.getTestCaseName());
      parameters.put(TestRunner.ParameterKey.TEST_METHOD, runSettings.getTestMethodName());
    }
    if (coverageFilePath != null) {
      parameters.put(TestRunner.ParameterKey.OUTPUT_COVERAGE_FILE, coverageFilePath);
    }
    return buildCommandLine(parameters);
  }

  private static GeneralCommandLine buildCommandLine(Map<TestRunner.ParameterKey, String> parameters) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    // uncomment this if you want to debug jsTestDriver code in the test-runner process
    //addParameter("-Xdebug");
    //addParameter("-Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=y");

    File file = new File(PathUtil.getJarPathForClass(JsTestDriverServer.class));
    commandLine.setWorkDirectory(file.getParentFile());

    commandLine.addParameter("-cp");
    commandLine.addParameter(buildClasspath());

    commandLine.addParameter(TestRunner.class.getName());
    for (Map.Entry<TestRunner.ParameterKey, String> param : parameters.entrySet()) {
      commandLine.addParameter("--" + param.getKey().name().toLowerCase() + "=" + param.getValue());
    }

    return commandLine;
  }

  private static String joinConfigs(List<VirtualFile> configs) {
    List<String> jstdConfigPaths = Lists.newArrayList();
    for (VirtualFile config : configs) {
      try {
        String encodedPath = URLEncoder.encode(config.getPath(), Charsets.UTF_8.name());
        jstdConfigPaths.add(encodedPath);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    return StringUtil.join(jstdConfigPaths, ",");
  }

  @NotNull
  public static List<VirtualFile> collectVirtualFiles(JstdRunSettings runSettings, Project project) {
    TestType testType = runSettings.getTestType();
    List<VirtualFile> res = Collections.emptyList();
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(runSettings.getDirectory()));
      if (virtualFile != null) {
        res = collectJstdConfigFilesInDirectory(project, virtualFile);
      }
    } else {
      File configFile = extractConfigFile(project, runSettings);
      VirtualFile configVirtualFile = null;
      if (configFile != null) {
        configVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(configFile);
      }
      if (configVirtualFile != null) {
        res = Collections.singletonList(configVirtualFile);
      }
    }
    return res;
  }

  private static File extractConfigFile(Project project, JstdRunSettings runSettings) {
    if (runSettings.getTestType() == TestType.CONFIG_FILE || runSettings.getConfigType() == JstdConfigType.FILE_PATH) {
      return new File(runSettings.getConfigFile());
    }
    try {
      return JstdConfigGenerator.INSTANCE.generateTempConfig(project, new File(runSettings.getJsFilePath()));
    } catch (IOException ignored) {
    }
    return null;
  }

  @NotNull
  public static List<VirtualFile> collectJstdConfigFilesInDirectory(@NotNull Project project,
                                                                    @NotNull VirtualFile directory) {
    GlobalSearchScope directorySearchScope = buildDirectorySearchScrope(project, directory);
    if (directorySearchScope == null) {
      return Collections.emptyList();
    }
    Collection<VirtualFile> configs = FileTypeIndex.getFiles(JstdConfigFileType.INSTANCE, directorySearchScope);
    return Lists.newArrayList(configs);
  }

  @Nullable
  private static GlobalSearchScope buildDirectorySearchScrope(@NotNull Project project,
                                                              @NotNull VirtualFile directory) {
    final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(directory);
    if (module == null) {
      return null;
    }
    GlobalSearchScope directorySearchScope = GlobalSearchScopes.directoryScope(project, directory, true);
    return module.getModuleContentWithDependenciesScope().intersectWith(directorySearchScope);
  }

  public static boolean areJstdConfigFilesInDirectory(@NotNull Project project, @NotNull VirtualFile directory) {
    GlobalSearchScope directorySearchScope = buildDirectorySearchScrope(project, directory);
    if (directorySearchScope == null) {
      return false;
    }
    FileBasedIndex index = FileBasedIndex.getInstance();
    final Ref<Boolean> jstdConfigFound = Ref.create(false);
    index.processValues(FileTypeIndex.NAME, JstdConfigFileType.INSTANCE, null, new FileBasedIndex.ValueProcessor<Void>() {
      @Override
      public boolean process(final VirtualFile file, final Void value) {
        jstdConfigFound.set(true);
        return false;
      }
    }, directorySearchScope);
    return jstdConfigFound.get();
  }

  private static String buildClasspath() {
    Set<String> classpath = Sets.newHashSet();

    List<File> files = getClasspath(TestRunner.class);
    classpath.addAll(transform(files, GET_ABSOLUTE_PATH));

    return Joiner.on(pathSeparator).join(classpath);
  }

  private static List<File> getClasspath(Class<?>... classList) {
    List<File> classpath = Lists.newArrayList();
    for (Class<?> clazz : classList) {
      String path = PathUtil.getJarPathForClass(clazz);
      File file = new File(path);
      if (!file.exists()) {
        log.warn("Can't find path for " + clazz.getName());
      } else {
        classpath.add(file.getAbsoluteFile());
      }
    }
    return classpath;
  }

}
