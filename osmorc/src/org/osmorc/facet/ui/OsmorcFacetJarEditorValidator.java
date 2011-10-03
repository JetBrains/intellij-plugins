package org.osmorc.facet.ui;

import com.intellij.facet.impl.ProjectFacetsConfigurator;
import com.intellij.facet.impl.ui.FacetEditorImpl;
import com.intellij.facet.ui.FacetConfigurationQuickFix;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;

import javax.swing.*;

public class OsmorcFacetJarEditorValidator extends FacetEditorValidator {
  private FacetEditorContext myEditorContext;
  private OsmorcFacetJAREditorTab myJarEditorTab;

  public OsmorcFacetJarEditorValidator(FacetEditorContext editorContext, OsmorcFacetJAREditorTab jarEditorTab) {
    myEditorContext = editorContext;
    myJarEditorTab = jarEditorTab;
  }

  @Nullable
  private FacetEditorImpl getFacetEditor() {
    FacetsProvider facetsProvider = myEditorContext.getFacetsProvider();
    OsmorcFacet osmorcFacet = (OsmorcFacet)myEditorContext.getFacet();
    ProjectFacetsConfigurator projectFacetsConfigurator = (ProjectFacetsConfigurator)facetsProvider;
    return projectFacetsConfigurator.getEditor(osmorcFacet);
  }

  @Override
  public ValidationResult check() {
    OsmorcFacetConfiguration.OutputPathType type = myJarEditorTab.getSelectedOutputPathType();
    if (type == OsmorcFacetConfiguration.OutputPathType.SpecificOutputPath) {
      String path = myJarEditorTab.getSelectedOutputPath();
      if (path.length() == 0) {
        return new ValidationResult("You need to specify an output path for your bundle.", new FacetConfigurationQuickFix() {
          @Override
          public void run(JComponent place) {
            FacetEditorImpl facetEditor = getFacetEditor();
            if (facetEditor != null) {
              facetEditor.setSelectedTabName(myJarEditorTab.getDisplayName());
              myJarEditorTab.onOutputPathSelect();
            }
          }
        });
      }
    }
    if (StringUtil.isEmptyOrSpaces(myJarEditorTab.getJarFileName())) {
      return new ValidationResult("You need to specify a filename for the JAR file.");
    }
    return ValidationResult.OK;
  }
}
