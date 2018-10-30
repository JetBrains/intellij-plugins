// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.Pair;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.Angular2Pipe;
import org.angular2.entities.metadata.stubs.Angular2MetadataPipeStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;

public class Angular2MetadataPipe extends Angular2MetadataDeclaration<Angular2MetadataPipeStub> implements Angular2Pipe {

  public Angular2MetadataPipe(@NotNull Angular2MetadataPipeStub element) {
    super(element);
  }

  @NotNull
  @Override
  public String getName() {
    return getStub().getPipeName();
  }

  @NotNull
  @Override
  public Collection<? extends TypeScriptFunction> getTransformMethods() {
    return getCachedValue(() -> {
      Pair<TypeScriptClass, Collection<Object>> pair = getClassAndDependencies();
      return create(notNull(doIfNotNull(pair.first, Angular2EntityUtils::getPipeTransformMethods),
                            Collections::emptyList), pair.second);
    });
  }

  @Nullable
  @Override
  public Angular2Module getModule() {
    return null;
  }
}
