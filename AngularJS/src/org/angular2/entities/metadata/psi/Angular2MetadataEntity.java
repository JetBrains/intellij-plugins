// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.NullableConsumer;
import com.intellij.util.containers.Stack;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataEntityStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public abstract class Angular2MetadataEntity<Stub extends Angular2MetadataEntityStub<?>> extends Angular2MetadataClassBase<Stub> implements
                                                                                                                                 Angular2Entity {

  public Angular2MetadataEntity(@NotNull Stub element) {
    super(element);
  }

  @Override
  public @NotNull PsiElement getNavigableElement() {
    return getSourceElement();
  }

  @Override
  public @Nullable ES6Decorator getDecorator() {
    return null;
  }


  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  protected static void collectReferencedElements(@NotNull PsiElement root,
                                                  @NotNull NullableConsumer<? super PsiElement> consumer,
                                                  @Nullable Set<PsiElement> cacheDependencies) {
    Stack<PsiElement> resolveQueue = new Stack<>(root);
    Set<PsiElement> visited = new HashSet<>();
    while (!resolveQueue.empty()) {
      ProgressManager.checkCanceled();
      PsiElement element = resolveQueue.pop();
      if (element != null && !visited.add(element)) {
        // Protect against cyclic references or visiting same thing several times
        continue;
      }
      if (cacheDependencies != null && element != null) {
        cacheDependencies.add(element);
      }
      if (element instanceof Angular2MetadataArray) {
        resolveQueue.addAll(asList(element.getChildren()));
      }
      else if (element instanceof Angular2MetadataReference) {
        resolveQueue.push(((Angular2MetadataReference)element).resolve());
      }
      else if (element instanceof Angular2MetadataCall) {
        resolveQueue.push(((Angular2MetadataCall)element).getValue());
      }
      else if (element instanceof Angular2MetadataSpread) {
        resolveQueue.push(((Angular2MetadataSpread)element).getExpression());
      }
      else {
        consumer.consume(element);
      }
    }
  }
}
