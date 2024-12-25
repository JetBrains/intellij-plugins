// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This extension allows downstream plugins to measure the end-to-end completion time, calling one method just before the Dart Analysis
 * Server is called, and a second just before the UI is displayed to the user. For each dartCompletionStart call, either dartCompletionEnd
 * or dartCompletionError will always be called.
 * <p/>
 * For the DAS completion timing only, a RequestListener and ResponseListener can be attached to the analysis server already.
 */
public abstract class DartCompletionTimerExtension {

  private static final ExtensionPointName<DartCompletionTimerExtension> EP_NAME =
    ExtensionPointName.create("Dart.completionTimerExtension");

  public static @NotNull List<DartCompletionTimerExtension> getExtensions() {
    return EP_NAME.getExtensionList();
  }

  public abstract void dartCompletionStart();

  public abstract void dartCompletionEnd();

  /**
   * The parameters match those of {@link org.dartlang.analysis.server.protocol.RequestError}.
   */
  public abstract void dartCompletionError(@NotNull String code, @NotNull String message, @NotNull String stackTrace);
}
