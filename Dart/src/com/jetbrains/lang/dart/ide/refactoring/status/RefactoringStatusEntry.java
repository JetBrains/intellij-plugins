/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.refactoring.status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable object representing an entry in the list in {@link RefactoringStatus}. A refactoring
 * status entry consists of a severity, a message and a context object.
 */
public class RefactoringStatusEntry {
  /**
   * The severity level.
   */
  @NotNull private final RefactoringStatusSeverity severity;

  /**
   * The message of the status entry.
   */
  @NotNull private final String message;

  /**
   * The {@link RefactoringStatusContext} which can be used to show more detailed information
   * regarding this status entry in the UI. May be {@code null} indicating that no context is
   * available.
   */
  @Nullable private final RefactoringStatusContext context;

  public RefactoringStatusEntry(@NotNull RefactoringStatusSeverity severity, @NotNull String message) {
    this(severity, message, null);
  }

  public RefactoringStatusEntry(@NotNull RefactoringStatusSeverity severity,
                                @NotNull String message,
                                @Nullable RefactoringStatusContext ctx) {
    this.severity = severity;
    this.message = message;
    this.context = ctx;
  }

  /**
   * @return the {@link RefactoringStatusContext} which can be used to show more detailed
   * information regarding this status entry in the UI. The method may return {@code null}
   * indicating that no context is available.
   */
  @Nullable
  public RefactoringStatusContext getContext() {
    return context;
  }

  /**
   * @return the message of the status entry.
   */
  @NotNull
  public String getMessage() {
    return message;
  }

  /**
   * @return the severity level.
   */
  @NotNull
  public RefactoringStatusSeverity getSeverity() {
    return severity;
  }

  /**
   * Returns whether the entry represents an error or not.
   *
   * @return <code>true</code> if (severity ==<code>RefactoringStatusSeverity.ERROR</code>).
   */
  public boolean isError() {
    return severity == RefactoringStatusSeverity.ERROR;
  }

  /**
   * Returns whether the entry represents a fatal error or not.
   *
   * @return <code>true</code> if (severity ==<code>RefactoringStatusSeverity.FATAL</code>)
   */
  public boolean isFatalError() {
    return severity == RefactoringStatusSeverity.FATAL;
  }

  /**
   * Returns whether the entry represents an information or not.
   *
   * @return <code>true</code> if (severity ==<code>RefactoringStatusSeverity.INFO</code>).
   */
  public boolean isInfo() {
    return severity == RefactoringStatusSeverity.INFO;
  }

  /**
   * Returns whether the entry represents a warning or not.
   *
   * @return <code>true</code> if (severity ==<code>RefactoringStatusSeverity.WARNING</code>).
   */
  public boolean isWarning() {
    return severity == RefactoringStatusSeverity.WARNING;
  }

  @Override
  public String toString() {
    if (context != null) {
      return severity + ": " + message + "; Context: " + context;
    }
    else {
      return severity + ": " + message;
    }
  }
}
