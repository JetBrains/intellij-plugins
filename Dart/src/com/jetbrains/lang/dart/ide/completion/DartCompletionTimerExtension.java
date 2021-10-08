package com.jetbrains.lang.dart.ide.completion;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This extension allows downstream plugins to measure the end-to-end completion time, calling one method just before the Dart Analysis
 * Server is called, and a second just before the UI is displayed to the user.
 * <p/>
 * For the DAS completion timing only, a RequestListener and ResponseListener can be attached to the analysis server already.
 */
public abstract class DartCompletionTimerExtension {

  private static final ExtensionPointName<DartCompletionTimerExtension> EP_NAME =
    ExtensionPointName.create("Dart.completionTimerExtension");

  @NotNull
  public static List<DartCompletionTimerExtension> getExtensions() {
    return EP_NAME.getExtensionList();
  }

  public abstract void dartCompletionStart();

  public abstract void dartCompletionEnd();
}
