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

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.util.InspectionValidator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.ui.ValidationConfigurationSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base-class for validators.
 *
 * @author Yann C&eacute;bron
 */
public abstract class ValidatorBase extends InspectionValidator {

  protected ValidatorBase(@NotNull final String description,
                          @NotNull final String progressIndicatorText,
                          final Class<? extends LocalInspectionTool>... inspectionToolClasses) {
    super(description, progressIndicatorText, inspectionToolClasses);
  }

  /**
   * Determine whether to run validation for our model using the current facet configuration settings.
   *
   * @param validationConfigurationSettings Current facet settings.
   *
   * @return true if validation is enabled.
   */
  protected abstract boolean isValidationEnabledForModel(final ValidationConfigurationSettings validationConfigurationSettings);

  public final boolean isAvailableOnScope(@NotNull final CompileScope scope) {
    for (final Module module : scope.getAffectedModules()) {
      final StrutsFacet strutsFacet = StrutsFacet.getInstance(module);
      if (strutsFacet != null) {
        if (isValidationEnabledForModel(strutsFacet.getConfiguration().getValidationConfigurationSettings())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Handles {@link com.intellij.struts2.facet.ui.ValidationConfigurationSettings#isReportErrorsAsWarning()}.
   *
   * @param severity    Display severity.
   * @param virtualFile Current file.
   * @param context     Current compile context.
   *
   * @return Either {@link com.intellij.openapi.compiler.CompilerMessageCategory#ERROR} or {@link com.intellij.openapi.compiler.CompilerMessageCategory#WARNING}.
   */
  public final CompilerMessageCategory getCategoryByHighlightDisplayLevel(@NotNull final HighlightDisplayLevel severity,
                                                                          @NotNull final VirtualFile virtualFile,
                                                                          @NotNull final CompileContext context) {
    final CompilerMessageCategory level = super.getCategoryByHighlightDisplayLevel(severity, virtualFile, context);
    if (level == CompilerMessageCategory.ERROR) {
      final Module module = context.getModuleByFile(virtualFile);
      if (module != null) {
        final StrutsFacet facet = StrutsFacet.getInstance(module);
        if (facet != null) {
          return facet.getConfiguration().getValidationConfigurationSettings().isReportErrorsAsWarning() ?
                 CompilerMessageCategory.WARNING : level;
        }
        final List<Module> dependentModules = ModuleUtil.getAllDependentModules(module);
        for (final Module dependentModule : dependentModules) {
          final StrutsFacet strutsFacet = StrutsFacet.getInstance(dependentModule);
          if (strutsFacet != null) {
            return strutsFacet.getConfiguration().getValidationConfigurationSettings().isReportErrorsAsWarning() ?
                   CompilerMessageCategory.WARNING : level;
          }
        }
      }
    }
    return level;
  }

}