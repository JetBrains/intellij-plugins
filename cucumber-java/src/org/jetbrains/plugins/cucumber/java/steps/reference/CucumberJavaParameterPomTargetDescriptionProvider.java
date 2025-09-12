// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.pom.PomDescriptionProvider;
import com.intellij.pom.PomTarget;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.usageView.UsageViewNodeTextLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;

@NotNullByDefault
public final class CucumberJavaParameterPomTargetDescriptionProvider extends PomDescriptionProvider {
  @Override
  public @Nullable String getElementDescription(PomTarget element, ElementDescriptionLocation location) {
    if (element instanceof CucumberJavaParameterPomTarget cucumberParameterPomTarget) {
      if (location == UsageViewTypeLocation.INSTANCE) {
        return CucumberJavaBundle.message("cucumber.java.parameter.type");
      }
      if (location == UsageViewNodeTextLocation.INSTANCE) {
        return cucumberParameterPomTarget.getName();
      }
    }

    return null;
  }
}
