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

public class ExecutionEvent {
  public String type;
  public String id;
  public String filename;
  public Integer line;
  public String parentId;
  public String name;
  public String message;
  public GaugeNotification notification;
  public ExecutionResult result;
  public static final String SUITE_START = "suiteStart";
  public static final String SPEC_START = "specStart";
  public static final String SPEC_END = "specEnd";
  public static final String SCENARIO_START = "scenarioStart";
  public static final String SCENARIO_END = "scenarioEnd";
  public static final String SUITE_END = "suiteEnd";
  public static final String NOTIFICATION = "notification";
  public static final String STANDARD_OUTPUT = "out";
  public static final String FAIL = "fail";
  public static final String SKIP = "skip";
}
