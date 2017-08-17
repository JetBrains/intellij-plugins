/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package training.components

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.ApplicationComponent
import training.actions.StartLearnAction

class LearnApplicationComponent : ApplicationComponent {

  override fun getComponentName(): String = "IDE Features Trainer application level component"

  override fun disposeComponent() {
  }

  override fun initComponent() {
    if (!StartLearnAction.isEnabled()) removeWelcomeFrameAction()
  }

  private fun removeWelcomeFrameAction() {
    ActionManager.getInstance().unregisterAction(StartLearnAction.ACTION_ID)
  }

}