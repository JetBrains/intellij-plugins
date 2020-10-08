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

package com.thoughtworks.gauge;

public final class GaugeConstants {
  private GaugeConstants() {
  }

  public static final String PLUGIN_ID = "com.thoughtworks.gauge";

  public static final String GAUGE = "gauge";
  public static final String SPEC_EXTENSION = "spec";
  public static final String CONCEPT_EXTENSION = "cpt";
  public static final String MANIFEST_FILE = "manifest.json";
  public static final String RUN = "run";
  public static final String INSTALL = "install";
  public static final String DOCS = "docs";
  public static final String VERSION = "--version";
  public static final String MACHINE_READABLE = "--machine-readable";
  public static final String SIMPLE_CONSOLE = "--simple-console";
  public static final String TAGS = "--tags";
  public static final String PARALLEL = "--parallel";
  public static final String PARALLEL_NODES = "-n";
  public static final String TABLE_ROWS = "--table-rows";
  public static final String DAEMON = "daemon";
  public static final String VALIDATE = "validate";
  public static final String FORMAT = "format";
  public static final String ENV_FLAG = "--env";
  public static final String INIT_FLAG = "init";
  public static final String FAILED = "--failed";
  public static final String GAUGE_HOME = "GAUGE_HOME";
  public static final String GAUGE_CUSTOM_CLASSPATH = "gauge_custom_classpath";
  public static final String SPEC_FILE_DELIMITER = "||";
  public static final String SPEC_FILE_DELIMITER_REGEX = "\\|\\|";
  private static final String COLON = ":";
  public static final String SPEC_SCENARIO_DELIMITER = COLON;
  public static final String CLASSPATH_DELIMITER = COLON;
  public static final String HIDE_SUGGESTION = "--hide-suggestion";
  public static final String MIN_GAUGE_VERSION = "0.9.0";
}
