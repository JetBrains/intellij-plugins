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

package com.thoughtworks.gauge.inspection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.thoughtworks.gauge.GaugeConstants;
import org.jetbrains.annotations.Nullable;

public final class GaugeError {
  private final String fileName;
  private final int lineNumber;
  private final @NlsSafe String message;

  public GaugeError(@NlsSafe String type, String fileName, int lineNumber, @NlsSafe String message) {
    this.fileName = fileName;
    this.lineNumber = lineNumber;
    // here error comes from the Gauge CLI in EN, so we do not extract `line number` to i18n
    this.message = String.format("%s line number: %d, %s", type, lineNumber, message);
  }

  boolean isFrom(String fileName) {
    return this.fileName.equals(fileName);
  }

  public @NlsSafe String getMessage() {
    return message;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getFileName() {
    return fileName;
  }

  int getOffset(String text) {
    return StringUtil.lineColToOffset(text, lineNumber - 1, 0);
  }

  public static @Nullable GaugeError parseCliError(@NlsSafe String error) {
    try {
      String[] parts = error.split(" ");
      String[] fileInfo = parts[1].split(GaugeConstants.SPEC_SCENARIO_DELIMITER);
      return new GaugeError(parts[0], fileInfo[0], Integer.parseInt(fileInfo[1]), error.split(":\\d+:? ")[1]);
    }
    catch (Exception e) {
      Logger.getInstance(GaugeError.class).debug("Unable to parse Gauge CLI error", e);
      return null;
    }
  }
}
