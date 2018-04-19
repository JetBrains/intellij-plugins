package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.testing.JsTestRunConfigurationProducer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class KarmaRunSettingsSerializationUtil {

  private static final String CONFIG_FILE = "config-file";
  private static final String KARMA_PACKAGE_DIR = "karma-package-dir";
  private static final String WORKING_DIRECTORY = "working-directory";
  private static final String BROWSERS = "browsers";
  private static final String NODE_INTERPRETER = "node-interpreter";
  private static final String NODE_OPTIONS = "node-options";
  private static final String SCOPE_KIND = "scope-kind";
  private static final String TEST_FILE_PATH = "test-file-path";
  private static final String TEST_NAMES = "test-names";
  private static final String TEST_NAME = "test-name";

  private KarmaRunSettingsSerializationUtil() {}

  public static KarmaRunSettings readXml(@NotNull Element element,
                                         @NotNull Project project,
                                         boolean templateRunConfiguration) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();

    String configPath = StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, CONFIG_FILE));
    builder.setConfigPath(configPath);
    builder.setBrowsers(JDOMExternalizerUtil.readCustomField(element, BROWSERS));

    String karmaPackageDir = JDOMExternalizerUtil.readCustomField(element, KARMA_PACKAGE_DIR);
    if (karmaPackageDir != null) {
      builder.setKarmaPackage(new NodePackage(karmaPackageDir));
    }

    String workingDirPath = JDOMExternalizerUtil.readCustomField(element, WORKING_DIRECTORY);
    if (workingDirPath == null && !templateRunConfiguration) {
      VirtualFile workingDir = JsTestRunConfigurationProducer.guessWorkingDirectory(project, configPath);
      if (workingDir != null) {
        workingDirPath = workingDir.getPath();
      }
      else {
        workingDirPath = PathUtil.getParentPath(configPath);
      }
    }
    builder.setWorkingDirectory(workingDirPath);
    builder.setInterpreterRef(NodeJsInterpreterRef.create(JDOMExternalizerUtil.readCustomField(element, NODE_INTERPRETER)));
    builder.setNodeOptions(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, NODE_OPTIONS)));
    builder.setEnvData(EnvironmentVariablesData.readExternal(element));

    KarmaScopeKind scopeKind = readScopeKind(element);
    builder.setScopeKind(scopeKind);
    if (scopeKind == KarmaScopeKind.TEST_FILE) {
      builder.setTestFilePath(JDOMExternalizerUtil.readCustomField(element, TEST_FILE_PATH));
    }
    else if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      builder.setTestNames(readTestNames(element));
    }

    return builder.build();
  }

  @NotNull
  private static KarmaScopeKind readScopeKind(@NotNull Element element) {
    String value = JDOMExternalizerUtil.readCustomField(element, SCOPE_KIND);
    if (StringUtil.isNotEmpty(value)) {
      try {
        return KarmaScopeKind.valueOf(value);
      }
      catch (IllegalArgumentException ignored) {
      }
    }
    return KarmaScopeKind.ALL;
  }

  @NotNull
  private static List<String> readTestNames(@NotNull Element parent) {
    Element testNamesElement = parent.getChild(TEST_NAMES);
    if (testNamesElement == null) {
      return Collections.emptyList();
    }
    return JDOMExternalizerUtil.getChildrenValueAttributes(testNamesElement, TEST_NAME);
  }

  public static void writeXml(@NotNull Element element,
                              @NotNull KarmaRunSettings settings,
                              boolean templateRunConfiguration) {
    JDOMExternalizerUtil.writeCustomField(element, CONFIG_FILE, settings.getConfigPathSystemIndependent());
    if (StringUtil.isNotEmpty(settings.getBrowsers())) {
      JDOMExternalizerUtil.writeCustomField(element, BROWSERS, settings.getBrowsers());
    }
    if (settings.getKarmaPackage() != null) {
      JDOMExternalizerUtil.writeCustomField(element, KARMA_PACKAGE_DIR,
                                            settings.getKarmaPackage().getSystemIndependentPath());
    }
    String workingDir = settings.getWorkingDirectorySystemIndependent();
    if (!workingDir.isEmpty() && (templateRunConfiguration || shouldWriteWorkingDir(settings))) {
      JDOMExternalizerUtil.writeCustomField(element, WORKING_DIRECTORY, workingDir);
    }
    JDOMExternalizerUtil.writeCustomField(element, NODE_INTERPRETER, settings.getInterpreterRef().getReferenceName());
    if (StringUtil.isNotEmpty(settings.getNodeOptions())) {
      JDOMExternalizerUtil.writeCustomField(element, NODE_OPTIONS, settings.getNodeOptions());
    }
    settings.getEnvData().writeExternal(element);
    KarmaScopeKind scopeKind = settings.getScopeKind();
    if (scopeKind != KarmaScopeKind.ALL) {
      JDOMExternalizerUtil.writeCustomField(element, SCOPE_KIND, scopeKind.name());
    }
    if (scopeKind == KarmaScopeKind.TEST_FILE) {
      JDOMExternalizerUtil.writeCustomField(element, TEST_FILE_PATH, settings.getTestFileSystemIndependentPath());
    }
    else if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      Element testNamesElement = new Element(TEST_NAMES);
      if (!settings.getTestNames().isEmpty()) {
        JDOMExternalizerUtil.addChildrenWithValueAttribute(testNamesElement, TEST_NAME, settings.getTestNames());
      }
      element.addContent(testNamesElement);
    }
  }

  private static boolean shouldWriteWorkingDir(@NotNull KarmaRunSettings settings) {
    String configFileDirPath = trimTrailingPathSeparator(PathUtil.getParentPath(settings.getConfigPathSystemIndependent()));
    String workingDirPath = trimTrailingPathSeparator(settings.getWorkingDirectorySystemIndependent());
    return !configFileDirPath.equals(workingDirPath);
  }

  @NotNull
  private static String trimTrailingPathSeparator(@NotNull String path) {
    if (path.length() > 1) {
      char ch = path.charAt(path.length() - 1);
      if (ch == '/' || ch == '\\') {
        return path.substring(0, path.length() - 1);
      }
    }
    return path;
  }
}
