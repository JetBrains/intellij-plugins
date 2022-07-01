// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSPrimitiveLiteralType;
import com.intellij.psi.util.CachedValueProvider;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2Declaration;
import org.jetbrains.annotations.NotNull;

public abstract class Angular2SourceDeclaration extends Angular2SourceEntity implements Angular2Declaration {

  public Angular2SourceDeclaration(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Override
  public boolean isStandalone() {
    return getCachedValue(() -> {
      JSProperty property = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2DecoratorUtil.STANDALONE_PROP);
      var type = property != null ? property.getJSType() : null;
      var result = type instanceof JSPrimitiveLiteralType && Boolean.TRUE.equals(((JSPrimitiveLiteralType<?>)type).getLiteral()) ;
      return CachedValueProvider.Result.create(result, getDecorator());
    });
  }
}
