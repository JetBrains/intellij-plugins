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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.list.SelectionInList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class LibraryBundlingEditor implements Configurable, ApplicationSettingsAwareEditor {
    private ApplicationSettingsProvider applicationSettingsProvider;

    public LibraryBundlingEditor(ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier) {
        this.applicationSettingsUpdateNotifier = applicationSettingsUpdateNotifier;
        addRuleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                List<LibraryBundlificationRule> list = getApplicationSettingsWorkingCopy().getLibraryBundlificationRules();
                LibraryBundlificationRule newRule = new LibraryBundlificationRule();
                list.add(newRule);
                selectedRule.fireIntervalAdded(list.size() - 1, list.size() - 1);
                selectedRule.setSelection(newRule);
                notifyChanged();
            }
        });

        removeRuleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                List<LibraryBundlificationRule> list = getApplicationSettingsWorkingCopy().getLibraryBundlificationRules();
                if (list.size() == 1) {
                    LibraryBundlificationRule newRule = new LibraryBundlificationRule();
                    list.set(0, newRule);
                    selectedRule.fireContentsChanged(0, 1);
                    selectedRule.setSelection(newRule);
                } else {
                    int oldSelectionIndex = selectedRule.getSelectionIndex();
                    list.remove(selectedRule.getValue());
                    selectedRule.fireIntervalRemoved(list.size(), list.size());
                    selectedRule.setSelectionIndex(oldSelectionIndex > 0 ? oldSelectionIndex - 1 : oldSelectionIndex);
                }
                notifyChanged();
            }
        });

        duplicateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                LibraryBundlificationRule rule = selectedRule.getSelection();
                if (rule != null) {
                    LibraryBundlificationRule newRule = rule.copy();
                    List<LibraryBundlificationRule> list = getApplicationSettingsWorkingCopy().getLibraryBundlificationRules();
                    int selectedIndex = selectedRule.getSelectionIndex();
                    list.add(selectedIndex, newRule);
                    selectedRule.fireIntervalAdded(selectedIndex, selectedIndex);
                    notifyChanged();
                }
            }
        });

        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                List<LibraryBundlificationRule> list = getApplicationSettingsWorkingCopy().getLibraryBundlificationRules();
                int selectionIndex = selectedRule.getSelectionIndex();
                if (selectionIndex > 0) {
                    LibraryBundlificationRule ruleToMove = list.get(selectionIndex);
                    list.set(selectionIndex, list.get(selectionIndex - 1));
                    list.set(selectionIndex - 1, ruleToMove);
                    selectedRule.fireContentsChanged(selectionIndex - 1, selectionIndex);
                    selectedRule.setSelectionIndex(selectionIndex - 1);
                    notifyChanged();
                }
            }
        });

        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                List<LibraryBundlificationRule> list = getApplicationSettingsWorkingCopy().getLibraryBundlificationRules();
                int selectionIndex = selectedRule.getSelectionIndex();
                if (selectionIndex < list.size() - 1) {
                    LibraryBundlificationRule ruleToMove = list.get(selectionIndex);
                    list.set(selectionIndex, list.get(selectionIndex + 1));
                    list.set(selectionIndex + 1, ruleToMove);
                    selectedRule.fireContentsChanged(selectionIndex, selectionIndex + 1);
                    selectedRule.setSelectionIndex(selectionIndex + 1);
                    notifyChanged();
                }
            }
        });


    }

    private void notifyChanged() {
        applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
        changed = true;
    }

    private void createUIComponents() {
        selectedRule = new SelectionInList<LibraryBundlificationRule>();
        libraries = BasicComponentFactory.createList(selectedRule);
        // adapter always holds currently selected bean
        beanAdapter = new BeanAdapter<LibraryBundlificationRule>(new LibraryBundlificationRule());
        libraryRegex = BasicComponentFactory.createTextField(beanAdapter.getValueModel("ruleRegex"), false);
        manifestEntries = BasicComponentFactory.createTextArea(beanAdapter.getValueModel("additionalProperties"), false);
        neverBundle = BasicComponentFactory.createCheckBox(beanAdapter.getValueModel("doNotBundle"), "");
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

    @Nls
    public String getDisplayName() {
        return "Library Bundling";
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        beanAdapter.addBeanPropertyChangeListener(beanPropertyChangeListener);
        return mainPanel;
    }

    public boolean isModified() {
        return changed;
    }

    public void apply() throws ConfigurationException {
        copySettings(getApplicationSettingsWorkingCopy(), getApplicationSettings());
        changed = false;
    }

    public void reset() {
        if (getApplicationSettings() != null) {
            copySettings(getApplicationSettings(), getApplicationSettingsWorkingCopy());

            selectedRule.setList(getApplicationSettingsWorkingCopy().getLibraryBundlificationRules());
            selectedRule.setSelectionIndex(0);
            changed = false;
        }
    }

    private void copySettings(ApplicationSettings from, ApplicationSettings to) {
        List<LibraryBundlificationRule> copiedRules = new ArrayList<LibraryBundlificationRule>();
        for (LibraryBundlificationRule libraryBundlificationRule : from.getLibraryBundlificationRules()) {
            LibraryBundlificationRule copiedRule = new LibraryBundlificationRule();
            XmlSerializerUtil.copyBean(libraryBundlificationRule, copiedRule);
            copiedRules.add(copiedRule);
        }
        to.setLibraryBundlificationRules(copiedRules);
    }

    public void disposeUIResources() {
        beanAdapter.removeBeanPropertyChangeListener(beanPropertyChangeListener);
    }

    public void setApplicationSettingsProvider(
            @NotNull ApplicationSettingsProvider applicationSettingsProvider) {
        this.applicationSettingsProvider = applicationSettingsProvider;
        reset();
    }

    private ApplicationSettings getApplicationSettings() {
        return applicationSettingsProvider != null ? applicationSettingsProvider.getApplicationSettings() : null;
    }

    private ApplicationSettings getApplicationSettingsWorkingCopy() {
        return applicationSettingsProvider != null ? applicationSettingsProvider.getApplicationSettingsWorkingCopy() : null;
    }

    private JPanel mainPanel;
    private JTextField libraryRegex;
    private JButton addRuleButton;
    private JButton removeRuleButton;
    private JButton duplicateButton;
    private JButton upButton;
    private JButton downButton;
    private JList libraries;
    private JTextArea manifestEntries;
    private JCheckBox neverBundle;
    private SelectionInList<LibraryBundlificationRule> selectedRule;
    private boolean changed;
    private ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier;
    private PropertyChangeListener beanPropertyChangeListener;
    private BeanAdapter<LibraryBundlificationRule> beanAdapter;
}
