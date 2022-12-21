// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.model.Pointer;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.angular2.entities.Angular2EntityUtils.forEachEntity;

public interface Angular2Component extends Angular2Directive, Angular2ImportsOwner {

  @Override
  @NotNull Pointer<? extends Angular2Component> createPointer();

  @Nullable
  PsiFile getTemplateFile();

  @NotNull
  List<PsiFile> getCssFiles();

  @NotNull
  List<Angular2DirectiveSelector> getNgContentSelectors();

  @Override
  default boolean isComponent() {
    return true;
  }

  @Override
  @NotNull
  default Set<Angular2Entity> getImports() {
    return Set.of();
  }

  /**
   * @see Angular2Module#getDeclarationsInScope()
   */
  default @NotNull Set<Angular2Declaration> getDeclarationsInScope() {
    Set<Angular2Declaration> result = new HashSet<>();
    result.add(this); // for self-reference
    forEachEntity(
      getImports(),
      module -> result.addAll(module.getAllExportedDeclarations()),
      declaration -> {
        if (declaration.isStandalone()) result.add(declaration);
      }
    );
    return result;
  }
}
