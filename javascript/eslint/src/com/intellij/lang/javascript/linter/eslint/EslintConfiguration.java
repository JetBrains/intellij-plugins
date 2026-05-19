package com.intellij.lang.javascript.linter.eslint;

import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "EslintConfiguration", storages = @Storage("jsLinters/eslint.xml"))
public class EslintConfiguration extends JSLinterConfiguration<EslintState> {

  private static final String TAG_WORK_DIR_PATTERNS = "work-dir-patterns";
  private static final String TAG_CUSTOM_CONFIGURATION_FILE = "custom-configuration-file";
  private static final String ATTR_CUSTOM_CONFIGURATION_FILE_USED = "used";
  private static final String ATTR_CUSTOM_CONFIGURATION_FILE_PATH = "path";
  private static final String TAG_ADDITIONAL_RULES_DIR = "additional-rules-dir";
  private static final String TAG_EXTRA_ESLINT_OPTIONS = "extra-options";
  private static final String TAG_FILES_PATTERN = "files-pattern";
  private static final String TAG_RUN_ON_SAVE = "fix-on-save";

  static final @NonNls String ESLINT_FILES_PATTERN_DEFAULT = "**/*.{js,ts,jsx,tsx,cjs,cts,mjs,mts,html,vue}";
  static final boolean RUN_ON_SAVE_DEFAULT = false;

  private final JSLinterPackage myPackage;

  public EslintConfiguration(@NotNull Project project) {
    super(project);
    myPackage = new JSLinterPackage(project, EslintUtil.PACKAGE_NAME, true);
  }

  public static @NotNull EslintConfiguration getInstance(@NotNull Project project) {
    return getInstance(project, EslintConfiguration.class);
  }

  public boolean isFixOnSaveEnabled() {
    return isEnabled() && getExtendedState().getState().isRunOnSave();
  }

  @Override
  protected @NotNull Class<? extends JSLinterInspection> getInspectionClass() {
    return EslintInspection.class;
  }

  @Override
  protected void savePrivateSettings(@NotNull EslintState state) {
    doSavePrivateSettings(state);
  }

  @Override
  protected @NotNull EslintState loadPrivateSettings(@NotNull EslintState state) {
    EslintState.Builder builder = new EslintState.Builder(state);
    doLoadPrivateSettings(builder);
    return builder.build();
  }

  @Override
  protected @Nullable Element toXml(@NotNull EslintState state) {
    if (isEmpty(state)) {
      return null;
    }
    Element parent = new Element("eslint");
    String workDirPatterns = state.getWorkDirPatterns();
    if (!workDirPatterns.isEmpty()) {
      JDOMExternalizerUtil.writeCustomField(parent, TAG_WORK_DIR_PATTERNS, workDirPatterns);
    }
    if (state.isCustomConfigFileUsed() || !state.getCustomConfigFilePath().isEmpty()) {
      Element customConfigurationFileElement = new Element(TAG_CUSTOM_CONFIGURATION_FILE);
      customConfigurationFileElement.setAttribute(ATTR_CUSTOM_CONFIGURATION_FILE_USED, Boolean.toString(state.isCustomConfigFileUsed()));
      customConfigurationFileElement.setAttribute(ATTR_CUSTOM_CONFIGURATION_FILE_PATH, state.getCustomConfigFilePath());
      parent.addContent(customConfigurationFileElement);
    }
    String additionalRulesDirPath = state.getAdditionalRulesDirPath();
    if (!additionalRulesDirPath.isEmpty()) {
      JDOMExternalizerUtil.writeCustomField(parent, TAG_ADDITIONAL_RULES_DIR, additionalRulesDirPath);
    }
    String extraOptions = state.getExtraOptions();
    if (!StringUtil.isEmptyOrSpaces(extraOptions)) {
      JDOMExternalizerUtil.writeCustomField(parent, TAG_EXTRA_ESLINT_OPTIONS, extraOptions);
    }

    String filesPattern = state.getFilesPattern();
    if (!ESLINT_FILES_PATTERN_DEFAULT.equals(filesPattern)) {
      JDOMExternalizerUtil.writeCustomField(parent, TAG_FILES_PATTERN, filesPattern);
    }

    JDOMExternalizerUtil.writeField(parent, TAG_RUN_ON_SAVE, String.valueOf(state.isRunOnSave()), String.valueOf(RUN_ON_SAVE_DEFAULT));

    return parent;
  }


  @Override
  protected @NotNull EslintState fromXml(@NotNull Element element) {
    EslintState.Builder builder = new EslintState.Builder();
    Element customConfigurationFileElement = element.getChild(TAG_CUSTOM_CONFIGURATION_FILE);
    String workDirPatterns = JDOMExternalizerUtil.readCustomField(element, TAG_WORK_DIR_PATTERNS);
    if (workDirPatterns != null) {
      builder.setWorkDirPatterns(workDirPatterns);
    }
    if (customConfigurationFileElement != null) {
      boolean used = Boolean.parseBoolean(customConfigurationFileElement.getAttributeValue(ATTR_CUSTOM_CONFIGURATION_FILE_USED));
      builder.setCustomConfigFileUsed(used);
      String customConfigFilePath = customConfigurationFileElement.getAttributeValue(ATTR_CUSTOM_CONFIGURATION_FILE_PATH);
      builder.setCustomConfigFilePath(StringUtil.notNullize(customConfigFilePath));
    }
    String additionalRulesDirPath = JDOMExternalizerUtil.readCustomField(element, TAG_ADDITIONAL_RULES_DIR);
    if (additionalRulesDirPath != null) {
      builder.setAdditionalRulesDirPath(additionalRulesDirPath);
    }
    String extraOptions = JDOMExternalizerUtil.readCustomField(element, TAG_EXTRA_ESLINT_OPTIONS);
    if (extraOptions != null) {
      builder.setExtraOptions(extraOptions);
    }

    String filesPattern = JDOMExternalizerUtil.readCustomField(element, TAG_FILES_PATTERN);
    if (filesPattern != null) {
      builder.setFilesPattern(filesPattern);
    }

    builder
      .setRunOnSave(Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, TAG_RUN_ON_SAVE, String.valueOf(RUN_ON_SAVE_DEFAULT))));

    return builder.build();
  }

  @Override
  protected @NotNull EslintState getDefaultState() {
    return EslintState.DEFAULT;
  }

  private void doSavePrivateSettings(@NotNull EslintState state) {
    myPackage.force(state.getNodePackageRef());
  }

  private void doLoadPrivateSettings(@NotNull EslintState.Builder builder) {
    myPackage.readOrDetect();
    builder.setEslintPackage(myPackage.getPackage());
  }

  private static boolean isEmpty(@NotNull EslintState state) {
    String additionalRulesDirPath = state.getAdditionalRulesDirPath();
    String extraOptions = state.getExtraOptions();
    return state.getWorkDirPatterns().isEmpty() &&
           (!state.isCustomConfigFileUsed() || state.getCustomConfigFilePath().isEmpty()) &&
           additionalRulesDirPath.isEmpty() &&
           StringUtil.isEmptyOrSpaces(extraOptions) &&
           state.getFilesPattern().equals(ESLINT_FILES_PATTERN_DEFAULT) &&
           state.isRunOnSave() == RUN_ON_SAVE_DEFAULT;
  }
}
