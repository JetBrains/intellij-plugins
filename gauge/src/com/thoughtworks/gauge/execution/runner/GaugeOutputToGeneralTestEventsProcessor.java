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

package com.thoughtworks.gauge.execution.runner;

import com.google.gson.GsonBuilder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.ServiceMessageBuilder;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.openapi.util.Key;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;
import com.thoughtworks.gauge.execution.runner.event.ExecutionResult;
import com.thoughtworks.gauge.execution.runner.processors.*;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public final class GaugeOutputToGeneralTestEventsProcessor extends OutputToGeneralTestEventsConverter implements MessageProcessor {
  private static final Integer SUCCESS = 0;
  private final ProcessHandler handler;
  private Key<?> outputType;
  private ServiceMessageVisitor visitor;
  private final List<EventProcessor> processors;
  private final EventProcessor unexpectedEndProcessor;

  GaugeOutputToGeneralTestEventsProcessor(@NotNull String testFrameworkName,
                                          @NotNull TestConsoleProperties consoleProperties,
                                          ProcessHandler handler) {
    super(testFrameworkName, consoleProperties);
    this.handler = handler;
    TestsCache cache = new TestsCache();
    processors = Arrays.asList(
      new SuiteEventProcessor(this, cache),
      new SpecEventProcessor(this, cache),
      new ScenarioEventProcessor(this, cache),
      new NotificationEventProcessor(this, cache),
      new StandardOutputEventProcessor(this, cache)
    );
    unexpectedEndProcessor = new UnexpectedEndProcessor(this, cache);
  }

  @Override
  public void setProcessor(@Nullable GeneralTestEventsProcessor processor) {
    super.setProcessor(processor);
    if (processor != null) {
      processor.onRootPresentationAdded("Test Suite", null, null);
    }
  }

  @Override
  protected boolean processServiceMessages(String text, Key outputType, ServiceMessageVisitor visitor) throws ParseException {
    this.outputType = outputType;
    this.visitor = visitor;
    if (text.startsWith("{")) {
      ExecutionEvent event = new GsonBuilder().create().fromJson(text, ExecutionEvent.class);
      for (EventProcessor processor : processors) {
        if (processor.canProcess(event)) return processor.process(event);
      }
    }
    if (text.trim().startsWith("Process finished with exit code") && unexpectedEndProcessor.canProcess(null)) {
      unexpectedEndProcessor.process(new ExecutionEvent() {{
        result = new ExecutionResult() {{
          status = SUCCESS.equals(handler.getExitCode()) ? ExecutionEvent.SKIP : ExecutionEvent.FAIL;
        }};
      }});
    }
    return super.processServiceMessages(text, outputType, visitor);
  }

  @Override
  public boolean process(ServiceMessageBuilder msg, Integer nodeId, Integer parentId) throws ParseException {
    msg.addAttribute("nodeId", String.valueOf(nodeId));
    msg.addAttribute("parentNodeId", String.valueOf(parentId));
    super.processServiceMessages(msg.toString(), outputType, visitor);
    return true;
  }

  @Override
  public void process(String text) {
    super.process(text, outputType);
  }

  @Override
  public boolean processLineBreak() {
    super.flushBufferOnProcessTermination(0);
    return true;
  }
}
