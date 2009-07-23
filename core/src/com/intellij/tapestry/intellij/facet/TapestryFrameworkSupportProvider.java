package com.intellij.tapestry.intellij.facet;

import com.intellij.CommonBundle;
import com.intellij.facet.impl.ui.FacetTypeFrameworkSupportProvider;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.intellij.facet.ui.NewFacetDialog;
import com.intellij.tapestry.intellij.util.Validators;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TapestryFrameworkSupportProvider extends FacetTypeFrameworkSupportProvider<TapestryFacet> {

  public TapestryFrameworkSupportProvider() {
    super(TapestryFacetType.INSTANCE);
  }

  protected void setupConfiguration(TapestryFacet tapestryFacet, ModifiableRootModel modifiableRootModel, String version) {
    TapestryFacetConfiguration conf = tapestryFacet.getConfiguration();
    conf.setVersion(getVersion(version));

    if(StringUtil.isEmpty(conf.getFilterName())) conf.setFilterName(tapestryFacet.getModule().getName().toLowerCase());
    if(StringUtil.isEmpty(conf.getApplicationPackage())) conf.setApplicationPackage("com.app");
  }

  @Override
  @NotNull
  public String[] getVersions() {
    List<String> versions = new ArrayList<String>();

    for (TapestryVersion version : TapestryVersion.values()) {
      versions.add(version.toString());
    }

    return versions.toArray(new String[versions.size()]);
  }

  @Override
  public String getTitle() {
    return "Tapestry";
  }

  @Override
  @NotNull
  @NonNls
  protected String getLibraryName(final String selectedVersion) {
    return "tapestry-" + selectedVersion;
  }

  @Override
  protected void onLibraryAdded(final TapestryFacet facet, final @NotNull Library library) {
  }

  @NotNull
  protected LibraryInfo[] getLibraries(String selectedVersion) {
    TapestryVersion version = getVersion(selectedVersion);
    return version != null ? version.getJars() : LibraryInfo.EMPTY_ARRAY;
  }

  @Override
  protected void onFacetCreated(final TapestryFacet facet, final ModifiableRootModel rootModel, final String version) {
    StartupManager.getInstance(facet.getModule().getProject()).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final TapestryFacetConfiguration configuration = facet.getConfiguration();

        final NewFacetDialog newFacetDialog = new NewFacetDialog(configuration);
        final DialogBuilder builder = new DialogBuilder(rootModel.getModule().getProject());

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

        AddTapestrySupportUtil.addSupportInWriteCommandAction(rootModel, configuration,
                                                              newFacetDialog.shouldGenerateStartupApplication(),
                                                              newFacetDialog.shouldGeneratePom());
      }
    });
  }

  private static TapestryVersion getVersion(String versionName) {
    for (TapestryVersion version : TapestryVersion.values()) {
      if (versionName.equals(version.toString())) {
        return version;
      }
    }

    return null;
  }
}
