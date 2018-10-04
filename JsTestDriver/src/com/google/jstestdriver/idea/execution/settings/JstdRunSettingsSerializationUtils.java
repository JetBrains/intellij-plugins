// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.jstestdriver.idea.execution.settings;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.EnumUtils;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.JdomKt;
import com.intellij.util.ObjectUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JstdRunSettingsSerializationUtils {

  private enum Key {
    TEST_TYPE("configLocationType"),
    JSTD_CONFIG_FILE("settingsFile"),
    ALL_IN_DIRECTORY("allInDirectory"),
    CONFIG_TYPE("configType"),
    JS_FILE("jsFile"),
    TEST_CASE("testCase"),
    TEST_METHOD("testMethod"),
    SERVER_ADDRESS("serverAddress"),
    SERVER_TYPE("serverType"),
    COVERAGE("coverage"),
    COVERAGE_EXCLUDED("excluded"),
    COVERAGE_EXCLUDED_PATH("path"),
    PREFERRED_DEBUG_BROWSER("preferredDebugBrowser");

    private final String key;

    Key(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }

  private JstdRunSettingsSerializationUtils() {}

  public static JstdRunSettings readFromXml(@NotNull Element element) {
    JstdRunSettings.Builder builder = new JstdRunSettings.Builder();

    TestType testType = readEnumByName(element, Key.TEST_TYPE, TestType.CONFIG_FILE);
    builder.setTestType(testType);
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      String directory = readString(element, Key.ALL_IN_DIRECTORY, "");
      builder.setDirectory(FileUtil.toSystemDependentName(directory));
    } else if (testType == TestType.CONFIG_FILE) {
      String configFile = readString(element, Key.JSTD_CONFIG_FILE, "");
      builder.setConfigFile(FileUtil.toSystemDependentName(configFile));
    } else if (testType == TestType.JS_FILE) {
      readJsFile(element, builder);
    } else if (testType == TestType.TEST_CASE) {
      readTestCase(element, builder);
    } else if (testType == TestType.TEST_METHOD) {
      readTestMethod(element, builder);
    }
    ServerType serverType = readEnumByName(element, Key.SERVER_TYPE, ServerType.INTERNAL);
    builder.setServerType(serverType);
    if (serverType == ServerType.EXTERNAL) {
      String serverAddress = readString(element, Key.SERVER_ADDRESS, "");
      builder.setServerAddress(serverAddress);
    }
    List<String> filesExcludedFromCoverage = readFilesExcludedFromCoverage(element);
    builder.setFilesExcludedFromCoverage(filesExcludedFromCoverage);

    builder.setPreferredDebugBrowser(WebBrowserManager.getInstance().getFirstBrowser(readEnumByName(element, Key.PREFERRED_DEBUG_BROWSER, BrowserFamily.CHROME)));
    return builder.build();
  }

  private static List<String> readFilesExcludedFromCoverage(@NotNull Element root) {
    List<String> excludedPaths = Lists.newArrayList();
    Element coverageElement = root.getChild(Key.COVERAGE.getKey());
    if (coverageElement != null) {
      List<Element> excludedElements = coverageElement.getChildren(Key.COVERAGE_EXCLUDED.getKey());
      for (Element excludedElement : excludedElements) {
        Attribute pathAttr = excludedElement.getAttribute(Key.COVERAGE_EXCLUDED_PATH.getKey());
        if (pathAttr != null) {
          String path = pathAttr.getValue();
          if (StringUtil.isNotEmpty(path)) {
            excludedPaths.add(FileUtil.toSystemDependentName(path));
          }
        }
      }
    }
    return excludedPaths;
  }

  private static void writeFilesExcludedFromCoverage(@NotNull Element root,
                                                     @NotNull List<String> excludedPaths) {
    if (excludedPaths.isEmpty()) {
      return;
    }
    Element coverageElement = new Element(Key.COVERAGE.getKey());
    root.addContent(coverageElement);
    for (String path : excludedPaths) {
      Element excludedElement = new Element(Key.COVERAGE_EXCLUDED.getKey());
      excludedElement.setAttribute(
        Key.COVERAGE_EXCLUDED_PATH.getKey(),
        FileUtil.toSystemIndependentName(path)
      );
      coverageElement.addContent(excludedElement);
    }
  }

  private static void readTestMethod(@NotNull Element element, @NotNull JstdRunSettings.Builder builder) {
    readTestCase(element, builder);
    String testMethodName = readString(element, Key.TEST_METHOD, "");
    builder.setTestMethodName(testMethodName);
  }

  private static void readTestCase(@NotNull Element element, @NotNull JstdRunSettings.Builder builder) {
    readJsFile(element, builder);
    String testCaseName = readString(element, Key.TEST_CASE, "");
    builder.setTestCaseName(testCaseName);
  }

  private static void readJsFile(@NotNull Element element, @NotNull JstdRunSettings.Builder builder) {
    String configFile = readString(element, Key.JSTD_CONFIG_FILE, "");
    builder.setConfigFile(FileUtil.toSystemDependentName(configFile));
    String jsFile = readString(element, Key.JS_FILE, "");
    builder.setJSFilePath(FileUtil.toSystemDependentName(jsFile));
  }

  @NotNull
  private static <E extends Enum<E>> E readEnumByName(@NotNull Element element, @NotNull Key key, @NotNull E defaultValue) {
    String str = readString(element, key, "");
    E enumConstant = EnumUtils.findEnum(defaultValue.getDeclaringClass(), str);
    return ObjectUtils.notNull(enumConstant, defaultValue);
  }

  @NotNull
  private static String readString(@NotNull Element element, @NotNull Key key, @NotNull String defaultValue) {
    String value = JDOMExternalizer.readString(element, key.getKey());
    return value != null ? value : defaultValue;
  }

  public static void writeToXml(@NotNull Element element, @NotNull JstdRunSettings runSettings) {
    TestType testType = runSettings.getTestType();
    writeString(element, Key.TEST_TYPE, testType.name());
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      writeString(element, Key.ALL_IN_DIRECTORY, FileUtil.toSystemIndependentName(runSettings.getDirectory()));
    } else if (testType == TestType.CONFIG_FILE) {
      writeString(element, Key.JSTD_CONFIG_FILE, FileUtil.toSystemIndependentName(runSettings.getConfigFile()));
    } else if (testType == TestType.JS_FILE) {
      writeJsFile(element, runSettings);
    } else if (testType == TestType.TEST_CASE) {
      writeTestCase(element, runSettings);
    } else if (testType == TestType.TEST_METHOD) {
      writeTestMethod(element, runSettings);
    }
    writeString(element, Key.SERVER_TYPE, runSettings.getServerType().name());
    if (runSettings.getServerType() == ServerType.EXTERNAL) {
      writeString(element, Key.SERVER_ADDRESS, runSettings.getServerAddress());
    }
    writeFilesExcludedFromCoverage(element, runSettings.getFilesExcludedFromCoverage());
    writeString(element, Key.PREFERRED_DEBUG_BROWSER, runSettings.getPreferredDebugBrowser().getName());
  }

  private static void writeTestMethod(@NotNull Element element, @NotNull JstdRunSettings runSettings) {
    writeTestCase(element, runSettings);
    writeString(element, Key.TEST_METHOD, runSettings.getTestMethodName());
  }

  private static void writeTestCase(@NotNull Element element, @NotNull JstdRunSettings runSettings) {
    writeJsFile(element, runSettings);
    writeString(element, Key.TEST_CASE, runSettings.getTestCaseName());
  }

  private static void writeJsFile(@NotNull Element element, @NotNull JstdRunSettings runSettings) {
    writeString(element, Key.CONFIG_TYPE, "FILE_PATH");
    writeString(element, Key.JSTD_CONFIG_FILE, FileUtil.toSystemIndependentName(runSettings.getConfigFile()));
    writeString(element, Key.JS_FILE, FileUtil.toSystemIndependentName(runSettings.getJsFilePath()));
  }

  private static void writeString(@NotNull Element element, @NotNull Key key, @NotNull String value) {
    JdomKt.addOptionTag(element, key.getKey(), value, "setting");
  }
}
