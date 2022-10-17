package com.intellij.plugins.drools;

import org.jetbrains.annotations.NonNls;

public interface DroolsConstants {

  @NonNls String KNOWLEDGE_HELPER_CLASS = "org.drools.core.spi.KnowledgeHelper";
  @NonNls String KIE_CONTEXT_CLASS = "org.kie.api.runtime.rule.RuleContext";

  @NonNls String EXIT_POINT_CLASS = "org.drools.runtime.ExitPoint";

  @NonNls String TRAITS_THING = "org.drools.core.factmodel.traits.Thing";
}
