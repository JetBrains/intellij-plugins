package training.statistic

import com.intellij.openapi.application.PermanentInstallationID

/**
 * Created by jetbrains on 03/08/16.
 */


class FeedbackEvent(feedback: String) {
    @Transient var recorderId = "training"
    @Transient var timestamp = System.currentTimeMillis()
    @Transient var sessionUid: String = "1"
    @Transient var actionType: String = "post.feedback"
    @Transient var userUid: String = PermanentInstallationID.get()
    @Transient var feedback = feedback

    override fun toString(): String {
        return "${timestamp}\t${recorderId}\t${userUid}\t${sessionUid}\t${actionType}\t$feedback}"
    }

}