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

package com.intellij.struts2.dom.inspection;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.ui.ValidationConfigurationSettings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for validation.xml/validators.xml files when running "Make Project".
 *
 * @author Yann CŽbron
 */
public class ValidatorModelValidator extends ValidatorBase {

  public ValidatorModelValidator() {
    super("Validator Model Validator", "Validating validator model...",
        ValidatorModelInspection.class, ValidatorConfigModelInspection.class);
  }

  protected boolean isValidationEnabledForModel(final ValidationConfigurationSettings validationConfigurationSettings) {
    return validationConfigurationSettings.isValidateValidation();
  }

  public Collection<VirtualFile> getFilesToProcess(final Project project, final CompileContext context) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final ValidatorManager validatorManager = ValidatorManager.getInstance(project);

    // collect all validation.xml files located in sources
    final Set<VirtualFile> files = new HashSet<VirtualFile>();
    for (final VirtualFile file : context.getCompileScope().getFiles(StdFileTypes.XML, true)) {
      if (file.getName().endsWith("-validation.xml")) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile instanceof XmlFile &&
            validatorManager.isValidationConfigFile((XmlFile) psiFile)) {
          files.add(file);
        }
      }
    }

    // add validator-config.xml if not default one from xwork.jar
    final CompileScope scope = context.getCompileScope();
    for (final Module module : scope.getAffectedModules()) {
      if (StrutsFacet.getInstance(module) != null) {
        final PsiFile psiFile = validatorManager.getValidatorConfigFile(module);
        if (psiFile != null &&
            validatorManager.isCustomValidatorsConfigFile(psiFile)) {
          files.add(psiFile.getVirtualFile());
        }
      }
    }

    return files;
  }

}