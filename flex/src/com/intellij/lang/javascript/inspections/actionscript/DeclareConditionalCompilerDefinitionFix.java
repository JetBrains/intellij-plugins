package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompilerOptionsConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.lang.javascript.validation.fixes.FixAndIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.navigation.Place;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeclareConditionalCompilerDefinitionFix extends FixAndIntentionAction {

  private final Module myModule;
  private final String myConditionalCompilerDefinitionName;

  public DeclareConditionalCompilerDefinitionFix(final @NotNull Module module, final String conditionalCompilerDefinitionName) {
    myModule = module;
    myConditionalCompilerDefinitionName = conditionalCompilerDefinitionName;
  }

  @Override
  @NotNull
  public String getName() {
    return FlexBundle.message("define.0", myConditionalCompilerDefinitionName);
  }

  @Override
  protected void applyFix(final Project project, final PsiElement psiElement, @NotNull final PsiFile file, @Nullable final Editor editor) {
    final ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(project);

    ShowSettingsUtil.getInstance().editConfigurable(project, configurable, () -> {
      final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();
      final Place place = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(myModule, bc.getName())
        .putPath(CompositeConfigurable.TAB_NAME, CompilerOptionsConfigurable.getTabName())
        .putPath(FlexBCConfigurable.LOCATION_ON_TAB, CompilerOptionsConfigurable.Location.ConditionalCompilerDefinition)
        .putPath(CompilerOptionsConfigurable.CONDITIONAL_COMPILER_DEFINITION_NAME, myConditionalCompilerDefinitionName);
    configurable.navigateTo(place, true);
    });
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
