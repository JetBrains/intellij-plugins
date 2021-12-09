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
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.execution.runner.MessageProcessor;
import com.thoughtworks.gauge.execution.runner.TestsCache;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;

import java.text.ParseException;

public final class SpecEventProcessor extends GaugeEventProcessor {
  private static final String BEFORE_SPEC = "Before Specification";
  private static final String AFTER_SPEC = "After Specification";

  public SpecEventProcessor(MessageProcessor processor, TestsCache cache) {
    super(processor, cache);
  }

  @Override
  protected boolean onStart(ExecutionEvent event) throws ParseException {
    if (getCache().getCurrentId() == SuiteEventProcessor.SUITE_ID) getProcessor().processLineBreak();
    getCache().setId(event.id);
    if (getCache().getId(event.id.split(GaugeConstants.SPEC_SCENARIO_DELIMITER)[0]) == null) {
      getCache().setId(event.id.split(GaugeConstants.SPEC_SCENARIO_DELIMITER)[0], getCache().getId(event.id));
    }
    ServiceMessageBuilder msg = ServiceMessageBuilder.testSuiteStarted(event.name);
    super.addLocation(event, msg);
    return getProcessor().process(msg, getCache().getId(event.id), SuiteEventProcessor.SUITE_ID);
  }

  @Override
  protected boolean onEnd(ExecutionEvent event) throws ParseException {
    super.addHooks(event, BEFORE_SPEC, AFTER_SPEC, event.id, getCache().getId(event.id));
    ServiceMessageBuilder msg = ServiceMessageBuilder.testSuiteFinished(event.name);
    msg.addAttribute("duration", String.valueOf(event.result.time));
    return getProcessor().process(msg, getCache().getId(event.id), SuiteEventProcessor.SUITE_ID);
  }

  @Override
  public boolean canProcess(ExecutionEvent event) {
    return event.type.equalsIgnoreCase(ExecutionEvent.SPEC_START) ||
           event.type.equalsIgnoreCase(ExecutionEvent.SPEC_END);
  }
}
