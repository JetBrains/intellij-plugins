// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.intellij.util.ObjectUtils.doIfNotNull;

/**
 * This class is intended to be a single point of origin for any hack to support a badly written library.
 */
public class Angular2LibrariesHacks {

  @NonNls private static final String IONIC_ANGULAR_PACKAGE = "@ionic/angular";
  @NonNls private static final String NG_MODEL_CHANGE = "ngModelChange";
  @NonNls private static final String EVENT_EMITTER = "EventEmitter";

  /** Hack for WEB-37879 */
  @Nullable
  public static JSType hackNgModelChangeType(@Nullable JSType type, @NotNull String propertyName) {
    if (type != null
        // Workaround issue with ngModelChange field.
        // The workaround won't execute once Angular source is corrected.
        && propertyName.equals(NG_MODEL_CHANGE)
        && type instanceof JSRecordType
        && !((JSRecordType)type).hasProperties()) {
      return JSAnyType.get(type.getSource());
    }
    return type;
  }

  /** Hack for WEB-37838 */
  public static void hackIonicComponentOutputs(@NotNull Angular2MetadataDirectiveBase directive, @NotNull Map<String, String> outputs) {
    if (!IONIC_ANGULAR_PACKAGE.equals(doIfNotNull(directive.getNodeModule(), Angular2MetadataNodeModule::getName))) {
      return;
    }
    TypeScriptClass cls = directive.getTypeScriptClass();
    if (cls == null) {
      return;
    }
    // We can guess outputs by looking for fields with EventEmitter type
    cls.getJSType().asRecordType().getProperties().forEach(prop -> {
      try {
        JSType type;
        if (prop instanceof TypeScriptField
            && (type = prop.getJSType()) != null
            && type.getTypeText().startsWith(EVENT_EMITTER)) {
          outputs.put(prop.getMemberName(), prop.getMemberName());
        }
      }
      catch (IllegalArgumentException ex) {
        //getTypeText may throw IllegalArgumentException - ignore it
      }
    });
  }
}
