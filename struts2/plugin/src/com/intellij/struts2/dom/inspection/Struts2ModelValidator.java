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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.struts2.facet.ui.ValidationConfigurationSettings;
import com.intellij.util.containers.ContainerUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for struts.xml files when running "Make Project".
 *
 * @author Yann Cebron
 */
public class Struts2ModelValidator extends ValidatorBase {

  public Struts2ModelValidator() {
    super("Struts 2 Model Validator", "Validating Struts 2 model...", Struts2ModelInspection.class);
  }

  protected boolean isValidationEnabledForModel(final ValidationConfigurationSettings validationConfigurationSettings) {
    return validationConfigurationSettings.isValidateStruts();
  }

  public Collection<VirtualFile> getFilesToProcess(final Project project, final CompileContext context) {
    final StrutsManager strutsManager = StrutsManager.getInstance(project);

    final Set<VirtualFile> files = new HashSet<VirtualFile>();
    final CompileScope scope = context.getCompileScope();
    for (final Module module : scope.getAffectedModules()) {
      final StrutsFacet strutsFacet = StrutsFacet.getInstance(module);
      if (strutsFacet != null) {
        for (final StrutsFileSet fileSet : strutsManager.getAllConfigFileSets(module)) {
          for (final VirtualFilePointer pointer : fileSet.getFiles()) {
            final VirtualFile file = pointer.getFile();
            ContainerUtil.addIfNotNull(file, files);
          }
        }
      }
    }

    return files;
  }

}