// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring.status;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of a condition checking operation.
 */
public class RefactoringStatus {
  private final @NotNull List<RefactoringStatusEntry> entries = new ArrayList<>();
  private @NotNull RefactoringStatusSeverity severity = RefactoringStatusSeverity.OK;

  /**
   * Adds given {@link RefactoringStatusEntry} and updates {@link #severity}.
   */
  public void addEntry(@NotNull RefactoringStatusEntry entry) {
    entries.add(entry);
    severity = max(severity, entry.getSeverity());
  }

  /**
   * Adds a {@code ERROR} entry filled with the given message to this status.
   */
  public void addError(@NotNull String msg) {
    addError(msg, null);
  }

  /**
   * Adds a {@code ERROR} entry filled with the given message and status to this status.
   */
  public void addError(@NotNull String msg, @Nullable RefactoringStatusContext context) {
    addEntry(new RefactoringStatusEntry(RefactoringStatusSeverity.ERROR, msg, context));
  }

  /**
   * Adds a {@code FATAL} entry filled with the given message to this status.
   */
  public void addFatalError(@NotNull String msg) {
    addFatalError(msg, null);
  }

  /**
   * Adds a {@code FATAL} entry filled with the given message and status to this status.
   */
  public void addFatalError(@NotNull String msg, @Nullable RefactoringStatusContext context) {
    addEntry(new RefactoringStatusEntry(RefactoringStatusSeverity.FATAL, msg, context));
  }

  /**
   * Adds a {@code WARNING} entry filled with the given message to this status.
   */
  public void addWarning(String msg) {
    addWarning(msg, null);
  }

  /**
   * Adds a {@code WARNING} entry filled with the given message and status to this status.
   */
  public void addWarning(@NotNull String msg, @Nullable RefactoringStatusContext context) {
    addEntry(new RefactoringStatusEntry(RefactoringStatusSeverity.WARNING, msg, context));
  }

  /**
   * @return the copy of this {@link RefactoringStatus} with {@link RefactoringStatusSeverity#ERROR}
   * replaced with {@link RefactoringStatusSeverity#FATAL}.
   */
  public @NotNull RefactoringStatus escalateErrorToFatal() {
    RefactoringStatus result = new RefactoringStatus();
    for (RefactoringStatusEntry entry : entries) {
      RefactoringStatusSeverity severity = entry.getSeverity();
      if (severity == RefactoringStatusSeverity.ERROR) {
        severity = RefactoringStatusSeverity.FATAL;
      }
      result.addEntry(new RefactoringStatusEntry(severity, entry.getMessage(), entry.getContext()));
    }
    return result;
  }

  /**
   * @return the {@link RefactoringStatusEntry}s.
   */
  public @NotNull List<RefactoringStatusEntry> getEntries() {
    return entries;
  }

  /**
   * @return the RefactoringStatusEntry with the highest severity, or {@code null} if no
   * entries are present.
   */
  public @Nullable RefactoringStatusEntry getEntryWithHighestSeverity() {
    if (entries.isEmpty()) {
      return null;
    }
    RefactoringStatusEntry result = entries.get(0);
    for (RefactoringStatusEntry entry : entries) {
      if (result.getSeverity().ordinal() < entry.getSeverity().ordinal()) {
        result = entry;
      }
    }
    return result;
  }

  /**
   * Return the message from the {@link RefactoringStatusEntry} with the highest severity; may be
   * {@code null} if no entries are present.
   */
  public @Nullable @NlsSafe String getMessage() {
    RefactoringStatusEntry entry = getEntryWithHighestSeverity();
    if (entry == null) {
      return null;
    }
    return entry.getMessage();
  }

  /**
   * @return the current severity of the {@link RefactoringStatus}.
   */
  public @NotNull RefactoringStatusSeverity getSeverity() {
    return severity;
  }

  /**
   * @return {@code true} if the current severity is {@code
   * FATAL} or {@code ERROR}.
   */
  public boolean hasError() {
    return severity == RefactoringStatusSeverity.FATAL || severity == RefactoringStatusSeverity.ERROR;
  }

  /**
   * @return {@code true} if the current severity is {@code FATAL}.
   */
  public boolean hasFatalError() {
    return severity == RefactoringStatusSeverity.FATAL;
  }

  /**
   * @return {@code true} if the current severity is {@code
   * FATAL}, {@code ERROR}, {@code WARNING} or {@code INFO}.
   */
  public boolean hasInfo() {
    return severity == RefactoringStatusSeverity.FATAL ||
           severity == RefactoringStatusSeverity.ERROR ||
           severity == RefactoringStatusSeverity.WARNING ||
           severity == RefactoringStatusSeverity.INFO;
  }

  /**
   * @return {@code true} if the current severity is {@code
   * FATAL}, {@code ERROR} or {@code WARNING}.
   */
  public boolean hasWarning() {
    return severity == RefactoringStatusSeverity.FATAL ||
           severity == RefactoringStatusSeverity.ERROR ||
           severity == RefactoringStatusSeverity.WARNING;
  }

  /**
   * @return {@code true} if the severity is {@code OK}.
   */
  public boolean isOK() {
    return severity == RefactoringStatusSeverity.OK;
  }

  /**
   * Merges the receiver and the parameter statuses. The resulting list of entries in the receiver
   * will contain entries from both. The resulting severity in the receiver will be the more severe
   * of its current severity and the parameter's severity. Merging with {@code null} is allowed
   * - it has no effect.
   */
  public void merge(@Nullable RefactoringStatus other) {
    if (other == null) {
      return;
    }
    entries.addAll(other.entries);
    severity = max(severity, other.getSeverity());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(severity.name());
    if (!isOK()) {
      sb.append("\n");
      for (RefactoringStatusEntry entry : entries) {
        sb.append("\t").append(entry).append("\n");
      }
    }
    sb.append(">");
    return sb.toString();
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#ERROR}.
   */
  public static @NotNull RefactoringStatus createErrorStatus(String msg) {
    RefactoringStatus status = new RefactoringStatus();
    status.addError(msg);
    return status;
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#FATAL}.
   */
  public static @NotNull RefactoringStatus createFatalErrorStatus(String msg) {
    RefactoringStatus status = new RefactoringStatus();
    status.addFatalError(msg);
    return status;
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#FATAL}.
   */
  public static @NotNull RefactoringStatus createFatalErrorStatus(String msg, RefactoringStatusContext context) {
    RefactoringStatus status = new RefactoringStatus();
    status.addFatalError(msg, context);
    return status;
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#WARNING}.
   */
  public static @NotNull RefactoringStatus createWarningStatus(String msg) {
    RefactoringStatus status = new RefactoringStatus();
    status.addWarning(msg);
    return status;
  }

  /**
   * @return the {@link Enum} value with maximal ordinal.
   */
  private static @NotNull <T extends Enum<T>> T max(@NotNull T a, @NotNull T b) {
    if (b.ordinal() > a.ordinal()) {
      return b;
    }
    return a;
  }
}
