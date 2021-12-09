// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.util.containers.ContainerUtil;

import java.util.Set;

public final class CucumberJavaAnnotationProvider {
  public static final Set<String> STEP_MARKERS = ContainerUtil.set("Given", "Then", "And", "But", "When");
  public static final Set<String> HOOK_MARKERS = ContainerUtil.set("Before", "After");
}
