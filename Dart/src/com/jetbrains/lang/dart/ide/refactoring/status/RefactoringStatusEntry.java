// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring.status;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable object representing an entry in the list in {@link RefactoringStatus}. A refactoring
 * status entry consists of a severity, a message and a context object.
 */
public class RefactoringStatusEntry {

  private final @NotNull RefactoringStatusSeverity mySeverity;
  private final @NotNull @NlsSafe String myMessage;

  /**
   * The {@link RefactoringStatusContext} which can be used to show more detailed information
   * regarding this status entry in the UI. May be {@code null} indicating that no context is
   * available.
   */
  private final @Nullable RefactoringStatusContext myContext;

  public RefactoringStatusEntry(@NotNull RefactoringStatusSeverity severity, @NotNull String message) {
    this(severity, message, null);
  }

  public RefactoringStatusEntry(@NotNull RefactoringStatusSeverity severity,
                                @NotNull String message,
                                @Nullable RefactoringStatusContext ctx) {
    this.mySeverity = severity;
    this.myMessage = message;
    this.myContext = ctx;
  }

  /**
   * @return the {@link RefactoringStatusContext} which can be used to show more detailed
   * information regarding this status entry in the UI. The method may return {@code null}
   * indicating that no context is available.
   */
  public @Nullable RefactoringStatusContext getContext() {
    return myContext;
  }

  /**
   * @return the message of the status entry.
   */
  public @NotNull @NlsSafe String getMessage() {
    return myMessage;
  }

  /**
   * @return the severity level.
   */
  public @NotNull RefactoringStatusSeverity getSeverity() {
    return mySeverity;
  }

  /**
   * Returns whether the entry represents an error or not.
   *
   * @return {@code true} if (severity =={@code RefactoringStatusSeverity.ERROR}).
   */
  public boolean isError() {
    return mySeverity == RefactoringStatusSeverity.ERROR;
  }

  /**
   * Returns whether the entry represents a fatal error or not.
   *
   * @return {@code true} if (severity =={@code RefactoringStatusSeverity.FATAL})
   */
  public boolean isFatalError() {
    return mySeverity == RefactoringStatusSeverity.FATAL;
  }

  /**
   * Returns whether the entry represents an information or not.
   *
   * @return {@code true} if (severity =={@code RefactoringStatusSeverity.INFO}).
   */
  public boolean isInfo() {
    return mySeverity == RefactoringStatusSeverity.INFO;
  }

  /**
   * Returns whether the entry represents a warning or not.
   *
   * @return {@code true} if (severity =={@code RefactoringStatusSeverity.WARNING}).
   */
  public boolean isWarning() {
    return mySeverity == RefactoringStatusSeverity.WARNING;
  }

  @Override
  public String toString() {
    if (myContext != null) {
      return mySeverity + ": " + myMessage + "; Context: " + myContext;
    }
    else {
      return mySeverity + ": " + myMessage;
    }
  }
}
