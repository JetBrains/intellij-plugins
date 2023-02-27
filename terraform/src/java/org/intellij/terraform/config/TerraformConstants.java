// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.wm.ToolWindowId;

public final class TerraformConstants {
  public static final NotificationGroup EXECUTION_NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("Terraform Execution");
}
