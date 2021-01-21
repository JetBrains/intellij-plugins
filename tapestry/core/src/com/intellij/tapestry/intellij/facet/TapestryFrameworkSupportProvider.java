package com.intellij.tapestry.intellij.facet;

import com.intellij.CommonBundle;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkRole;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.javaee.framework.JavaeeProjectCategory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.intellij.facet.ui.NewFacetDialog;
import com.intellij.tapestry.intellij.util.Validators;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TapestryFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<TapestryFacet> {

  public TapestryFrameworkSupportProvider() {
    super(TapestryFacetType.getInstance());
  }

  @Override
  protected void setupConfiguration(TapestryFacet tapestryFacet, ModifiableRootModel modifiableRootModel, FrameworkVersion version) {
    setupConfiguration(tapestryFacet.getConfiguration(), tapestryFacet.getModule(), TapestryVersion.fromString(version.getVersionName()));
  }

  protected static void setupConfiguration(TapestryFacetConfiguration conf, Module module, final TapestryVersion version) {
    conf.setVersion(version);
    if(StringUtil.isEmpty(conf.getFilterName())) conf.setFilterName(StringUtil.toLowerCase(module.getName()));
    if(StringUtil.isEmpty(conf.getApplicationPackage())) conf.setApplicationPackage("com.app");
  }

  @Override
  @NotNull
  public List<FrameworkVersion> getVersions() {
    List<FrameworkVersion> result = new ArrayList<>();
    for (TapestryVersion version : TapestryVersion.values()) {
      final String name = version.toString();

      result.add(new FrameworkVersion(name, "tapestry-" + name, TapestryFacetConfiguration.getLibraryInfos(name)));
    }
    return result;
  }

  @Override
  public boolean isEnabledForModuleBuilder(@NotNull ModuleBuilder builder) {
    return JavaeeProjectCategory.LEGACY_MODULE_BUILDER_ID.equals(builder.getBuilderId());
  }

  @Override
  public String getTitle() {
    return "Tapestry";
  }

  @Override
  protected void onFacetCreated(final TapestryFacet facet, final ModifiableRootModel rootModel, final FrameworkVersion version) {
    final Project project = facet.getModule().getProject();
    StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
      Runnable action = () -> {
        if (project.isDisposed()) return;
        final TapestryFacetConfiguration configuration = facet.getConfiguration();

        final NewFacetDialog newFacetDialog = new NewFacetDialog(configuration);
        final DialogBuilder builder = new DialogBuilder(project);

        builder.removeAllActions();
        builder.addOkAction();
        builder.setCenterPanel(newFacetDialog.getMainPanel());
        builder.setTitle("New Tapestry Support");

        builder.setOkOperation(() -> {
          if (!Validators.isValidPackageName(newFacetDialog.getApplicationPackage())) {
            Messages.showErrorDialog("Invalid package!", CommonBundle.getErrorTitle());
            return;
          }
          configuration.setFilterName(newFacetDialog.getFilterName());
          configuration.setApplicationPackage(newFacetDialog.getApplicationPackage());

          builder.getWindow().dispose();
        });
        builder.showModal(true);

        AddTapestrySupportUtil.addSupportInWriteCommandAction(rootModel.getModule(), configuration,
                                                              newFacetDialog.shouldGenerateStartupApplication(),
                                                              newFacetDialog.shouldGeneratePom());
      };

      if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
        ApplicationManager.getApplication().invokeLater(action, ModalityState.defaultModalityState());
      } else {
        action.run();
      }
    });
  }

  @Override
  public FrameworkRole[] getRoles() {
    return new FrameworkRole[] {JavaeeProjectCategory.ROLE };
  }
}
