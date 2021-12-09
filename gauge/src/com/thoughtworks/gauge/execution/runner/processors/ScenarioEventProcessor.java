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

package com.thoughtworks.gauge.execution.runner.processors;

import com.intellij.execution.testframework.sm.ServiceMessageBuilder;
import com.thoughtworks.gauge.execution.runner.MessageProcessor;
import com.thoughtworks.gauge.execution.runner.TestsCache;
import com.thoughtworks.gauge.execution.runner.event.ExecutionError;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;
import com.thoughtworks.gauge.execution.runner.event.ExecutionResult;
import com.thoughtworks.gauge.execution.runner.event.TableInfo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ScenarioEventProcessor extends GaugeEventProcessor {
  private static final String TABLE_ROW_SEPARATOR = "_";

  public ScenarioEventProcessor(MessageProcessor processor, TestsCache cache) {
    super(processor, cache);
  }

  @Override
  public boolean onStart(ExecutionEvent event) throws ParseException {
    Integer parentId = getCache().getId(event.parentId);
    return super.addTest(getIdentifier(event, event.name), parentId, getIdentifier(event, event.id), event);
  }

  @Override
  public boolean onEnd(ExecutionEvent event) throws ParseException {
    Integer parentId = getCache().getId(event.parentId);
    Integer id = getCache().getId(getIdentifier(event, event.id));
    String name = getIdentifier(event, event.name);
    if (event.result.failed()) {
      scenarioMessage(ServiceMessageBuilder.testFailed(name), id, parentId, event.result, "Failed: ");
    }
    else if (event.result.skipped()) {
      scenarioMessage(ServiceMessageBuilder.testIgnored(name), id, parentId, event.result, "Skipped: ");
    }
    ServiceMessageBuilder scenarioEnd = ServiceMessageBuilder.testFinished(name);
    scenarioEnd.addAttribute("duration", String.valueOf(event.result.time));
    return getProcessor().process(scenarioEnd, id, parentId);
  }

  @Override
  public boolean canProcess(ExecutionEvent event) {
    return event.type.equalsIgnoreCase(ExecutionEvent.SCENARIO_START) ||
           event.type.equalsIgnoreCase(ExecutionEvent.SCENARIO_END);
  }

  private void scenarioMessage(ServiceMessageBuilder msg, Integer nodeId, Integer parentId, ExecutionResult result, String status)
    throws ParseException {
    List<ExecutionError> errors = new ArrayList<>();
    String tableText = "";
    if (result.table != null) {
      tableText = (result.table.text.startsWith("\n") ? result.table.text.substring(1) : result.table.text) + "\n";
    }
    if (result.beforeHookFailure != null) errors.add(result.beforeHookFailure);
    if (result.errors != null) errors.addAll(Arrays.asList(result.errors));
    if (result.afterHookFailure != null) errors.add(result.afterHookFailure);
    msg.addAttribute("message", tableText + errors.stream()
      .map(e -> e.format(status))
      .collect(Collectors.joining("\n\n")));
    getProcessor().process(msg, nodeId, parentId);
  }

  private static String getIdentifier(ExecutionEvent event, String value) {
    TableInfo table = event.result.table;
    return table != null ? value + TABLE_ROW_SEPARATOR + (table.rowIndex + 1) : value;
  }
}
