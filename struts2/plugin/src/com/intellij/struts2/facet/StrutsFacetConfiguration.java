/*
 * Copyright 2007 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.struts2.facet.configuration.StrutsFileSet;
import com.intellij.struts2.facet.configuration.StrutsValidationConfiguration;
import com.intellij.struts2.facet.ui.FeaturesConfigurationTab;
import com.intellij.struts2.facet.ui.FileSetConfigurationTab;
import com.intellij.struts2.facet.ui.ValidationConfigurationTab;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides the configuration tabs and reads/stores settings.
 *
 * @author Yann CŽbron
 */
public class StrutsFacetConfiguration implements FacetConfiguration, ModificationTracker {

  // Filesets
  @NonNls
  private static final String FILESET = "fileset";
  @NonNls
  private static final String SET_ID = "id";
  @NonNls
  private static final String SET_NAME = "name";
  @NonNls
  private static final String SET_REMOVED = "removed";
  @NonNls
  private static final String FILE = "file";

  // Features
  @NonNls
  private static final String FEATURES = "features";

  @NonNls
  private static final String ERRORS_AS_WARNING = "errors_as_warnings";

  @NonNls
  private static final String VALIDATE_STRUTS = "validate_struts";

  @NonNls
  private static final String VALIDATE_VALIDATION = "validate_validation";

  // FileSetConfigurationTab
  private final Set<StrutsFileSet> myFileSets = new LinkedHashSet<StrutsFileSet>();


  // FeaturesConfigurationTab
  private final StrutsValidationConfiguration validationConfiguration = new StrutsValidationConfiguration();

  private long myModificationCount;

  /**
   * Gets the currently configured filesets.
   *
   * @return Filesets.
   */
  public Set<StrutsFileSet> getFileSets() {
    return myFileSets;
  }

  public StrutsValidationConfiguration getFeaturesConfiguration() {
    return validationConfiguration;
  }

  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext,
                                           final FacetValidatorsManager validatorsManager) {
    final FacetLibrariesValidator validator =
      FacetEditorsFactory.getInstance().createLibrariesValidator(LibraryInfo.EMPTY_ARRAY,
                                                                 new FacetLibrariesValidatorDescription("struts2"),
                                                                 editorContext,
                                                                 validatorsManager);
    validatorsManager.registerValidator(validator);

    return new FacetEditorTab[]{new FileSetConfigurationTab(this, editorContext),
                                new FeaturesConfigurationTab(editorContext, validator),
                                new ValidationConfigurationTab(validator, validationConfiguration)};
  }

  public void readExternal(final Element element) throws InvalidDataException {
    for (final Object setElement : element.getChildren(FILESET)) {
      final String setName = ((Element) setElement).getAttributeValue(SET_NAME);
      final String setId = ((Element) setElement).getAttributeValue(SET_ID);
      final String removed = ((Element) setElement).getAttributeValue(SET_REMOVED);
      if (setName != null && setId != null) {
        final StrutsFileSet fileSet = new StrutsFileSet(setId, setName);
        final List files = ((Element) setElement).getChildren(FILE);
        for (final Object fileElement : files) {
          final String text = ((Element) fileElement).getText();
          fileSet.addFile(text);
        }
        fileSet.setRemoved(Boolean.valueOf(removed));
        myFileSets.add(fileSet);
      }
    }

    final Element features = element.getChild(FEATURES);
    validationConfiguration.setReportErrorsAsWarning(Boolean.valueOf(features.getChild(ERRORS_AS_WARNING).getText()));
    validationConfiguration.setValidateStruts(Boolean.valueOf(features.getChild(VALIDATE_STRUTS).getText()));
    validationConfiguration.setValidateValidation(Boolean.valueOf(features.getChild(VALIDATE_VALIDATION).getText()));
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    for (final StrutsFileSet fileSet : myFileSets) {
      final Element setElement = new Element(FILESET);
      setElement.setAttribute(SET_ID, fileSet.getId());
      setElement.setAttribute(SET_NAME, fileSet.getName());
      setElement.setAttribute(SET_REMOVED, Boolean.toString(fileSet.isRemoved()));
      element.addContent(setElement);

      for (final VirtualFilePointer fileName : fileSet.getFiles()) {
        final Element fileElement = new Element(FILE);
        fileElement.setText(fileName.getUrl());
        setElement.addContent(fileElement);
      }
    }

    final Element features = new Element(FEATURES);

    final Element errorsAsWarnings = new Element(ERRORS_AS_WARNING);
    errorsAsWarnings.setText(Boolean.toString(validationConfiguration.isReportErrorsAsWarning()));
    features.addContent(errorsAsWarnings);

    final Element validateStruts = new Element(VALIDATE_STRUTS);
    validateStruts.setText(Boolean.toString(validationConfiguration.isValidateStruts()));
    features.addContent(validateStruts);

    final Element validateValidation = new Element(VALIDATE_VALIDATION);
    validateValidation.setText(Boolean.toString(validationConfiguration.isValidateValidation()));
    features.addContent(validateValidation);

    element.addContent(features);
  }

  public long getModificationCount() {
    return myModificationCount;
  }

  public void setModified() {
    myModificationCount++;
  }

}