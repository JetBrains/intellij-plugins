// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.msg

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import tanvd.grazi.GraziConfig

interface GraziStateLifecycle {
    companion object {
        val topic = Topic.create("grazi_state_lifecycle_topic", GraziStateLifecycle::class.java)
        val publisher by lazy { ApplicationManager.getApplication().messageBus.syncPublisher(topic) }
    }

    /** Initialize Grazi with passed state */
    fun init(state: GraziConfig.State, project: Project) {}

    /** Update state of object */
    fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {}
}
