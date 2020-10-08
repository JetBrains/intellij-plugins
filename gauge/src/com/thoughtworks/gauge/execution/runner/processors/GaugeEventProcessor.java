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
import com.thoughtworks.gauge.execution.runner.event.ExecutionError;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;

import java.text.ParseException;

abstract class GaugeEventProcessor implements EventProcessor {
  private static final String FILE_PREFIX = "gauge://";
  private final MessageProcessor processor;
  private final TestsCache cache;

  GaugeEventProcessor(MessageProcessor processor, TestsCache cache) {
    this.processor = processor;
    this.cache = cache;
  }

  protected abstract boolean onStart(ExecutionEvent event) throws ParseException;

  protected abstract boolean onEnd(ExecutionEvent event) throws ParseException;

  @Override
  public boolean process(ExecutionEvent event) throws ParseException {
    return event.type.endsWith("Start") ? onStart(event) : onEnd(event);
  }

  MessageProcessor getProcessor() {
    return processor;
  }

  TestsCache getCache() {
    return cache;
  }

  boolean addHooks(ExecutionEvent event, String before, String after, String prefix, Integer parentId) throws ParseException {
    failTest(parentId, before, event.result.beforeHookFailure, prefix + before, event);
    failTest(parentId, after, event.result.afterHookFailure, prefix + after, event);
    return true;
  }

  boolean addTest(String name, Integer parentId, String key, ExecutionEvent event) throws ParseException {
    ServiceMessageBuilder test = ServiceMessageBuilder.testStarted(name);
    addLocation(event, test);
    getCache().setId(key);
    return getProcessor().process(test, getCache().getId(key), parentId);
  }

  void addLocation(ExecutionEvent event, ServiceMessageBuilder msg) {
    if (event.filename != null && event.line != null) {
      msg.addAttribute("locationHint", FILE_PREFIX + event.filename + GaugeConstants.SPEC_SCENARIO_DELIMITER + event.line.toString());
    }
  }

  private void failTest(Integer parentId, String name, ExecutionError failure, String key, ExecutionEvent event) throws ParseException {
    if (failure != null) {
      addTest(name, parentId, key, event);
      ServiceMessageBuilder failed = ServiceMessageBuilder.testFailed(name);
      failed.addAttribute("message", failure.format("Failed: "));
      getProcessor().process(failed, getCache().getId(key), parentId);
      getProcessor().process(ServiceMessageBuilder.testFinished(name), getCache().getId(key), parentId);
    }
  }
}
