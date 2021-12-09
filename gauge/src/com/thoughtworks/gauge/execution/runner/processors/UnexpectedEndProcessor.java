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
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;

import java.text.ParseException;

public final class UnexpectedEndProcessor extends GaugeEventProcessor {
  public UnexpectedEndProcessor(MessageProcessor processor, TestsCache cache) {
    super(processor, cache);
  }

  @Override
  protected boolean onStart(ExecutionEvent event) {
    return true;
  }

  @Override
  protected boolean onEnd(ExecutionEvent event) throws ParseException {
    String name = "Failed";
    ServiceMessageBuilder msg = ServiceMessageBuilder.testFailed(name);
    if (event.result.skipped()) {
      name = "Ignored";
      msg = ServiceMessageBuilder.testIgnored(name);
    }
    getProcessor().process(ServiceMessageBuilder.testStarted(name), 1, SuiteEventProcessor.SUITE_ID);
    msg.addAttribute("message", " ");
    getProcessor().process(msg, 1, 0);
    getProcessor().process(ServiceMessageBuilder.testFinished(name), 1, SuiteEventProcessor.SUITE_ID);
    return false;
  }

  @Override
  public boolean process(ExecutionEvent event) throws ParseException {
    return onEnd(event);
  }

  @Override
  public boolean canProcess(ExecutionEvent event) {
    return getCache().getCurrentId() == SuiteEventProcessor.SUITE_ID;
  }
}
