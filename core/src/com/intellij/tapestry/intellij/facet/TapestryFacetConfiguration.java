package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.tapestry.intellij.facet.ui.FacetEditor;
import org.jdom.Element;

public class TapestryFacetConfiguration implements FacetConfiguration {

    private String _filterName;
    private String _applicationPackage;
    private TapestryVersion _version;

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        FacetLibrariesValidator validator = FacetEditorsFactory.getInstance().createLibrariesValidator(TapestryVersion.TAPESTRY_5_0_11.getJars(), new TapestryLibrariesValidatorDescription(), editorContext, validatorsManager);

        validatorsManager.registerValidator(validator);

        return new FacetEditorTab[]{new FacetEditor((TapestryFacet) editorContext.getFacet(), this)};
    }

    public void readExternal(Element element) throws InvalidDataException {
        _filterName = JDOMExternalizer.readString(element, "filterName");
        _applicationPackage = JDOMExternalizer.readString(element, "applicationPackage");
        _version = TapestryVersion.fromString(JDOMExternalizer.readString(element, "version"));
    }

    public void writeExternal(Element element) throws WriteExternalException {
        JDOMExternalizer.write(element, "filterName", _filterName);
        JDOMExternalizer.write(element, "applicationPackage", _applicationPackage);
        JDOMExternalizer.write(element, "version", _version != null ? _version.toString() : null);
    }

    public String getFilterName() {
        return _filterName;
    }

    public void setFilterName(String filterName) {
        _filterName = filterName;
    }

    public String getApplicationPackage() {
        return _applicationPackage;
    }

    public void setApplicationPackage(String applicationPackage) {
        _applicationPackage = applicationPackage;
    }

    public TapestryVersion getVersion() {
        return _version;
    }

    public void setVersion(TapestryVersion version) {
        _version = version;
    }
}
