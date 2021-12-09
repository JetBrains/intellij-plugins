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

import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.Nls;

public final class GaugeNotification {
  public @Nls String title;
  public @Nls String message;
  public String type;

  public NotificationType getType() {
    switch (type) {
      case "error":
        return NotificationType.ERROR;
      case "warning":
        return NotificationType.WARNING;
      default:
        return NotificationType.INFORMATION;
    }
  }
}
