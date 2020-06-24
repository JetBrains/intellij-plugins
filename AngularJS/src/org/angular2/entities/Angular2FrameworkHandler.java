// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Angular framework handler allows to tailor Angular plugin behaviour to properly support a particular framework.
 */
@ApiStatus.Experimental
public interface Angular2FrameworkHandler {

  ExtensionPointName<Angular2FrameworkHandler>
    EP_NAME = ExtensionPointName.create("org.angular2.frameworkHandler");

  /**
   * Contribute additional component classes to the template context of external template file. This is required when
   * template file name is not the same as the one linked in the component's templateUrl property. Only the first component
   * in the list will be used for building the template context.
   */
  default @NotNull List<TypeScriptClass> findAdditionalComponentClasses(@NotNull PsiFile context) {
    return Collections.emptyList();
  }

  /**
   * When there are multiple modules in which component is included, the one with the first name is chosen by default. Framework handler can
   * decide which module should be chosen in a particular context. E.g. in NativeScript modules should have similar suffix in the
   * file name as the template file name.
   */
  default @Nullable Angular2Module selectModuleForDeclarationsScope(@NotNull Collection<@NotNull Angular2Module> modules,
                                                                    @NotNull Angular2Component component,
                                                                    @NotNull PsiFile context) {
    return null;
  }

  /**
   * In some specific cases Angular declaration is included in many modules (like components in NativeScript), framework handler can
   * suppress incorrect inspection error in such a case for any declaration.
   */
  default boolean suppressModuleInspectionErrors(@NotNull Collection<@NotNull Angular2Module> modules,
                                                 @NotNull Angular2Declaration declaration) {
    return false;
  }
}
