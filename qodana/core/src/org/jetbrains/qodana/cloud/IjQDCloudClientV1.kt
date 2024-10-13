package org.jetbrains.qodana.cloud

import org.jetbrains.qodana.cloudclient.v1.QDCloudNotAuthorizedApiV1
import org.jetbrains.qodana.cloudclient.v1.QDCloudProjectApiV1
import org.jetbrains.qodana.cloudclient.v1.QDCloudUserApiV1

interface IjQDCloudClientV1 {
  fun userApi(): QDCloudUserApiV1

  fun projectApi(projectToken: String): QDCloudProjectApiV1

  fun notAuthorizedApi(): QDCloudNotAuthorizedApiV1
}