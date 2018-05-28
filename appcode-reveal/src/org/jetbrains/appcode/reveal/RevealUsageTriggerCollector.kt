// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.appcode.reveal

import com.intellij.internal.statistic.service.fus.collectors.FUSProjectUsageTrigger
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsageTriggerCollector
import com.intellij.openapi.project.Project

class RevealUsageTriggerCollector: ProjectUsageTriggerCollector() {
  override fun getGroupId() = "statistics.appcode.reveal"

  companion object {
    fun trigger(project: Project, feature: String) {
      FUSProjectUsageTrigger.getInstance(project).trigger(RevealUsageTriggerCollector::class.java, feature)
    }
  }
}