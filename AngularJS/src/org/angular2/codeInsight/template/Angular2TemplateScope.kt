// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class Angular2TemplateScope {

  private final @Nullable Angular2TemplateScope myParent;
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

  public final @Nullable Angular2TemplateScope getParent() {
    return myParent;
  }

  public final @NotNull List<Angular2TemplateScope> getChildren() {
    return Collections.unmodifiableList(children);
  }

  private void add(Angular2TemplateScope scope) {
    this.children.add(scope);
  }

  /**
   * This method is called on every provided scope and allows for providing resolve results from enclosing scopes.
   */
  public final boolean resolveAllScopesInHierarchy(@NotNull Processor<? super ResolveResult> processor) {
    Angular2TemplateScope scope = this;
    Ref<Boolean> found = new Ref<>(false);
    Consumer<? super ResolveResult> consumer = resolveResult -> {
      if (!processor.process(resolveResult)) {
        found.set(true);
      }
    };
    while (scope != null && found.get() != Boolean.TRUE) {
      scope.resolve(consumer);
      scope = scope.getParent();
    }
    return found.get() == Boolean.TRUE;
  }

  public abstract void resolve(@NotNull Consumer<? super ResolveResult> consumer);
}
