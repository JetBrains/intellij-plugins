/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ChangeListChooser {
  private final JComboBox myChangeList;

  private final P4Connection myConnection;
  private final PerforceRunner myRunner;

  public ChangeListChooser(final JComboBox changeLists,
                           final JButton newButton,
                           Project project,
                           final P4Connection connection) {
    myChangeList = changeLists;
    myConnection = connection;
    myRunner = PerforceRunner.getInstance(project);

    if (newButton != null) {
      newButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          final String description = Messages.showInputDialog(PerforceBundle.message("request.text.enter.change.description"), PerforceBundle.message("request.title.create.new.change.list"), Messages.getQuestionIcon());
          if (description != null) {
            try {
              long newChangeListNumber = myRunner.createChangeList(description, connection, null);
              refreshChangeLists(newChangeListNumber);
            }
            catch (VcsException e1) {
              Messages.showErrorDialog(PerforceBundle.message("message.text.cannot.create.new.changelist", e1.getLocalizedMessage()), PerforceBundle.message("message.title.create.new.changelist"));
            }
          }
        }
      });
    }
    myChangeList.setRenderer(new ColoredListCellRenderer() {
      @Override
      protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof PerforceChangeList changeList) {
          String description = changeList.getComment();
          if (description.length() > 30) {
            description = description.substring(0, 25) + "...";
          }
          append("#" + changeList.getNumber() + " (" + description + ")", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        else if (value != null) {
          //noinspection HardCodedStringLiteral
          append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }
    });
  }

  private void refreshChangeLists(long newChangeListNumber) {
    final long selected = newChangeListNumber > 0 ? newChangeListNumber : getChangeListNumber();
    try {
      fillChangeLists(myRunner.getPendingChangeLists(myConnection), selected);
    }
    catch (VcsException e1) {
      Messages.showErrorDialog(PerforceBundle.message("message.text.cannot.load.changes", e1.getLocalizedMessage()), PerforceBundle.message("message.title.refresh.changes"));
    }
  }

  public void fillChangeLists(final List<PerforceChangeList> changeLists, long selection) {
    myChangeList.removeAllItems();
    myChangeList.addItem(getDefault());
    Object selected = getDefault();
    for (PerforceChangeList changeListInfo : changeLists) {
      myChangeList.addItem(changeListInfo);
      if (changeListInfo.getNumber() == selection) {
        selected = changeListInfo;
      }
    }
    myChangeList.setSelectedItem(selected);
  }

  public long getChangeListNumber() {
    final ComboBoxModel model = myChangeList.getModel();
    final Object selectedItem = model.getSelectedItem();
    if (selectedItem instanceof PerforceChangeList) {
      return ((PerforceChangeList)selectedItem).getNumber();
    } else {
      return -1;
    }
  }

  private static @Nls String getDefault() {
    return PerforceBundle.message("default.changelist.presentation");
  }
}
