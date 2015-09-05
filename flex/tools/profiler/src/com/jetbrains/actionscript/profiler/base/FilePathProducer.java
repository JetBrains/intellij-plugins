package com.jetbrains.actionscript.profiler.base;

import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public interface FilePathProducer {
  @Nullable
  String getFilePath();
}
