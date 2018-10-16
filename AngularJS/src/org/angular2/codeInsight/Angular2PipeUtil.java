// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.jetbrains.annotations.Nullable;

public class Angular2PipeUtil {

  public static final String PIPE_DEC = "Pipe";
  public static final String NAME_PROP = "name";
  public static final String TRANSFORM_METHOD = "transform";

  public static boolean isPipeTransformMethod(PsiElement element) {
    return element instanceof TypeScriptFunction
           && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
           && Angular2EntitiesProvider.getPipe(element) != null;
  }

  public static boolean isPipeType(@Nullable String type) {
    return type != null && type.startsWith("P;") && type.endsWith(";;") && type.length() >= 4;
  }

  public static String createTypeString(@Nullable String aClass) {
    return "P;" + StringUtil.notNullize(aClass) + ";;";
  }

}
