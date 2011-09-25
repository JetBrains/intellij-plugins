package com.intellij.flexIde;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.artifacts.ArtifactsStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.FacetStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.GlobalLibrariesConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectLibrariesConfigurable;

import javax.swing.*;

/**
 * User: ksafonov
 */
public class FlexIdeProjectStructureConfigurable extends ProjectStructureConfigurable {
  
  public FlexIdeProjectStructureConfigurable(final Project project,
                                             final ProjectLibrariesConfigurable projectLibrariesConfigurable,
                                             final GlobalLibrariesConfigurable globalLibrariesConfigurable,
                                             final ModuleStructureConfigurable moduleStructureConfigurable,
                                             FacetStructureConfigurable facetStructureConfigurable,
                                             ArtifactsStructureConfigurable artifactsStructureConfigurable) {
    super(project, projectLibrariesConfigurable, globalLibrariesConfigurable, moduleStructureConfigurable, facetStructureConfigurable,
          artifactsStructureConfigurable);
    myUiState.lastEditedConfigurable = moduleStructureConfigurable.getDisplayName();
  }

  @Override
  public JComponent createComponent() {
    JComponent c = super.createComponent();
    hideSidePanel();
    return c;
  }
}
