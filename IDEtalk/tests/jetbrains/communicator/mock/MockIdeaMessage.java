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
package jetbrains.communicator.mock;

import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleColoredComponent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.IdeaLocalMessage;

import javax.swing.*;

/**
 * @author Kir
 */
public class MockIdeaMessage extends MockMessage implements IdeaLocalMessage {

  @Override
  public String getComment() {
    return "MockComment";
  }

  @Override
  public String getTitle() {
    return "MockTitle";
  }

  @Override
  public void customizeTreeNode(SimpleColoredComponent label, int refreshCounter) {
  }

  @Override
  public JComponent getPopupComponent(User user, Project project) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }
}
