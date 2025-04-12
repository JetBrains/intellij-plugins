// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface CucumberGlueProvider {
  @RequiresReadLock
  void calculateGlue(@NotNull Consumer<String> consumer);
}
