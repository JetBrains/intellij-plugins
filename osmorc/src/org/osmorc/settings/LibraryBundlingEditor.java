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
public class LibraryBundlingEditor implements Configurable, ApplicationSettingsAwareEditor
{
  public LibraryBundlingEditor(ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier)
  {
    _applicationSettingsUpdateNotifier = applicationSettingsUpdateNotifier;
    _addRuleButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        List<LibraryBundlificationRule> list = _applicationSettingsWorkingCopy.getLibraryBundlificationRules();
        LibraryBundlificationRule newRule = new LibraryBundlificationRule();
        list.add(newRule);
        _selectedRule.fireIntervalAdded(list.size() - 1, list.size() - 1);
        _selectedRule.setSelection(newRule);
        notifyChanged();
      }
    });

    _removeRuleButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        List<LibraryBundlificationRule> list = _applicationSettingsWorkingCopy.getLibraryBundlificationRules();
        if (list.size() == 1)
        {
          LibraryBundlificationRule newRule = new LibraryBundlificationRule();
          list.set(0, newRule);
          _selectedRule.fireContentsChanged(0, 1);
          _selectedRule.setSelection(newRule);
        }
        else
        {
          int oldSelectionIndex = _selectedRule.getSelectionIndex();
          list.remove(_selectedRule.getValue());
          _selectedRule.fireIntervalRemoved(list.size(), list.size());
          _selectedRule.setSelectionIndex(oldSelectionIndex > 0 ? oldSelectionIndex - 1 : oldSelectionIndex);
        }
        notifyChanged();
      }
    });

    _duplicateButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        LibraryBundlificationRule rule = _selectedRule.getSelection();
        if (rule != null)
        {
          LibraryBundlificationRule newRule = rule.copy();
          List<LibraryBundlificationRule> list = _applicationSettingsWorkingCopy.getLibraryBundlificationRules();
          int selectedIndex = _selectedRule.getSelectionIndex();
          list.add(selectedIndex, newRule);
          _selectedRule.fireIntervalAdded(selectedIndex, selectedIndex);
          notifyChanged();
        }
      }
    });

    _upButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        List<LibraryBundlificationRule> list = _applicationSettingsWorkingCopy.getLibraryBundlificationRules();
        int selectionIndex = _selectedRule.getSelectionIndex();
        if (selectionIndex > 0)
        {
          LibraryBundlificationRule ruleToMove = list.get(selectionIndex);
          list.set(selectionIndex, list.get(selectionIndex - 1));
          list.set(selectionIndex - 1, ruleToMove);
          _selectedRule.fireContentsChanged(selectionIndex - 1, selectionIndex);
          _selectedRule.setSelectionIndex(selectionIndex - 1);
          notifyChanged();
        }
      }
    });

    _downButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        List<LibraryBundlificationRule> list = _applicationSettingsWorkingCopy.getLibraryBundlificationRules();
        int selectionIndex = _selectedRule.getSelectionIndex();
        if (selectionIndex < list.size() - 1)
        {
          LibraryBundlificationRule ruleToMove = list.get(selectionIndex);
          list.set(selectionIndex, list.get(selectionIndex + 1));
          list.set(selectionIndex + 1, ruleToMove);
          _selectedRule.fireContentsChanged(selectionIndex, selectionIndex + 1);
          _selectedRule.setSelectionIndex(selectionIndex + 1);
          notifyChanged();
        }
      }
    });


  }

  private void notifyChanged()
  {
    _applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
    _changed = true;
  }

  private void createUIComponents()
  {
    _selectedRule = new SelectionInList<LibraryBundlificationRule>();
    _libraries = BasicComponentFactory.createList(_selectedRule);
    // adapter always holds currently selected bean
    _beanAdapter = new BeanAdapter<LibraryBundlificationRule>(new LibraryBundlificationRule());
    _libraryRegex = BasicComponentFactory.createTextField(_beanAdapter.getValueModel("ruleRegex"), false);
    _manifestEntries = BasicComponentFactory.createTextArea(_beanAdapter.getValueModel("additionalProperties"), false);
    _neverBundle = BasicComponentFactory.createCheckBox(_beanAdapter.getValueModel("doNotBundle"), "");
    _selectedRule.addValueChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent event)
      {
        // put currently selected bean into adapter, so all the textfields know which bean to work on.
        _beanAdapter.setBean(_selectedRule.getSelection());
      }
    });

    _beanPropertyChangeListener = new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent event)
      {
        _selectedRule.fireContentsChanged(_selectedRule.getSelectionIndex(), 1);
        notifyChanged();
      }
    };
  }

  @Nls
  public String getDisplayName()
  {
    return "Library Bundling";
  }

  public Icon getIcon()
  {
    return null;
  }

  public String getHelpTopic()
  {
    return null;
  }

  public JComponent createComponent()
  {
    _beanAdapter.addBeanPropertyChangeListener(_beanPropertyChangeListener);
    return _mainPanel;
  }

  public boolean isModified()
  {
    return _changed;
  }

  public void apply() throws ConfigurationException
  {
    copySettings(_applicationSettingsWorkingCopy, _applicationSettings);
    _changed = false;
  }

  public void reset()
  {
    if (_applicationSettings != null)
    {
      copySettings(_applicationSettings, _applicationSettingsWorkingCopy);

      _selectedRule.setList(_applicationSettingsWorkingCopy.getLibraryBundlificationRules());
      _selectedRule.setSelectionIndex(0);
      _changed = false;
    }
  }

  private void copySettings(ApplicationSettings from, ApplicationSettings to)
  {
    List<LibraryBundlificationRule> copiedRules = new ArrayList<LibraryBundlificationRule>();
    for (LibraryBundlificationRule libraryBundlificationRule : from.getLibraryBundlificationRules())
    {
      LibraryBundlificationRule copiedRule = new LibraryBundlificationRule();
      XmlSerializerUtil.copyBean(libraryBundlificationRule, copiedRule);
      copiedRules.add(copiedRule);
    }
    to.setLibraryBundlificationRules(copiedRules);
  }

  public void disposeUIResources()
  {
    _beanAdapter.removeBeanPropertyChangeListener(_beanPropertyChangeListener);
  }

  public void setApplicationSettings(ApplicationSettings applicationSettings,
                                     ApplicationSettings applicationSettingsWorkingCopy)
  {
    _applicationSettings = applicationSettings;
    _applicationSettingsWorkingCopy = applicationSettingsWorkingCopy;
    reset();
  }


  private JPanel _mainPanel;
  private JTextField _libraryRegex;
  private JButton _addRuleButton;
  private JButton _removeRuleButton;
  private JButton _duplicateButton;
  private JButton _upButton;
  private JButton _downButton;
  private JList _libraries;
  private JTextArea _manifestEntries;
  private JCheckBox _neverBundle;
  private SelectionInList<LibraryBundlificationRule> _selectedRule;
  private boolean _changed;
  private ApplicationSettings _applicationSettings;
  private ApplicationSettings _applicationSettingsWorkingCopy;
  private ApplicationSettingsUpdateNotifier _applicationSettingsUpdateNotifier;
  private PropertyChangeListener _beanPropertyChangeListener;
  private BeanAdapter<LibraryBundlificationRule> _beanAdapter;
}
