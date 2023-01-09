// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools;

import org.jetbrains.annotations.NonNls;

public interface DroolsConstants {
  @NonNls String KNOWLEDGE_HELPER_CLASS = "org.drools.core.spi.KnowledgeHelper";
  @NonNls String KNOWLEDGE_HELPER_8_X = "org.drools.core.rule.consequence.KnowledgeHelper";
  @NonNls String KIE_CONTEXT_CLASS = "org.kie.api.runtime.rule.RuleContext";

  @NonNls String TRAITS_THING = "org.drools.core.factmodel.traits.Thing";
}
