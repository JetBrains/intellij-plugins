// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.psi.DartReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DartNameSuggesterUtil {
  private DartNameSuggesterUtil() {
  }

  private static @NotNull String deleteNonLetterFromString(final @NotNull String string) {
    Pattern pattern = Pattern.compile("[^a-zA-Z_]+");
    Matcher matcher = pattern.matcher(string);
    return matcher.replaceAll("_");
  }

  public static Collection<String> getSuggestedNames(final DartExpression expression) {
    return getSuggestedNames(expression, null);
  }

  public static Collection<String> getSuggestedNames(final DartExpression expression, @Nullable Collection<String> additionalUsedNames) {
    Collection<String> candidates = new LinkedHashSet<>();

    String text = expression.getText();
    if (expression instanceof DartReference) {
      DartClass dartClass = ((DartReference)expression).resolveDartClass().getDartClass();
      String dartClassName = dartClass == null ? null : dartClass.getName();
      if (dartClassName != null && !dartClassName.equals(StringUtil.decapitalize(dartClassName))) {
        candidates.add(StringUtil.decapitalize(dartClassName));
      }
    }

    if (expression instanceof DartCallExpression) {
      final DartExpression callee = ((DartCallExpression)expression).getExpression();
      if (callee != null) {
        text = callee.getText();
      }
    }

    if (text != null) {
      candidates.addAll(generateNames(text));
    }

    final Set<String> usedNames = DartRefactoringUtil.collectUsedNames(expression);
    if (additionalUsedNames != null && !additionalUsedNames.isEmpty()) {
      usedNames.addAll(additionalUsedNames);
    }
    final Collection<String> result = new ArrayList<>();

    for (String candidate : candidates) {
      int index = 0;
      String suffix = "";
      while (usedNames.contains(candidate + suffix)) {
        suffix = Integer.toString(++index);
      }
      result.add(candidate + suffix);
    }

    if (result.isEmpty()) {
      result.add("o"); // never empty
    }

    return result;
  }

  public static @NotNull Collection<String> generateNames(@NotNull String name) {
    name = StringUtil.decapitalize(deleteNonLetterFromString(StringUtil.unquoteString(name.replace('.', '_'))));
    if (name.startsWith("get")) {
      name = name.substring(3);
    }
    else {
      name = StringUtil.trimStart(name, "is");
    }
    while (name.startsWith("_")) {
      name = name.substring(1);
    }
    while (name.endsWith("_")) {
      name = name.substring(0, name.length() - 1);
    }
    final int length = name.length();
    final Collection<String> possibleNames = new LinkedHashSet<>();
    for (int i = 0; i < length; i++) {
      if (Character.isLetter(name.charAt(i)) &&
          (i == 0 || name.charAt(i - 1) == '_' || (Character.isLowerCase(name.charAt(i - 1)) && Character.isUpperCase(name.charAt(i))))) {
        final String candidate = StringUtil.decapitalize(name.substring(i));
        if (candidate.length() < 25) {
          possibleNames.add(candidate);
        }
      }
    }
    // prefer shorter names
    ArrayList<String> reversed = new ArrayList<>(possibleNames);
    Collections.reverse(reversed);
    return ContainerUtil.map(reversed, name1 -> {
      if (name1.indexOf('_') == -1) {
        return name1;
      }
      name1 = StringUtil.capitalizeWords(name1, "_", true, true);
      return StringUtil.decapitalize(name1.replaceAll("_", ""));
    });
  }
}
