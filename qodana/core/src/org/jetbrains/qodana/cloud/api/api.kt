package org.jetbrains.qodana.cloud.api

import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema

val QDCloudSchema.UserInfo.name: String
  get() = fullName ?: username ?: id