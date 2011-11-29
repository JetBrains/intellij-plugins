package com.jetbrains.actionscript.profiler.base;

import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public interface NavigatableDataProducer {
  @Nullable
  Navigatable getNavigatable();
}
