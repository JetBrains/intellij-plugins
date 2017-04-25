package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
@State(name = "TsLintConfiguration", storages = @Storage("jsLinters/tslint.xml"))
public class TsLintConfiguration extends JSLinterConfiguration<TsLintState> {

  public static final String TSLINT_JSON = "tslint.json";

  public static final String LOG_CATEGORY = "#com.intellij.lang.javascript.linter.tslint.TsLint";
  private static final String TSLINT_ELEMENT_NAME = "tslint";
  private static final String IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME = "use-custom-config-file";
  private static final String CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME = "custom-config-file-path";
  private static final String RULES = "rules";
  private static final String ALLOW_JS = "allowJs";

  private final PathHolder myPathHolder;
  private final JSLinterPackage myPackage;

  private TsLintState DEFAULT_STATE;

  public TsLintConfiguration(@NotNull Project project, @NotNull SharedConfigurationPaths paths) {
    super(project);
    myPathHolder = paths.createPathHolder(new Runnable() {
      @Override
      public void run() {
        final ExtendedLinterState<TsLintState> state = getExtendedState();
        state.getState().setCustomConfigFilePath(myPathHolder.getPath());
        setExtendedState(state);
      }
    });
    myPackage = new JSLinterPackage(project, "tslint");
  }

  @NotNull
  public static TsLintConfiguration getInstance(@NotNull final Project project) {
    return JSLinterConfiguration.getInstance(project, TsLintConfiguration.class);
  }

  @Override
  protected void savePrivateSettings(@NotNull TsLintState state) {
    myPathHolder.setPath(StringUtil.notNullize(state.getCustomConfigFilePath()));
    storeLinterLocalPaths(state);
  }

  @NotNull
  @Override
  protected TsLintState loadPrivateSettings(@NotNull TsLintState state) {
    myPathHolder.setPath(StringUtil.notNullize(state.getCustomConfigFilePath()));
    TsLintState.Builder builder = new TsLintState.Builder(state);
    restoreLinterLocalPaths(builder);
    return builder.build();
  }

  public boolean isAllowJs() {
    return getMyStateWithoutPrivateSettings().isAllowJs();
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
      root.setAttribute(CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME, FileUtil.toSystemIndependentName(customConfigFilePath));
    }
    final String rulesDirectory = state.getRulesDirectory();
    if (!StringUtil.isEmptyOrSpaces(rulesDirectory)) {
      root.setAttribute(RULES, FileUtil.toSystemIndependentName(rulesDirectory));
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
    builder.setCustomConfigFilePath(FileUtil.toSystemDependentName(customConfigFilePath));
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
    builder.setNodePath(myPackage.getInterpreter());
    builder.setPackagePath(StringUtil.notNullize(myPackage.getPackagePath()));
  }

  private void storeLinterLocalPaths(TsLintState state) {
    myPackage.force(state.getInterpreterRef(), state.getPackagePath());
  }

  @NotNull
  @Override
  protected TsLintState getDefaultState() {
    TsLintState state = DEFAULT_STATE;
    if (state != null) {
      return state;
    }
    final TsLintState.Builder builder = new TsLintState.Builder();
    builder.setCustomConfigFileUsed(false);

    state = builder.build();
    DEFAULT_STATE = state;
    return state;
  }
}
