// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "TsLintConfiguration", storages = @Storage("jsLinters/tslint.xml"))
public class TsLintConfiguration extends JSLinterConfiguration<TsLintState> {
  private static final String TSLINT_ELEMENT_NAME = "tslint";
  private static final String IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME = "use-custom-config-file";
  private static final String CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME = "custom-config-file-path";
  private static final String RULES = "rules";
  private static final String ALLOW_JS = "allowJs";

  private final JSLinterPackage myPackage;

  public TsLintConfiguration(@NotNull Project project) {
    super(project);
    myPackage = new JSLinterPackage(project, "tslint", true);
  }

  @NotNull
  public static TsLintConfiguration getInstance(@NotNull final Project project) {
    return JSLinterConfiguration.getInstance(project, TsLintConfiguration.class);
  }

  @Override
  protected void savePrivateSettings(@NotNull TsLintState state) {
    storeLinterLocalPaths(state);
  }

  @NotNull
  @Override
  protected TsLintState loadPrivateSettings(@NotNull TsLintState state) {
    TsLintState.Builder builder = new TsLintState.Builder(state);
    restoreLinterLocalPaths(builder);
    return builder.build();
  }

  @NotNull
  @Override
  protected Class<? extends JSLinterInspection> getInspectionClass() {
    return TsLintInspection.class;
  }

  @Nullable
  @Override
  protected Element toXml(@NotNull TsLintState state) {
    final Element root = new Element(TSLINT_ELEMENT_NAME);
    if (state.isCustomConfigFileUsed()) {
      root.setAttribute(IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME, Boolean.TRUE.toString());
    }
    final String customConfigFilePath = state.getCustomConfigFilePath();
    if (!StringUtil.isEmptyOrSpaces(customConfigFilePath)) {
      root.setAttribute(CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME, customConfigFilePath);
    }
    final String rulesDirectory = state.getRulesDirectory();
    if (!StringUtil.isEmptyOrSpaces(rulesDirectory)) {
      root.setAttribute(RULES, rulesDirectory);
    }
    if (state.isAllowJs()) {
      root.setAttribute(ALLOW_JS, String.valueOf(true));
    }
    storeLinterLocalPaths(state);
    return root;
  }

  @NotNull
  @Override
  protected TsLintState fromXml(@NotNull Element element) {
    final TsLintState.Builder builder = new TsLintState.Builder();
    builder.setCustomConfigFileUsed(Boolean.parseBoolean(element.getAttributeValue(IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME)));
    String customConfigFilePath = StringUtil.notNullize(element.getAttributeValue(CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME));
    builder.setCustomConfigFilePath(customConfigFilePath);
    final String rulesDirectory = element.getAttributeValue(RULES);
    if (!StringUtil.isEmptyOrSpaces(rulesDirectory)) {
      builder.setRulesDirectory(rulesDirectory);
    }
    builder.setAllowJs(Boolean.parseBoolean(element.getAttributeValue(ALLOW_JS)));
    restoreLinterLocalPaths(builder);
    return builder.build();
  }

  private void restoreLinterLocalPaths(TsLintState.Builder builder) {
    myPackage.readOrDetect();
    builder.setNodePackageRef(myPackage.getPackage());
  }

  private void storeLinterLocalPaths(TsLintState state) {
    myPackage.force(state.getNodePackageRef());
  }

  @NotNull
  @Override
  protected TsLintState getDefaultState() {
    return TsLintState.DEFAULT;
  }
}
