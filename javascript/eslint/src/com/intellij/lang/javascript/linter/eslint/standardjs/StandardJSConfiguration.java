package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "StandardJSConfiguration", storages = @Storage("jsLinters/standardjs.xml"))
public class StandardJSConfiguration extends JSLinterConfiguration<StandardJSState> {

  private final JSLinterPackage myLinterPackage;

  public StandardJSConfiguration(@NotNull Project project) {
    super(project);
    myLinterPackage = new JSLinterPackage(project, StandardJSUtil.PACKAGE_NAME);
  }

  public boolean isFixOnSaveEnabled() {
    return isEnabled() && EslintConfiguration.getInstance(myProject).getExtendedState().getState().isRunOnSave();
  }

  @Override
  protected void savePrivateSettings(@NotNull StandardJSState state) {
    if (!state.isDefault()) {
      myLinterPackage.force(state.getNodePackageRef());
    }
  }

  @Override
  protected @NotNull StandardJSState loadPrivateSettings(@NotNull StandardJSState state) {
    myLinterPackage.readOrDetect();
    NodePackage constantPackage = myLinterPackage.getPackage().getConstantPackage();
    assert constantPackage != null : "StandardJS does not support non-constant node package refs";
    return new StandardJSState(constantPackage);
  }

  @Override
  protected @NotNull Class<? extends JSLinterInspection> getInspectionClass() {
    return StandardJSInspection.class;
  }

  @Override
  protected @Nullable Element toXml(@NotNull StandardJSState state) {
    return null;
  }

  @Override
  protected @NotNull StandardJSState fromXml(@NotNull Element element) {
    return getDefaultState();
  }

  @Override
  protected @NotNull StandardJSState getDefaultState() {
    return StandardJSState.DEFAULT;
  }

  public static @NotNull StandardJSConfiguration getInstance(@NotNull Project project) {
    return getInstance(project, StandardJSConfiguration.class);
  }
}
