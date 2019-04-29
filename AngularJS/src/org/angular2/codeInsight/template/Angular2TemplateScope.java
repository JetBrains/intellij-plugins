// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class Angular2TemplateScope {

  @Nullable
  private final Angular2TemplateScope myParent;
  private final List<Angular2TemplateScope> children = new ArrayList<>();

  /**
   * A scope can be created with parent scope, which contents will be included in the resolution.
   * See {@link Angular2TemplateScope#resolveAllScopesInHierarchy}
   */
  protected Angular2TemplateScope(@Nullable Angular2TemplateScope parent) {
    myParent = parent;
    if (parent != null) {
      parent.add(this);
    }
  }

  @Nullable
  public final Angular2TemplateScope getParent() {
    return myParent;
  }

  @NotNull
  public final List<Angular2TemplateScope> getChildren() {
    return Collections.unmodifiableList(children);
  }

  private void add(Angular2TemplateScope scope) {
    this.children.add(scope);
  }

  /**
   * This method is called on every provided scope and allows for providing resolve results from enclosing scopes.
   */
  public final void resolveAllScopesInHierarchy(@NotNull Consumer<? super ResolveResult> consumer) {
    Angular2TemplateScope scope = this;
    while (scope != null) {
      scope.resolve(consumer);
      scope = scope.getParent();
    }
  }

  public abstract void resolve(@NotNull Consumer<? super ResolveResult> consumer);
}
