// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.init

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle

open class GraziProjectInit : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        GraziStateLifecycle.publisher.init(GraziConfig.get(), project)
    }
}

