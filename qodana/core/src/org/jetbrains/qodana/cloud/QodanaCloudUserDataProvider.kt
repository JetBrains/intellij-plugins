package org.jetbrains.qodana.cloud

import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import kotlin.time.Duration.Companion.seconds

private val USER_INFO_REFRESH_PERIOD = 30.seconds

class QodanaCloudUserDataProvider(
  private val authorized: UserState.Authorized,
  initialUserInfo: QDCloudSchema.UserInfo
) {
  val cloudUserPrimaryData = CloudUserPrimaryData(initialUserInfo.id)

  private val userInfoProperty = RefreshableProperty<QDCloudResponse<QDCloudSchema.UserInfo>>(
    USER_INFO_REFRESH_PERIOD,
    QDCloudResponse.Success(initialUserInfo)
  ) {
    qodanaCloudResponse {
      authorized.userApi().value()
        .getUserInfo().value()
    }
  }

  val userInfo: StateFlow<RefreshableProperty.PropertyState<QDCloudResponse<QDCloudSchema.UserInfo>>> =
    userInfoProperty.propertyState

  suspend fun refreshUserInfo(): RefreshableProperty.PropertyState<QDCloudResponse<QDCloudSchema.UserInfo>> =
    userInfoProperty.refreshManually()

  suspend fun refreshUserInfoLoop() = userInfoProperty.refreshLoop()

  suspend fun startComputeRequestsProcessing() {
    userInfoProperty.startRequestsProcessing()
  }
}

data class CloudUserPrimaryData(
  val id: String
)