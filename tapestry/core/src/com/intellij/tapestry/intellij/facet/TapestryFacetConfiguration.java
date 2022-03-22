package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.frameworks.LibrariesDownloadAssistant;
import com.intellij.facet.frameworks.beans.Artifact;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.tapestry.intellij.facet.ui.FacetEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public final class TapestryFacetConfiguration implements FacetConfiguration {
  private String _filterName;
  private String _applicationPackage;
  private TapestryVersion _version;

  @Override
  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    FacetLibrariesValidator validator = FacetEditorsFactory.getInstance()
        .createLibrariesValidator(getLibraryInfos(TapestryVersion.TAPESTRY_5_3_6.toString()), new TapestryLibrariesValidatorDescription(), editorContext,
                                  validatorsManager);

    validatorsManager.registerValidator(validator);

    return new FacetEditorTab[]{new FacetEditor((TapestryFacet)editorContext.getFacet(), this)};
  }

  public static LibraryInfo @NotNull [] getLibraryInfos(@NotNull String versionId) {
    final Artifact version = LibrariesDownloadAssistant.findVersion(versionId, getUrl());

    if (version != null) {
      return LibrariesDownloadAssistant.getLibraryInfos(version);
    }
    return LibraryInfo.EMPTY_ARRAY;
  }

  private static URL getUrl() {
    return TapestryFacetConfiguration.class.getResource("/libraries/tapestry.xml");
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    _filterName = JDOMExternalizer.readString(element, "filterName");
    _applicationPackage = JDOMExternalizer.readString(element, "applicationPackage");
    _version = TapestryVersion.fromString(JDOMExternalizer.readString(element, "version"));
  }

  @Override
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
