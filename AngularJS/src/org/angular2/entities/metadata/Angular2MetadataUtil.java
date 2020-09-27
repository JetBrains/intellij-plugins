// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import org.angular2.entities.Angular2DirectiveProperties;
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.angular2.index.Angular2MetadataClassNameIndex;
import org.angular2.index.Angular2MetadataFunctionIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.entities.Angular2EntitiesProvider.isDeclaredClass;

public final class Angular2MetadataUtil {
  public static @Nullable Angular2MetadataFunction findMetadataFunction(@NotNull JSFunction function) {
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


  public static Angular2MetadataEntity<?> getMetadataEntity(@NotNull TypeScriptClass typeScriptClass) {
    return getMetadataClass(typeScriptClass, Angular2MetadataEntity.class);
  }

  private static <T extends Angular2MetadataClassBase<?>> T getMetadataClass(@NotNull TypeScriptClass typeScriptClass, Class<T> clazz) {
    String className = typeScriptClass.getName();
    if (className == null
        //check classes only from d.ts files
        || !isDeclaredClass(typeScriptClass)) {
      return null;
    }
    Ref<T> result = new Ref<>();
    StubIndex.getInstance().processElements(
      Angular2MetadataClassNameIndex.KEY, className, typeScriptClass.getProject(),
      GlobalSearchScope.allScope(typeScriptClass.getProject()), Angular2MetadataClassBase.class,
      e -> {
        T casted = tryCast(e, clazz);
        if (casted != null && casted.isValid() && casted.getTypeScriptClass() == typeScriptClass) {
          result.set(casted);
          return false;
        }
        return true;
      });
    return result.get();
  }

  public static @Nullable Angular2DirectiveProperties getMetadataClassDirectiveProperties(@NotNull TypeScriptClass typeScriptClass) {
    Angular2MetadataClassBase<?> classBase = getMetadataClass(typeScriptClass, Angular2MetadataClassBase.class);
    return classBase != null ? classBase.getBindings() : null;
  }
}
