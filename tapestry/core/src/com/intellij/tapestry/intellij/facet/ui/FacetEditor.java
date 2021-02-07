package com.intellij.tapestry.intellij.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.TapestryBundle;
import com.intellij.tapestry.intellij.facet.AddTapestrySupportUtil;
import com.intellij.tapestry.intellij.facet.TapestryFacet;
import com.intellij.tapestry.intellij.facet.TapestryFacetConfiguration;
import com.intellij.tapestry.intellij.facet.TapestryVersion;
import com.intellij.ui.components.BrowserLink;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FacetEditor extends FacetEditorTab {

    private JPanel _mainPanel;
    private JTextField _filterName;
    private JTextField _applicationPackage;
    private JPanel _descriptionPanel;
    private final TapestryFacetConfiguration _configuration;

    public FacetEditor(TapestryFacet facet, TapestryFacetConfiguration configuration) {
        _configuration = configuration;

        //Filter filter = IntellijWebDescriptorUtils.getTapestryFilter(facet.getWebFacet().getRoot());

        //_configuration.setFilterName(filter != null ? filter.getFilterName().getValue() : null);
        //_configuration.setApplicationPackage(IntellijWebDescriptorUtils.getApplicationPackage(facet.getWebFacet().getRoot()));

        if (_configuration.getFilterName() == null)
            _configuration.setFilterName(StringUtil.toLowerCase(facet.getModule().getName()));

        _filterName.setText(_configuration.getFilterName());
        _applicationPackage.setText(_configuration.getApplicationPackage());
    }

    @Override
    @Nls
    public String getDisplayName() {
      return TapestryBundle.message("configurable.FacetEditor.display.name");
    }

    @Override
    @NotNull
    public JComponent createComponent() {
        return _mainPanel;
    }

  @Override
  public void onFacetInitialized(@NotNull final Facet facet) {
    if (_configuration.getVersion() == null) _configuration.setVersion(TapestryVersion.TAPESTRY_5_3_6);

    AddTapestrySupportUtil.addSupportInWriteCommandAction(facet.getModule(), _configuration, false, false);
  }

  @Override
  public boolean isModified() {
        return !_filterName.getText().equals(_configuration.getFilterName()) ||
               !_applicationPackage.getText().equals(_configuration.getApplicationPackage());
    }

    @Override
    public void apply() {
        _configuration.setFilterName(_filterName.getText());
        _configuration.setApplicationPackage(_applicationPackage.getText());
    }

    @Override
    public void reset() {
        _filterName.setText(_configuration.getFilterName());
        _applicationPackage.setText(_configuration.getApplicationPackage());
    }

    @Override
    public void disposeUIResources() {
    }

    private void createUIComponents() {
        _descriptionPanel = new JPanel(new VerticalFlowLayout());
        _descriptionPanel.add(new JLabel("<html>Tapestry is an open-source framework for creating dynamic, robust, highly scalable web applications in Java.</html>"));
        _descriptionPanel.add(new BrowserLink("More about Tapestry", "http://tapestry.apache.org"));
    }
}
