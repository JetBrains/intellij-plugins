/*
 * Copyright 2015 The authors
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

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.util.InspectionValidatorUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.containers.FactoryMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validator for validation.xml/validators.xml files when running "Build Project".
 *
 * @author Yann C&eacute;bron
 */
final class ValidatorModelValidator extends ValidatorBase {
  @NonNls
  private static final String FILENAME_EXTENSION_VALIDATION_XML = "-validation.xml";

  public ValidatorModelValidator() {
    super("Struts 2 Validation Model Validator", StrutsBundle.message("inspections.validator.model.validator"),
          StrutsBundle.message("inspections.validator.model.validator.progress")
    );
  }

  @Override
  public Collection<VirtualFile> getFilesToProcess(final Project project, final CompileContext context) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final ValidatorManager validatorManager = ValidatorManager.getInstance(project);

    // cache S2facet/validation settings per module
    final Map<Module, Boolean> enabledForModule =
      FactoryMap.create(module1 -> isEnabledForModule(module1) &&
                                   StrutsFacet.getInstance(module1) != null);

    // collect all validation.xml files located in sources of S2-modules
    final Set<VirtualFile> files = new HashSet<>();
    for (final VirtualFile file : context.getProjectCompileScope().getFiles(XmlFileType.INSTANCE, true)) {
      if (StringUtil.endsWith(file.getName(), FILENAME_EXTENSION_VALIDATION_XML)) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile instanceof XmlFile &&
            validatorManager.isValidatorsFile((XmlFile)psiFile)) {
          final Module module = ModuleUtilCore.findModuleForFile(file, project);
          if (module != null &&
              enabledForModule.get(module)) {
            files.add(file);
          }
        }
      }
    }

    // add validator-config.xml if not default one from xwork.jar
    final Set<VirtualFile> descriptorFiles = new HashSet<>();
    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      if (enabledForModule.get(module)) {
        final PsiFile psiFile = validatorManager.getValidatorConfigFile(module);
        if (psiFile != null &&
            validatorManager.isCustomValidatorConfigFile(psiFile)) {
          InspectionValidatorUtil.addFile(descriptorFiles, psiFile);
        }
      }
    }
    files.addAll(InspectionValidatorUtil.expandCompileScopeIfNeeded(descriptorFiles, context));

    return files;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends LocalInspectionTool> @NotNull [] getInspectionToolClasses(CompileContext context) {
    return new Class[]{ValidatorModelInspection.class, ValidatorConfigModelInspection.class};
  }
}