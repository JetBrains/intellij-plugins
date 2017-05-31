package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class KarmaRunSettingsSerializationUtil {

  private static final String CONFIG_FILE = "config-file";
  private static final String KARMA_PACKAGE_DIR = "karma-package-dir";
  private static final String BROWSERS = "browsers";
  private static final String NODE_INTERPRETER = "node-interpreter";
  private static final String SCOPE_KIND = "scope-kind";
  private static final String TEST_FILE_PATH = "test-file-path";
  private static final String TEST_NAMES = "test-names";
  private static final String TEST_NAME = "test-name";

  private KarmaRunSettingsSerializationUtil() {}

  public static KarmaRunSettings readXml(@NotNull Element element) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();

    builder.setConfigPath(JDOMExternalizerUtil.getFirstChildValueAttribute(element, CONFIG_FILE));
    builder.setBrowsers(JDOMExternalizerUtil.getFirstChildValueAttribute(element, BROWSERS));

    String karmaPackageDir = JDOMExternalizerUtil.getFirstChildValueAttribute(element, KARMA_PACKAGE_DIR);
    if (karmaPackageDir != null) {
      builder.setKarmaPackage(new NodePackage(karmaPackageDir));
    }

    String interpreterRefName = JDOMExternalizerUtil.getFirstChildValueAttribute(element, NODE_INTERPRETER);
    builder.setInterpreterRef(NodeJsInterpreterRef.create(interpreterRefName));

    EnvironmentVariablesData envData = EnvironmentVariablesData.readExternal(element);
    builder.setEnvData(envData);
    KarmaScopeKind scopeKind = readScopeKind(element);
    builder.setScopeKind(scopeKind);
    if (scopeKind == KarmaScopeKind.TEST_FILE) {
      builder.setTestFilePath(JDOMExternalizerUtil.getFirstChildValueAttribute(element, TEST_FILE_PATH));
    }
    else if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      builder.setTestNames(readTestNames(element));
    }

    return builder.build();
  }

  @NotNull
  private static KarmaScopeKind readScopeKind(@NotNull Element element) {
    String value = JDOMExternalizerUtil.getFirstChildValueAttribute(element, SCOPE_KIND);
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

  public static void writeXml(@NotNull Element element, @NotNull KarmaRunSettings settings) {
    JDOMExternalizerUtil.addElementWithValueAttribute(element, CONFIG_FILE, settings.getConfigSystemIndependentPath());
    if (StringUtil.isNotEmpty(settings.getBrowsers())) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, BROWSERS, settings.getBrowsers());
    }
    if (settings.getKarmaPackage() != null) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, KARMA_PACKAGE_DIR,
                                                        settings.getKarmaPackage().getSystemIndependentPath());
    }
    JDOMExternalizerUtil.addElementWithValueAttribute(element, NODE_INTERPRETER, settings.getInterpreterRef().getReferenceName());
    settings.getEnvData().writeExternal(element);
    KarmaScopeKind scopeKind = settings.getScopeKind();
    if (scopeKind != KarmaScopeKind.ALL) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, SCOPE_KIND, scopeKind.name());
    }
    if (scopeKind == KarmaScopeKind.TEST_FILE) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, TEST_FILE_PATH, settings.getTestFileSystemIndependentPath());
    }
    else if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      Element testNamesElement = new Element(TEST_NAMES);
      if (!settings.getTestNames().isEmpty()) {
        JDOMExternalizerUtil.addChildrenWithValueAttribute(testNamesElement, TEST_NAME, settings.getTestNames());
      }
      element.addContent(testNamesElement);
    }
  }
}
