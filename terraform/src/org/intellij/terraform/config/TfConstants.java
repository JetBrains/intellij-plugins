// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;

public final class TfConstants {
  public static NotificationGroup getNotificationGroup()  {
    return NotificationGroupManager.getInstance().getNotificationGroup("Terraform Execution");
  }
}
