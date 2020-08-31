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
import com.thoughtworks.gauge.Constants;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.thoughtworks.gauge.util.GaugeUtil.getGaugeSettings;

final class GaugeInspectionHelper {
  private static final Logger LOG = Logger.getInstance(GaugeInspectionHelper.class);

  @NotNull
  static List<GaugeError> getErrors(File directory) {
    try {
      GaugeSettingsModel settings = getGaugeSettings();
      ProcessBuilder processBuilder = new ProcessBuilder(settings.getGaugePath(), Constants.VALIDATE);
      GaugeUtil.setGaugeEnvironmentsTo(processBuilder, settings);
      processBuilder.directory(directory);
      Process process = processBuilder.start();
      process.waitFor();

      String[] errors = GaugeUtil.getOutput(process.getInputStream(), "\n").split("\n");
      return Arrays.stream(errors).map(GaugeError::parseCliError).filter(Objects::nonNull).collect(Collectors.toList());
    }
    catch (IOException | InterruptedException | GaugeNotFoundException e) {
      LOG.debug(e);
    }
    return new ArrayList<>();
  }
}
