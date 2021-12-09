/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.execution.runner.event;

public final class ExecutionError {
  public String text;
  public String filename;
  public String lineNo;
  public String message;
  public String stackTrace;

  public String format(String status) {
    return format(this.text, status, "\n") +
           format(getFileNameWithLineNo(), "Filename: ", "\n") +
           format(this.message, "Message: ", "\n") +
           format(this.stackTrace, "Stack Trace:\n", "");
  }

  private String getFileNameWithLineNo() {
    return lineNo.isEmpty() ? filename : format(":", filename, lineNo);
  }

  private static String format(String text, String prefix, String suffix) {
    return text != null && !text.isEmpty() ? prefix + text + suffix : "";
  }
}
