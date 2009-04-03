package com.intellij.tapestry.intellij.facet;

import com.intellij.CommonBundle;
import com.intellij.facet.impl.ui.FacetTypeFrameworkSupportProvider;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
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

    protected void setupConfiguration(TapestryFacet tapestryFacet, ModifiableRootModel modifiableRootModel, String s) {
    }

    @NotNull
    public String[] getVersions() {
        List<String> versions = new ArrayList<String>();

        for (TapestryVersion version : TapestryVersion.values()) {
            versions.add(version.toString());
        }

        return versions.toArray(new String[versions.size()]);
    }

    public String getTitle() {
        return "Tapestry";
    }

    @NotNull
    @NonNls
    protected String getLibraryName(final String selectedVersion) {
        return "tapestry-" + selectedVersion;
    }

    protected void onLibraryAdded(final TapestryFacet facet, final @NotNull Library library) {
        facet.getWebFacet().getPackagingConfiguration().addLibraryLink(library);
    }

    @NotNull
    protected LibraryInfo[] getLibraries(String selectedVersion) {
        TapestryVersion version = getVersion(selectedVersion);

        if (version != null)
            return version.getJars();
        else
            return LibraryInfo.EMPTY_ARRAY;
    }

    protected void onFacetCreated(final TapestryFacet facet, final ModifiableRootModel rootModel, final String version) {
        StartupManager.getInstance(facet.getModule().getProject()).runWhenProjectIsInitialized(new Runnable() {
            public void run() {
                final TapestryFacetConfiguration configuration = new TapestryFacetConfiguration();
                configuration.setFilterName(rootModel.getModule().getName().toLowerCase());
                configuration.setVersion(getVersion(version));

                final NewFacetDialog newFacetDialog = new NewFacetDialog();
                final DialogBuilder builder = new DialogBuilder(rootModel.getModule().getProject());

                builder.setCenterPanel(newFacetDialog.getMainPanel());
                builder.setTitle("New Tapestry Support");
                builder.setButtonsAlignment(SwingConstants.CENTER);
                builder.setOkOperation(new Runnable() {

                    public void run() {
                        if (newFacetDialog.getFilterName() == null || newFacetDialog.getFilterName().length() == 0) {
                            Messages.showErrorDialog("You must provide a filter name!", CommonBundle.getErrorTitle());
                            return;
                        }

                        if (!Validators.isValidPackageName(newFacetDialog.getApplicationPackage())) {
                            Messages.showErrorDialog("Invalid package!", CommonBundle.getErrorTitle());
                            return;
                        }

                        configuration.setFilterName(newFacetDialog.getFilterName());
                        configuration.setApplicationPackage(newFacetDialog.getApplicationPackage());

                        builder.getWindow().dispose();
                    }
                });

                builder.setCancelOperation(new Runnable() {

                    public void run() {
                        newFacetDialog.setGenerateStartupApplication(false);

                        builder.getWindow().dispose();
                    }
                });

                builder.showModal(true);

                AddTapestrySupportUtil.addSupportInWriteCommandAction(rootModel, facet.getWebFacet(), configuration, newFacetDialog.shouldGenerateStartupApplication(), newFacetDialog.shouldGeneratePom());
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
