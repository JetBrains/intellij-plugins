package org.osmorc.settings;

import com.intellij.openapi.util.Pair;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import java.util.List;

public interface FrameworkDefinitionListener {
  Topic<FrameworkDefinitionListener> TOPIC = new Topic<>("OSGi Framework Definition Changes", FrameworkDefinitionListener.class);

  /**
   * Receives pairs of (oldInstance, newInstance).
   * Old instance is null for added frameworks, new instance is null for removed ones.
   */
  void definitionsChanged(@NotNull List<Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition>> changes);
}
