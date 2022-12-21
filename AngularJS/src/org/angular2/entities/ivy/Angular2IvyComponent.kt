// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.model.Pointer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2DirectiveKind;
import org.angular2.entities.Angular2DirectiveSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2IvyComponent extends Angular2IvyDirective implements Angular2Component {

  private static final Key<List<Angular2DirectiveSelector>> IVY_NG_CONTENT_SELECTORS = new Key<>("ng.ivy.content-selectors");

  public Angular2IvyComponent(@NotNull Angular2IvySymbolDef.Component entityDef) {
    super(entityDef);
  }

  @Override
  public @NotNull Pointer<Angular2IvyComponent> createPointer() {
    var entityDef = myEntityDef.createPointer();
    return () -> {
      var newEntityDef = tryCast(entityDef.dereference(), Angular2IvySymbolDef.Component.class);
      return newEntityDef != null ? new Angular2IvyComponent(newEntityDef) : null;
    };
  }

  @Override
  public @Nullable PsiFile getTemplateFile() {
    return null;
  }

  @Override
  public @NotNull Angular2DirectiveKind getDirectiveKind() {
    return Angular2DirectiveKind.REGULAR;
  }

  @Override
  public @NotNull List<PsiFile> getCssFiles() {
    return Collections.emptyList();
  }

  @Override
  public @NotNull List<Angular2DirectiveSelector> getNgContentSelectors() {
    List<Angular2DirectiveSelector> result = getNullableLazyValue(
      IVY_NG_CONTENT_SELECTORS,
      () -> doIfNotNull(
        ((Angular2IvySymbolDef.Component)myEntityDef).getNgContentSelectors(),
        contentSelectors -> ContainerUtil.map(contentSelectors, this::createSelectorFromStringLiteralType))
    );
    if (result != null) {
      return result;
    }
    // Try to fallback to metadata JSON information - Angular 9.0.x case
    return Optional.ofNullable(getMetadataDirective(myClass))
      .map(directive -> tryCast(directive, Angular2Component.class))
      .map(Angular2Component::getNgContentSelectors)
      .orElseGet(Collections::emptyList);
  }
}
