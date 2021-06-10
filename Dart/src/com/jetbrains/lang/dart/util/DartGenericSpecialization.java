// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartTypeArguments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class DartGenericSpecialization implements Cloneable {
  public static final DartGenericSpecialization EMPTY = new DartGenericSpecialization();

  final Map<String, DartClassResolveResult> map;

  public DartGenericSpecialization() {
    this(new HashMap<>());
  }

  private DartGenericSpecialization(Map<String, DartClassResolveResult> map) {
    this.map = map;
  }

  @Override
  public DartGenericSpecialization clone() {
    final Map<String, DartClassResolveResult> clonedMap = new HashMap<>();
    for (String key : map.keySet()) {
      clonedMap.put(key, map.get(key));
    }
    return new DartGenericSpecialization(clonedMap);
  }

  public void put(PsiElement element, String genericName, DartClassResolveResult resolveResult) {
    map.put(getGenericKey(element, genericName), resolveResult);
  }

  public boolean containsKey(@Nullable PsiElement element, String genericName) {
    return map.containsKey(getGenericKey(element, genericName));
  }

  public DartClassResolveResult get(@Nullable PsiElement element, String genericName) {
    return map.get(getGenericKey(element, genericName));
  }

  public DartGenericSpecialization getInnerSpecialization(PsiElement element) {
    final String prefixToRemove = getGenericKey(element, "");
    final Map<String, DartClassResolveResult> result = new HashMap<>();
    for (String key : map.keySet()) {
      final DartClassResolveResult value = map.get(key);
      String newKey = key;
      newKey = StringUtil.trimStart(newKey, prefixToRemove);
      result.put(newKey, value);
    }
    return new DartGenericSpecialization(result);
  }

  public static String getGenericKey(@Nullable PsiElement element, @NotNull String genericName) {
    final StringBuilder result = new StringBuilder();
    final DartComponent dartComponent = PsiTreeUtil.getParentOfType(element, DartComponent.class, false);
    if (dartComponent instanceof DartClass) {
      result.append(dartComponent.getName());
    }
    else if (dartComponent != null) {
      DartClass dartClass = PsiTreeUtil.getParentOfType(dartComponent, DartClass.class);
      if (dartClass != null) {
        result.append(dartClass.getName());
      }
      if (PsiTreeUtil.getChildOfType(dartComponent, DartTypeArguments.class) != null) {
        // generic method
        result.append(":");
        result.append(dartComponent.getName());
      }
    }
    if (result.length() > 0) {
      result.append("-");
    }
    result.append(genericName);
    return result.toString();
  }
}
