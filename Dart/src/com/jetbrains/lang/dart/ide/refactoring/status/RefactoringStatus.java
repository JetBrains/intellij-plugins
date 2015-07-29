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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Outcome of a condition checking operation.
 */
public class RefactoringStatus {
  private final List<RefactoringStatusEntry> entries = Lists.newArrayList();
  private RefactoringStatusSeverity severity = RefactoringStatusSeverity.OK;

  /**
   * Adds given {@link RefactoringStatusEntry} and updates {@link #severity}.
   */
  public void addEntry(RefactoringStatusEntry entry) {
    entries.add(entry);
    severity = max(severity, entry.getSeverity());
  }

  /**
   * Adds a <code>ERROR</code> entry filled with the given message to this status.
   */
  public void addError(String msg) {
    addError(msg, null);
  }

  /**
   * Adds a <code>ERROR</code> entry filled with the given message and status to this status.
   */
  public void addError(String msg, RefactoringStatusContext context) {
    addEntry(new RefactoringStatusEntry(RefactoringStatusSeverity.ERROR, msg, context));
  }

  /**
   * Adds a <code>FATAL</code> entry filled with the given message to this status.
   */
  public void addFatalError(String msg) {
    addFatalError(msg, null);
  }

  /**
   * Adds a <code>FATAL</code> entry filled with the given message and status to this status.
   */
  public void addFatalError(String msg, RefactoringStatusContext context) {
    addEntry(new RefactoringStatusEntry(RefactoringStatusSeverity.FATAL, msg, context));
  }

  /**
   * Adds a <code>WARNING</code> entry filled with the given message to this status.
   */
  public void addWarning(String msg) {
    addWarning(msg, null);
  }

  /**
   * Adds a <code>WARNING</code> entry filled with the given message and status to this status.
   */
  public void addWarning(String msg, RefactoringStatusContext context) {
    addEntry(new RefactoringStatusEntry(RefactoringStatusSeverity.WARNING, msg, context));
  }

  /**
   * @return the copy of this {@link RefactoringStatus} with {@link RefactoringStatusSeverity#ERROR}
   * replaced with {@link RefactoringStatusSeverity#FATAL}.
   */
  public RefactoringStatus escalateErrorToFatal() {
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
  public List<RefactoringStatusEntry> getEntries() {
    return entries;
  }

  /**
   * @return the RefactoringStatusEntry with the highest severity, or <code>null</code> if no
   * entries are present.
   */
  public RefactoringStatusEntry getEntryWithHighestSeverity() {
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
   * @return the message from the {@link RefactoringStatusEntry} with highest severity; may be
   * <code>null</code> if not entries are present.
   */
  public String getMessage() {
    RefactoringStatusEntry entry = getEntryWithHighestSeverity();
    if (entry == null) {
      return null;
    }
    return entry.getMessage();
  }

  /**
   * @return the current severity of the {@link RefactoringStatus}.
   */
  public RefactoringStatusSeverity getSeverity() {
    return severity;
  }

  /**
   * @return <code>true</code> if the current severity is <code>
   * FATAL</code> or <code>ERROR</code>.
   */
  public boolean hasError() {
    return severity == RefactoringStatusSeverity.FATAL || severity == RefactoringStatusSeverity.ERROR;
  }

  /**
   * @return <code>true</code> if the current severity is <code>FATAL</code>.
   */
  public boolean hasFatalError() {
    return severity == RefactoringStatusSeverity.FATAL;
  }

  /**
   * @return <code>true</code> if the current severity is <code>
   * FATAL</code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>.
   */
  public boolean hasInfo() {
    return severity == RefactoringStatusSeverity.FATAL ||
           severity == RefactoringStatusSeverity.ERROR ||
           severity == RefactoringStatusSeverity.WARNING ||
           severity == RefactoringStatusSeverity.INFO;
  }

  /**
   * @return <code>true</code> if the current severity is <code>
   * FATAL</code>, <code>ERROR</code> or <code>WARNING</code>.
   */
  public boolean hasWarning() {
    return severity == RefactoringStatusSeverity.FATAL ||
           severity == RefactoringStatusSeverity.ERROR ||
           severity == RefactoringStatusSeverity.WARNING;
  }

  /**
   * @return <code>true</code> if the severity is <code>OK</code>.
   */
  public boolean isOK() {
    return severity == RefactoringStatusSeverity.OK;
  }

  /**
   * Merges the receiver and the parameter statuses. The resulting list of entries in the receiver
   * will contain entries from both. The resulting severity in the receiver will be the more severe
   * of its current severity and the parameter's severity. Merging with <code>null</code> is allowed
   * - it has no effect.
   */
  public void merge(RefactoringStatus other) {
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
  public static RefactoringStatus createErrorStatus(String msg) {
    RefactoringStatus status = new RefactoringStatus();
    status.addError(msg);
    return status;
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#FATAL}.
   */
  public static RefactoringStatus createFatalErrorStatus(String msg) {
    RefactoringStatus status = new RefactoringStatus();
    status.addFatalError(msg);
    return status;
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#FATAL}.
   */
  public static RefactoringStatus createFatalErrorStatus(String msg, RefactoringStatusContext context) {
    RefactoringStatus status = new RefactoringStatus();
    status.addFatalError(msg, context);
    return status;
  }

  /**
   * @return the new {@link RefactoringStatus} with {@link RefactoringStatusSeverity#WARNING}.
   */
  public static RefactoringStatus createWarningStatus(String msg) {
    RefactoringStatus status = new RefactoringStatus();
    status.addWarning(msg);
    return status;
  }

  /**
   * @return the {@link Enum} value with maximal ordinal.
   */
  private static <T extends Enum<T>> T max(T a, T b) {
    if (b.ordinal() > a.ordinal()) {
      return b;
    }
    return a;
  }
}
