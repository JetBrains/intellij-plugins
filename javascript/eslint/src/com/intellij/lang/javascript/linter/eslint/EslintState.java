package com.intellij.lang.javascript.linter.eslint;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;

public final class EslintState implements JSNpmLinterState<EslintState> {
  public static final EslintState DEFAULT = new EslintState.Builder().build();

  private final @NotNull NodePackageRef myNodePackageRef;
  private final String myWorkDirPatterns;
  private final boolean myCustomConfigFileUsed;
  private final String myCustomConfigFilePath;
  private final String myAdditionalRulesDirPath;
  private final String myExtraOptions;
  private final String myFilesPattern;
  private final boolean myRunOnSave;

  private EslintState(@NotNull Builder builder) {
    myNodePackageRef = builder.myEslintPackage;
    myWorkDirPatterns = builder.myWorkDirPatterns;
    myCustomConfigFileUsed = builder.myCustomConfigFileUsed;
    myCustomConfigFilePath = builder.myCustomConfigFilePath;
    myAdditionalRulesDirPath = builder.myAdditionalRulesDirPath;
    myExtraOptions = builder.myExtraOptions;
    myFilesPattern = builder.myFilesPattern;
    myRunOnSave = builder.myRunOnSave;
  }

  @Override
  public @NotNull NodePackageRef getNodePackageRef() {
    return myNodePackageRef;
  }

  /**
   * @return Semicolon-separated list of folder paths or glob patterns (absolute, or relative to the content root)
   */
  public @NotNull String getWorkDirPatterns() {
    return myWorkDirPatterns;
  }

  public boolean isCustomConfigFileUsed() {
    return myCustomConfigFileUsed;
  }

  public @NotNull @NlsSafe String getCustomConfigFilePath() {
    return myCustomConfigFilePath;
  }

  public @NotNull @NlsSafe String getAdditionalRulesDirPath() {
    return myAdditionalRulesDirPath;
  }

  public @NotNull @NlsSafe String getExtraOptions() {
    return myExtraOptions;
  }

  public @NotNull @NlsSafe String getFilesPattern() {
    return myFilesPattern;
  }

  public boolean isRunOnSave() {
    return myRunOnSave;
  }

  @Override
  public EslintState withLinterPackage(@NotNull NodePackageRef nodePackage) {
    return new EslintState(new Builder(this).setEslintPackage(nodePackage));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EslintState state = (EslintState)o;

    return myCustomConfigFileUsed == state.myCustomConfigFileUsed &&
           myCustomConfigFilePath.equals(state.myCustomConfigFilePath) &&
           myNodePackageRef.equals(state.myNodePackageRef) &&
           myWorkDirPatterns.equals(state.myWorkDirPatterns) &&
           myAdditionalRulesDirPath.equals(state.myAdditionalRulesDirPath) &&
           myExtraOptions.equals(state.myExtraOptions) &&
           myFilesPattern.equals(state.myFilesPattern) &&
           myRunOnSave == state.myRunOnSave;
  }

  @Override
  public int hashCode() {
    int result = myNodePackageRef.hashCode();
    result = 31 * result + myWorkDirPatterns.hashCode();
    result = 31 * result + (myCustomConfigFileUsed ? 1 : 0);
    result = 31 * result + myCustomConfigFilePath.hashCode();
    result = 31 * result + myAdditionalRulesDirPath.hashCode();
    result = 31 * result + myExtraOptions.hashCode();
    result = 31 * result + myFilesPattern.hashCode();
    result = 31 * result + (myRunOnSave ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "eslint=" + myNodePackageRef +
           ", workdir=" + myWorkDirPatterns +
           ", isCustomConfig=" + myCustomConfigFileUsed +
           ", customConfig=" + myCustomConfigFilePath +
           ", rulesDir='" + myAdditionalRulesDirPath +
           ", extraOptions='" + myExtraOptions +
           ", filesPattern='" + myFilesPattern +
           ", runOnSave='" + myRunOnSave;
  }

  public static class Builder {
    private NodePackageRef myEslintPackage = NodePackageRef.create(new NodePackage(""));
    private String myWorkDirPatterns = "";
    private boolean myCustomConfigFileUsed = false;
    private String myCustomConfigFilePath = "";
    private String myAdditionalRulesDirPath = "";
    private String myExtraOptions = "";
    private String myFilesPattern = EslintConfiguration.ESLINT_FILES_PATTERN_DEFAULT;
    private boolean myRunOnSave = EslintConfiguration.RUN_ON_SAVE_DEFAULT;

    public Builder() {}

    public Builder(@NotNull EslintState state) {
      myEslintPackage = state.getNodePackageRef();
      myWorkDirPatterns = state.getWorkDirPatterns();
      myCustomConfigFileUsed = state.isCustomConfigFileUsed();
      myCustomConfigFilePath = state.getCustomConfigFilePath();
      myAdditionalRulesDirPath = state.getAdditionalRulesDirPath();
      myExtraOptions = state.getExtraOptions();
      myFilesPattern = state.myFilesPattern;
      myRunOnSave = state.myRunOnSave;
    }

    public @NotNull Builder setEslintPackage(@NotNull NodePackageRef nodePackage) {
      myEslintPackage = nodePackage;
      return this;
    }

    public @NotNull Builder setWorkDirPatterns(@NotNull String workDirPatterns) {
      myWorkDirPatterns = workDirPatterns;
      return this;
    }

    public @NotNull Builder setCustomConfigFileUsed(boolean customConfigFileUsed) {
      myCustomConfigFileUsed = customConfigFileUsed;
      return this;
    }

    public @NotNull Builder setCustomConfigFilePath(@NotNull String customConfigFilePath) {
      myCustomConfigFilePath = customConfigFilePath;
      return this;
    }

    public @NotNull Builder setAdditionalRulesDirPath(@NotNull String additionalRulesDirPath) {
      myAdditionalRulesDirPath = additionalRulesDirPath;
      return this;
    }

    public @NotNull Builder setExtraOptions(@NotNull String extraOptions) {
      myExtraOptions = extraOptions;
      return this;
    }

    public @NotNull Builder setFilesPattern(@NotNull String pattern) {
      myFilesPattern = pattern;
      return this;
    }

    public @NotNull Builder setRunOnSave(boolean runOnSave) {
      myRunOnSave = runOnSave;
      return this;
    }

    public @NotNull EslintState build() {
      return new EslintState(this);
    }
  }
}
