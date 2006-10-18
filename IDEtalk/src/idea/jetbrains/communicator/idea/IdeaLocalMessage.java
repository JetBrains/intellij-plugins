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
package jetbrains.communicator.idea;

import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleColoredComponent;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;

import javax.swing.*;

/**
 * @author Kir
 */
public interface IdeaLocalMessage extends LocalMessage {
  String getComment();
  String getTitle();

  /** refreshCounter should be used for creating dynamic views.
   * It takes values 0..4 which change during one second (i.e. 200 msec between changes). */
  void customizeTreeNode(SimpleColoredComponent label, int refreshCounter);

  JComponent getPopupComponent(User user, Project project);
}
