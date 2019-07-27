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

package org.osmorc.obrimport;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.HttpConfigurable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Search panel which does the actual searching.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @version $Id:$
 */
public class ObrSearchPanel extends ProgressIndicatorBase {
  public ObrSearchPanel(QueryType queryType) {
    _queryType = queryType;
    _searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        search();
      }
    });
    _cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });

    _obrBox.setRenderer(SimpleListCellRenderer.create("", Obr::getDisplayName));
    _resultList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        firePropertyChangeEvent("hasResult", null, null);
      }
    });
    updateObrs();
    onRunningChange();
    onProgressChange();
  }

  private void search() {
    Thread t = new Thread(() -> {
      final Obr selectedObr = (Obr)_obrBox.getSelectedItem();
      if (selectedObr != null) {
        start();
        if (_queryType == QueryType.Maven) {
          List result = null;
          try {
            result = Arrays.asList(selectedObr.queryForMavenArtifact(_queryString.getText(), this));
            setResults(result);
          }
          catch (final IOException e1) {
            SwingUtilities.invokeLater(() -> {
              // dialog must be run on the event dispatch thread
              // TODO: icon
              int dialogResult = Messages
                .showDialog(_rootPanel, "Could not connect to " + selectedObr.getDisplayName() + ".\n" + e1.getMessage() + ".",
                            "Connection error", new String[]{"Retry", "Cancel", "Proxy Settings"}, 0, null);
              switch (dialogResult) {
                case 2:
                  // show proxy settings
                  HttpConfigurable.editConfigurable(_rootPanel);
                  // fall through..
                case 0:
                  search();
                  break;
                default:
                  // cancel
                  break;
              }
            });
          }
        }
        stop();
      }
    }, "Obr search");
    t.start();
  }

  public JPanel getRootPanel() {
    return _rootPanel;
  }

  public void setQueryString(String queryString) {
    _queryString.setText(queryString);
  }

  private void setResults(final List results) {
    //noinspection SSBasedInspection
    SwingUtilities.invokeLater(() -> _resultList.setModel(new CollectionListModel(results)));
  }

  @Override
  protected void onProgressChange() {
    //noinspection SSBasedInspection
    SwingUtilities.invokeLater(() -> {
      _progressBar.setIndeterminate(isIndeterminate());
      _progressBar.setValue((int)(100 * getFraction()));
      _statusLabel.setText(getText());
      _cancelButton.setEnabled(isRunning() && isCancelable());
    });
  }

  @Override
  protected void onRunningChange() {
    //noinspection SSBasedInspection
    SwingUtilities.invokeLater(() -> {
      _progressBar.setEnabled(isRunning());
      _statusLabel.setEnabled(isRunning());
      _cancelButton.setEnabled(isRunning() && isCancelable());
      _searchButton.setEnabled(!isRunning());
    });
  }

  private void updateObrs() {
    ObrProvider provider = ServiceManager.getService(ObrProvider.class);
    Obr[] obrs = provider.getAvailableObrs();
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    for (Obr obr : obrs) {
      if (_queryType == QueryType.Maven && obr.supportsMaven()) {
        model.addElement(obr);
      }
    }
    _obrBox.setModel(model);
    if (model.getSize() > 0) {
      _obrBox.setSelectedIndex(0);
    }
  }

  public Object getResult() {
    // return new ObrMavenResult("foogroup", "fooartifact", "1.5", null, new SpringSourceObr());
    return _resultList.getSelectedValue();
  }

  public boolean isHasResult() {
    return _resultList.getSelectedValue() != null;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeListeners.add(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeListeners.remove(listener);
  }


  private void firePropertyChangeEvent(String property, Object oldValue, Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);
    for (PropertyChangeListener propertyChangeListener : propertyChangeListeners) {
      propertyChangeListener.propertyChange(event);
    }
  }

  public JButton getSearchButton() {
    return _searchButton;
  }

  private JTextField _queryString;
  private JList _resultList;
  private JPanel _rootPanel;
  private JProgressBar _progressBar;


  private JButton _searchButton;
  private JButton _cancelButton;
  private JLabel _statusLabel;
  private JComboBox<Obr> _obrBox;
  private final QueryType _queryType;
  private final List<PropertyChangeListener> propertyChangeListeners = ContainerUtil.createLockFreeCopyOnWriteList();
}
