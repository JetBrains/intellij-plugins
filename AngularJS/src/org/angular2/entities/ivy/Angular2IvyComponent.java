// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.psi.PsiFile;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2DirectiveSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2IvyComponent extends Angular2IvyDirective implements Angular2Component {

  public Angular2IvyComponent(@NotNull Angular2IvyEntityDef.Component entityDef) {
    super(entityDef);
  }

  @Nullable
  @Override
  public PsiFile getTemplateFile() {
    return null;
  }

  @NotNull
  @Override
  public List<PsiFile> getCssFiles() {
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public List<Angular2DirectiveSelector> getNgContentSelectors() {
    // TODO Angular 9 d.ts metadata lacks this information
    // Try to fallback to metadata JSON information
    return Optional.ofNullable(getMetadataDirective())
      .map(directive -> tryCast(directive, Angular2Component.class))
      .map(Angular2Component::getNgContentSelectors)
      .orElseGet(Collections::emptyList);
  }
}
