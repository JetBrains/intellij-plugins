// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

final class KarmaRunSettingsSerializationUtil {
  private static final String CONFIG_FILE = "config-file";
  private static final String KARMA_PACKAGE_DIR = "karma-package-dir";
  private static final String WORKING_DIRECTORY = "working-directory";
  private static final String KARMA_OPTIONS = "karma-options";
  private static final String BROWSERS = "browsers";
  private static final String NODE_INTERPRETER = "node-interpreter";
  private static final String NODE_OPTIONS = "node-options";
  private static final String SCOPE_KIND = "scope-kind";
  private static final String TEST_FILE_PATH = "test-file-path";
  private static final String TEST_NAMES = "test-names";
  private static final String TEST_NAME = "test-name";

  private KarmaRunSettingsSerializationUtil() {}

  static KarmaRunSettings readXml(@NotNull Element element) {
    KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();

    String configPath = StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, CONFIG_FILE));
    builder.setConfigPath(configPath);
    String karmaOptions = JDOMExternalizerUtil.readCustomField(element, KARMA_OPTIONS);
    if (karmaOptions == null) {
      String browsers = JDOMExternalizerUtil.readCustomField(element, BROWSERS);
      if (!StringUtil.isEmptyOrSpaces(browsers)) {
        karmaOptions = "--browsers=" + browsers;
      }
    }
    builder.setKarmaOptions(karmaOptions);

    String karmaPackageDir = JDOMExternalizerUtil.readCustomField(element, KARMA_PACKAGE_DIR);
    if (karmaPackageDir != null) {
      builder.setKarmaPackage(KarmaUtil.PKG_DESCRIPTOR.createPackage(karmaPackageDir));
    }

    builder.setWorkingDirectory(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, WORKING_DIRECTORY)));
    builder.setInterpreterRef(NodeJsInterpreterRef.create(JDOMExternalizerUtil.readCustomField(element, NODE_INTERPRETER)));
    builder.setNodeOptions(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, NODE_OPTIONS)));
    builder.setEnvData(EnvironmentVariablesData.readExternal(element));

    KarmaScopeKind scopeKind = readScopeKind(element);
    builder.setScopeKind(scopeKind);
    builder.setTestFilePath(JDOMExternalizerUtil.readCustomField(element, TEST_FILE_PATH));
    if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
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

  static void writeXml(@NotNull Element element, @NotNull KarmaRunSettings settings) {
    JDOMExternalizerUtil.writeCustomField(element, CONFIG_FILE, settings.getConfigPathSystemIndependent());
    if (StringUtil.isNotEmpty(settings.getKarmaOptions())) {
      JDOMExternalizerUtil.writeCustomField(element, KARMA_OPTIONS, settings.getKarmaOptions());
    }
    if (settings.getKarmaPackage() != null) {
      JDOMExternalizerUtil.writeCustomField(element, KARMA_PACKAGE_DIR,
                                            settings.getKarmaPackage().getSystemIndependentPath());
    }
    String workingDir = settings.getWorkingDirectorySystemIndependent();
    if (!workingDir.isEmpty()) {
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
    String testFilePath = settings.getTestFileSystemIndependentPath();
    if (StringUtil.isNotEmpty(testFilePath)) {
      JDOMExternalizerUtil.writeCustomField(element, TEST_FILE_PATH, testFilePath);
    }
    if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      Element testNamesElement = new Element(TEST_NAMES);
      if (!settings.getTestNames().isEmpty()) {
        JDOMExternalizerUtil.addChildrenWithValueAttribute(testNamesElement, TEST_NAME, settings.getTestNames());
      }
      element.addContent(testNamesElement);
    }
  }
}
