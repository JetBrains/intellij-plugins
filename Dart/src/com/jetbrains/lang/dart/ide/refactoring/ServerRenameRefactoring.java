/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.ide.refactoring;

import org.apache.commons.lang3.text.WordUtils;
import org.dartlang.analysis.server.protocol.*;

/**
 * The LTK wrapper around the Analysis Server 'Rename' refactoring.
 */
public class ServerRenameRefactoring extends ServerRefactoring {
  private RenameOptions options;
  private String elementKindName;
  private String oldName;

  public ServerRenameRefactoring(String file, int offset, int length) {
    super(RefactoringKind.RENAME, "Rename", file, offset, length);
  }

  public String getElementKindName() {
    return elementKindName;
  }

  public String getOldName() {
    return oldName;
  }

  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  @Override
  protected void setFeedback(RefactoringFeedback _feedback) {
    RenameFeedback feedback = (RenameFeedback)_feedback;
    elementKindName = WordUtils.capitalize(feedback.getElementKindName());
    oldName = feedback.getOldName();
    if (options == null) {
      options = new RenameOptions(oldName);
    }
  }

  public void setNewName(String newName) {
    options.setNewName(newName);
    setOptions(true, null);
  }
}
