// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CompilerMessagesBuffer {

  private static final class CompilerMessage {
    private final CompilerMessageCategory category;
    private final String message;
    private final String url;
    private final int line;
    private final int column;

    private CompilerMessage(final CompilerMessageCategory category,
                            final String message,
                            final String url,
                            final int line,
                            final int column) {
      this.category = category;
      this.message = message;
      this.url = url;
      this.line = line;
      this.column = column;
    }
  }

  private final List<CompilerMessage> messages;
  private final CompileContext compileContext;
  private final boolean bufferingEnabled;

  private static final String OUT_OF_MEMORY = "java.lang.OutOfMemoryError";

  /**
   * @param bufferingEnabled is {@code false} then this buffer doen't buffer anything but just passes all messages to CompilerContext
   */
  public CompilerMessagesBuffer(final @Nullable CompileContext compileContext, final boolean bufferingEnabled) {
    this.compileContext = compileContext;
    this.bufferingEnabled = bufferingEnabled;
    messages = bufferingEnabled ? Collections.synchronizedList(new ArrayList<>()) : null;
  }

  public void addMessage(final CompilerMessageCategory category, final String message, final String url, final int line, final int column) {
    if (bufferingEnabled) {
      messages.add(new CompilerMessage(category, message, url, line, column));
    }
    else if (compileContext != null) {
      compileContext.addMessage(category, message, url, line, column);
    }
  }

  public void flush() {
    if (bufferingEnabled && compileContext != null) {
      synchronized (messages) {
        for (final CompilerMessage message : messages) {
          compileContext.addMessage(message.category, message.message, message.url, message.line, message.column);
        }
      }
    }
  }

  /**
   * This method may be called only if buffering is enabled.
   */
  public boolean containsErrors() {
    if (!bufferingEnabled) {
      throw new IllegalStateException();
    }
    synchronized (messages) {
      for (final CompilerMessage message : messages) {
        if (message.category == CompilerMessageCategory.ERROR) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This method may be called only if buffering is enabled.
   */
  public boolean containsOutOfMemoryError() {
    if (!bufferingEnabled) {
      throw new IllegalStateException();
    }
    synchronized (messages) {
      for (final CompilerMessage message : messages) {
        if (message.message.contains(OUT_OF_MEMORY)) {
          return true;
        }
      }
    }
    return false;
  }

  public void removeErrorsAndStackTrace() {
    if (bufferingEnabled) {
      synchronized (messages) {
        final Iterator<CompilerMessage> iterator = messages.iterator();
        while (iterator.hasNext()) {
          CompilerMessage message = iterator.next();
          if (message.category == CompilerMessageCategory.ERROR) {
            iterator.remove();
          }
          else if (isStackTrace(message.message)) {
            iterator.remove();
          }
        }
      }
    }
  }

  private static boolean isStackTrace(final String message) {
    return message.startsWith("\tat ") || message.startsWith(OUT_OF_MEMORY);
  }

}
