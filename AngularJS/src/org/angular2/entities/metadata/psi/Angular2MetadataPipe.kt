// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.Angular2Pipe;
import org.angular2.entities.metadata.stubs.Angular2MetadataPipeStub;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class Angular2MetadataPipe extends Angular2MetadataDeclaration<Angular2MetadataPipeStub> implements Angular2Pipe {

  public Angular2MetadataPipe(@NotNull Angular2MetadataPipeStub element) {
    super(element);
  }

  @Override
  public @NotNull String getName() {
    return getStub().getPipeName();
  }

  @Override
  public @NotNull Collection<? extends TypeScriptFunction> getTransformMethods() {
    return getCachedClassBasedValue(cls -> cls != null
                                           ? Angular2EntityUtils.getPipeTransformMethods(cls)
                                           : Collections.emptyList());
  }
}
