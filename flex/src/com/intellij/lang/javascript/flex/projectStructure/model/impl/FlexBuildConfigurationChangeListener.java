package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.openapi.util.Pair;
import com.intellij.util.messages.Topic;

import java.util.Map;

public interface FlexBuildConfigurationChangeListener {
  Topic<FlexBuildConfigurationChangeListener> TOPIC =
    new Topic<>("Flash build configuration changed",
                FlexBuildConfigurationChangeListener.class,
                Topic.BroadcastDirection.NONE);

  void buildConfigurationsRenamed(Map<Pair<String, String>, String> renames);
}
