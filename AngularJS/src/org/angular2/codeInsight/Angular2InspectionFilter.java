// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.highlighting.IntentionAndInspectionFilter;
import com.intellij.lang.javascript.intentions.ES6CoolRefactoring;
import org.jetbrains.annotations.NotNull;

public class Angular2InspectionFilter extends IntentionAndInspectionFilter {

  @Override
  public boolean isSupportedIntention(@NotNull Class clazz) {
    return !ES6CoolRefactoring.class.isAssignableFrom(clazz);
  }
}
