// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.angular2.index.Angular2MetadataFunctionIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2MetadataUtil {
  @Nullable
  public static Angular2MetadataFunction findMetadataFunction(@NotNull JSFunction function) {
    TypeScriptClass parent = tryCast(function.getContext(), TypeScriptClass.class);
    if (function.getName() == null || parent == null) {
      return null;
    }
    Ref<Angular2MetadataFunction> result = new Ref<>();
    StubIndex.getInstance().processElements(
      Angular2MetadataFunctionIndex.KEY, function.getName(), function.getProject(),
      GlobalSearchScope.allScope(function.getProject()),
      Angular2MetadataFunction.class, (f) -> {
        if (f.isValid()) {
          Angular2MetadataClassBase<?> parentClass = tryCast(f.getContext(), Angular2MetadataClassBase.class);
          if (parentClass != null && parent.equals(parentClass.getTypeScriptClass())) {
            result.set(f);
            return false;
          }
        }
        return true;
      });
    return result.get();
  }
}
