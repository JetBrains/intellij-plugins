// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.settings;

import com.intellij.openapi.util.Pair;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import java.util.List;

public interface FrameworkDefinitionListener {
  Topic<FrameworkDefinitionListener> TOPIC = new Topic<>("OSGi Framework Definition Changes", FrameworkDefinitionListener.class);

  /**
   * Receives a list of {@code [old_instance, new_instance]} pairs.
   * {@code old_instance} is {@code null} for added frameworks, {@code new_instance} is {@code null} for removed ones.
   */
  void definitionsChanged(@NotNull List<Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition>> changes);
}