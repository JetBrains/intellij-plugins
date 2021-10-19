// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.model.Pointer;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;
import static org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement;

public abstract class Angular2SourceEntity extends Angular2SourceEntityBase {

  private final ES6Decorator myDecorator;
  private final JSImplicitElement myImplicitElement;

  public Angular2SourceEntity(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(Objects.requireNonNull(getClassForDecoratorElement(decorator)));
    myDecorator = decorator;
    myImplicitElement = implicitElement;
  }

  protected <T extends Angular2SourceEntity> Pointer<T> createPointer(
    @NotNull BiFunction<? super ES6Decorator, ? super JSImplicitElement, T> constructor) {
    var decoratorPtr = createSmartPointer(myDecorator);
    var implicitElementPtr = createSmartPointer(myImplicitElement);
    return () ->{
      var decorator = decoratorPtr.dereference();
      var implicitElement = implicitElementPtr.dereference();
      return decorator != null && implicitElement != null ? constructor.apply(decorator, implicitElement) : null;
    };
  }

  @Override
  public @NotNull JSElement getNavigableElement() {
    return myDecorator;
  }

  @Override
  public @NotNull JSElement getSourceElement() {
    // try to find a fresh implicit element
    return StreamEx.ofNullable(myDecorator.getIndexingData())
      .map(JSElementIndexingData::getImplicitElements)
      .nonNull()
      .flatCollection(Function.identity())
      .filter(el -> Objects.equals(myImplicitElement.getName(), el.getName())
                    && Objects.equals(myImplicitElement.getUserString(), el.getUserString()))
      .findFirst()
      .orElse(myImplicitElement);
  }

  @Override
  public @NotNull ES6Decorator getDecorator() {
    return myDecorator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2SourceEntity entity = (Angular2SourceEntity)o;
    return myDecorator.equals(entity.myDecorator) &&
           myClass.equals(entity.myClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myDecorator, myClass);
  }
}
