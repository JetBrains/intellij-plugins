/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea.config;

import jetbrains.communicator.OptionFlag;

/**
 * @author Kir
 */
public interface IdeaFlags {
  OptionFlag USE_ENTER_FOR_MESSAGES = new OptionFlag(false, "USE_ENTER_FOR_MESSAGES");
  OptionFlag EXPAND_ON_MESSAGE = new OptionFlag(true, "EXPAND_ON_MESSAGE");
  OptionFlag SOUND_ON_MESSAGE = new OptionFlag(true, "SOUND_ON_MESSAGE");
  OptionFlag POPUP_ON_MESSAGE = new OptionFlag(false, "POPUP_ON_MESSAGE");
  OptionFlag ACTIVATE_WINDOW_ON_MESSAGE = new OptionFlag(true, "ACTIVATE_WINDOW_ON_MESSAGE");
}
