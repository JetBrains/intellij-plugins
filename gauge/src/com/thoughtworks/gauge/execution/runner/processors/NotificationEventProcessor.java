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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.thoughtworks.gauge.NotificationGroups;
import com.thoughtworks.gauge.execution.runner.MessageProcessor;
import com.thoughtworks.gauge.execution.runner.TestsCache;
import com.thoughtworks.gauge.execution.runner.event.ExecutionEvent;

public final class NotificationEventProcessor extends GaugeEventProcessor {

  public NotificationEventProcessor(MessageProcessor processor, TestsCache cache) {
    super(processor, cache);
  }

  @Override
  protected boolean onStart(ExecutionEvent event) {
    return true;
  }

  @Override
  protected boolean onEnd(ExecutionEvent event) {
    String title = event.notification.title;
    String message = event.notification.message;
    new Notification(NotificationGroups.GAUGE_GROUP, title, message, event.notification.getType())
      .setListener(NotificationListener.URL_OPENING_LISTENER)
      .notify(null);
    return true;
  }

  @Override
  public boolean canProcess(ExecutionEvent event) {
    return event.type.equalsIgnoreCase(ExecutionEvent.NOTIFICATION);
  }
}
