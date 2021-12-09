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

import com.thoughtworks.gauge.execution.runner.MessageProcessor;
import com.thoughtworks.gauge.execution.runner.TestsCache;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;

import java.text.ParseException;

public final class SuiteEventProcessor extends GaugeEventProcessor {
  private static final String BEFORE_SUITE = "Before Suite";
  private static final String AFTER_SUITE = "After Suite";
  static final int SUITE_ID = 0;

  public SuiteEventProcessor(MessageProcessor processor, TestsCache cache) {
    super(processor, cache);
  }

  @Override
  protected boolean onStart(ExecutionEvent event) {
    return getProcessor().processLineBreak();
  }

  @Override
  protected boolean onEnd(ExecutionEvent event) throws ParseException {
    return super.addHooks(event, BEFORE_SUITE, AFTER_SUITE, "", SUITE_ID);
  }

  @Override
  public boolean canProcess(ExecutionEvent event) {
    return event.type.equalsIgnoreCase(ExecutionEvent.SUITE_START) ||
           event.type.equalsIgnoreCase(ExecutionEvent.SUITE_END);
  }
}
