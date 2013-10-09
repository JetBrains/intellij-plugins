package com.intellij.tapestry.intellij.facet;

import com.intellij.CommonBundle;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.javaee.framework.JavaWebProjectType;
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

  protected void setupConfiguration(TapestryFacet tapestryFacet, ModifiableRootModel modifiableRootModel, FrameworkVersion version) {
    setupConfiguration(tapestryFacet.getConfiguration(), tapestryFacet.getModule(), TapestryVersion.fromString(version.getVersionName()));
  }

  protected static void setupConfiguration(TapestryFacetConfiguration conf, Module module, final TapestryVersion version) {
    conf.setVersion(version);
    if(StringUtil.isEmpty(conf.getFilterName())) conf.setFilterName(module.getName().toLowerCase());
    if(StringUtil.isEmpty(conf.getApplicationPackage())) conf.setApplicationPackage("com.app");
  }

  @Override
  @NotNull
  public List<FrameworkVersion> getVersions() {
    List<FrameworkVersion> result = new ArrayList<FrameworkVersion>();
    for (TapestryVersion version : TapestryVersion.values()) {
      final String name = version.toString();

      result.add(new FrameworkVersion(name, "tapestry-" + name, TapestryFacetConfiguration.getLibraryInfos(name)));
    }
    return result;
  }

  @Override
  public String getTitle() {
    return "Tapestry";
  }

  @Override
  protected void onFacetCreated(final TapestryFacet facet, final ModifiableRootModel rootModel, final FrameworkVersion version) {
    final Project project = facet.getModule().getProject();
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final TapestryFacetConfiguration configuration = facet.getConfiguration();

        final NewFacetDialog newFacetDialog = new NewFacetDialog(configuration);
        final DialogBuilder builder = new DialogBuilder(project);

        builder.removeAllActions();
        builder.addOkAction();
        builder.setCenterPanel(newFacetDialog.getMainPanel());
        builder.setTitle("New Tapestry Support");
        builder.setButtonsAlignment(SwingConstants.CENTER);
        builder.setOkOperation(new Runnable() {

          public void run() {
            facet.getConfiguration();
            if (!Validators.isValidPackageName(newFacetDialog.getApplicationPackage())) {
              Messages.showErrorDialog("Invalid package!", CommonBundle.getErrorTitle());
              return;
            }
            configuration.setFilterName(newFacetDialog.getFilterName());
            configuration.setApplicationPackage(newFacetDialog.getApplicationPackage());

            builder.getWindow().dispose();
          }
        });
        builder.showModal(true);


        AddTapestrySupportUtil.addSupportInWriteCommandAction(rootModel.getModule(), configuration,
                                                              newFacetDialog.shouldGenerateStartupApplication(),
                                                              newFacetDialog.shouldGeneratePom());
      }
    });
  }

  @NotNull
  @Override
  public String[] getProjectCategories() {
    return new String[] {JavaWebProjectType.JAVA_WEB};
  }
}
