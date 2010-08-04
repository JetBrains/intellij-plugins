/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.settings;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.list.SelectionInList;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class LibraryBundlingEditorComponent {
  private JPanel mainPanel;
  private JTextField libraryRegex;
  private JButton addRuleButton;
  private JButton removeRuleButton;
  private JButton duplicateButton;
  private JButton upButton;
  private JButton downButton;
  private JList libraries;
  private ManifestEditor manifestEntries;
  private JCheckBox neverBundle;
  private JCheckBox stopAfterThisRule;
  private JPanel _manifestEntriesHolder;
  private SelectionInList<LibraryBundlificationRule> selectedRule;
  private boolean modified;
  private PropertyChangeListener beanPropertyChangeListener;
  private BeanAdapter<LibraryBundlificationRule> beanAdapter;
  private List<LibraryBundlificationRule> rules;

  public LibraryBundlingEditorComponent() {

    addRuleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        LibraryBundlificationRule newRule = new LibraryBundlificationRule();
        rules.add(newRule);
        selectedRule.fireIntervalAdded(rules.size() - 1, rules.size() - 1);
        selectedRule.setSelection(newRule);
        notifyChanged();
      }
    });

    removeRuleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (rules.size() == 1) {
          LibraryBundlificationRule newRule = new LibraryBundlificationRule();
          rules.set(0, newRule);
          selectedRule.fireContentsChanged(0, 1);
          selectedRule.setSelection(newRule);
        }
        else {
          int oldSelectionIndex = selectedRule.getSelectionIndex();
          rules.remove(selectedRule.getValue());
          selectedRule.fireIntervalRemoved(rules.size(), rules.size());
          final int newSelectionIndex = oldSelectionIndex > 0 ? oldSelectionIndex - 1 : oldSelectionIndex;

          if (newSelectionIndex == oldSelectionIndex) {
            // force a change event, since the underlying list has changed and this needs to be reflected in the text fields
            selectedRule.clearSelection();
          }

          selectedRule.setSelectionIndex(newSelectionIndex);
        }
        notifyChanged();
      }
    });

    duplicateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        LibraryBundlificationRule rule = selectedRule.getSelection();
        if (rule != null) {
          LibraryBundlificationRule newRule = rule.copy();
          int selectedIndex = selectedRule.getSelectionIndex();
          rules.add(selectedIndex, newRule);
          selectedRule.fireIntervalAdded(selectedIndex, selectedIndex);
          notifyChanged();
        }
      }
    });

    upButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int selectionIndex = selectedRule.getSelectionIndex();
        if (selectionIndex > 0) {
          LibraryBundlificationRule ruleToMove = rules.get(selectionIndex);
          rules.set(selectionIndex, rules.get(selectionIndex - 1));
          rules.set(selectionIndex - 1, ruleToMove);
          selectedRule.fireContentsChanged(selectionIndex - 1, selectionIndex);
          selectedRule.setSelectionIndex(selectionIndex - 1);
          notifyChanged();
        }
      }
    });

    downButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int selectionIndex = selectedRule.getSelectionIndex();
        if (selectionIndex < rules.size() - 1) {
          LibraryBundlificationRule ruleToMove = rules.get(selectionIndex);
          rules.set(selectionIndex, rules.get(selectionIndex + 1));
          rules.set(selectionIndex + 1, ruleToMove);
          selectedRule.fireContentsChanged(selectionIndex, selectionIndex + 1);
          selectedRule.setSelectionIndex(selectionIndex + 1);
          notifyChanged();
        }
      }
    });

    // Am not sure if this is really a good idea. However using the default project produces some nice NPEs somehwere
    // deep inside the EnterHandler.
    final DataContext dataContext = DataManager.getInstance().getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    manifestEntries = new ManifestEditor(project, "");
    Bindings.bind(manifestEntries, "text", beanAdapter.getValueModel("additionalProperties"));
    _manifestEntriesHolder.add(manifestEntries, BorderLayout.CENTER);

    beanAdapter.addBeanPropertyChangeListener(beanPropertyChangeListener);
  }

  private void notifyChanged() {
    modified = true;
  }


  public void applyTo(ApplicationSettings settings) {
    settings.setLibraryBundlificationRules(rules);
    modified = false;
  }

  public void dispose() {
    manifestEntries = null;
    _manifestEntriesHolder.removeAll();
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public boolean isModified() {
    return modified;
  }

  public void resetTo(ApplicationSettings settings) {
    rules = new ArrayList<LibraryBundlificationRule>(settings.getLibraryBundlificationRules());
    selectedRule.setList(rules);
    selectedRule.setSelectionIndex(0);
    modified = false;
  }

  private void createUIComponents() {
    selectedRule = new SelectionInList<LibraryBundlificationRule>();
    libraries = BasicComponentFactory.createList(selectedRule);
    // adapter always holds currently selected bean
    beanAdapter = new BeanAdapter<LibraryBundlificationRule>(new LibraryBundlificationRule());
    libraryRegex = BasicComponentFactory.createTextField(beanAdapter.getValueModel("ruleRegex"), false);
    neverBundle = BasicComponentFactory.createCheckBox(beanAdapter.getValueModel("doNotBundle"), "");
    stopAfterThisRule = BasicComponentFactory.createCheckBox(beanAdapter.getValueModel("stopAfterThisRule"), "");
    selectedRule.addValueChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        // put currently selected bean into adapter, so all the textfields know which bean to work on.
        beanAdapter.setBean(selectedRule.getSelection());
      }
    });

    beanPropertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        selectedRule.fireContentsChanged(selectedRule.getSelectionIndex(), 1);
        notifyChanged();
      }
    };
  }
}
