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

package org.osmorc.run.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.make.BundleCompiler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.*;

/**
 * Dialog for selecting a bundle to be deployed.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public class BundleSelector extends JDialog
{
  private JPanel _contentPane;
  private JButton _buttonOK;
  private JButton _buttonCancel;
  private JList _bundlesList;
  private JTextField _searchField;
  private FrameworkInstanceDefinition _usedFramework;
  private List<SelectedBundle> _hideBundles = new ArrayList<SelectedBundle>();
  private final Project project;
  private ArrayList<SelectedBundle> _selectedBundles = new ArrayList<SelectedBundle>();
  private ArrayList<SelectedBundle> _allAvailableBundles = new ArrayList<SelectedBundle>();

  public BundleSelector(Project project)
  {
    this.project = project;
    setContentPane(_contentPane);
    setModal(true);
    setTitle(OsmorcBundle.getTranslation("bundleselector.title"));
    getRootPane().setDefaultButton(_buttonOK);
    _bundlesList.setCellRenderer(new SelectedBundleListCellRenderer());

    _buttonOK.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onOK();
      }
    });

    _buttonCancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onCancel();
      }
    });

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        onCancel();
      }
    });

    _searchField.getDocument().addDocumentListener(new DocumentAdapter()
    {
      protected void textChanged(DocumentEvent event)
      {
        updateList();
      }
    });
    _contentPane.registerKeyboardAction(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    _bundlesList.addListSelectionListener(new ListSelectionListener()
    {

      public void valueChanged(ListSelectionEvent e)
      {
        _buttonOK.setEnabled(_bundlesList.getSelectedIndex() != -1);
      }
    });
    setSize(400, 300);
  }

  public void show(JComponent owner)
  {
    setLocationRelativeTo(owner);
    setVisible(true);
  }

  private void createList()
  {
    _allAvailableBundles.clear();

    HashSet<SelectedBundle> hs = new HashSet<SelectedBundle>();
    // add all the modules
    Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules)
    {
      if (OsmorcFacet.hasOsmorcFacet(module))
      {
        SelectedBundle selectedBundle = new SelectedBundle(module.getName(), null, SelectedBundle.BundleType.Module);

        // treeset produced weird results here... so i gotta take the slow approach.

        hs.add(selectedBundle);
      }
    }
    // add all framework bundles, if there are some.
    if (_usedFramework != null)
    {
      LibraryHandler libraryHandler = ServiceManager.getService(LibraryHandler.class);

      List<Library> libs = libraryHandler.getLibraries(_usedFramework.getName());

      for (Library lib : libs)
      {
        String[] urls = lib.getUrls(OrderRootType.CLASSES);
        for (String url : urls)
        {
          url = BundleCompiler.convertJarUrlToFileUrl(url);
          url = BundleCompiler.fixFileURL(url);
          String bundleName = CachingBundleInfoProvider.getBundleSymbolicName(url);
          if (bundleName != null)
          {
            String bundleVersion = CachingBundleInfoProvider.getBundleVersions(url);
            SelectedBundle b =
                new SelectedBundle(bundleName + " - " + bundleVersion, url, SelectedBundle.BundleType.FrameworkBundle);
            hs.add(b);
          }
        }
      }
      // all the libraries that are bundles already (doesnt make much sense to start bundlified libs as they have no activator).
      for (Module module : modules)
      {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        OrderEntry[] entries = manager.getModifiableModel().getOrderEntries();
        for (OrderEntry entry : entries)
        {
          if (entry instanceof JdkOrderEntry)
          {
            continue; // no JDKs
          }

          if (entry instanceof LibraryOrderEntry &&
              libraryHandler.isFrameworkInstanceLibrary((LibraryOrderEntry) entry))
          {
            continue; // we got the framework libs already so in this case we skip it.
          }

          String[] urls = entry.getUrls(OrderRootType.CLASSES);
          for (String url : urls)
          {
            url = BundleCompiler.convertJarUrlToFileUrl(url);
            url = BundleCompiler.fixFileURL(url);


            String displayName = CachingBundleInfoProvider.getBundleSymbolicName(url);
            if (displayName != null)
            {
              // okay its a startable library
              SelectedBundle selectedBundle =
                  new SelectedBundle(displayName, url, SelectedBundle.BundleType.StartableLibrary);
              hs.add(selectedBundle);
            }
          }
        }
      }
    }
    hs.removeAll(_hideBundles);
    _allAvailableBundles.addAll(hs);
    Collections.sort(_allAvailableBundles, new TypeComparator());
  }

  private void updateList()
  {
    ArrayList<SelectedBundle> theList = new ArrayList<SelectedBundle>(_allAvailableBundles);
    // now filter
    String filterText = _searchField.getText().toLowerCase();
    DefaultListModel newModel = new DefaultListModel();
    for (SelectedBundle selectedBundle : theList)
    {
      boolean needsFiltering = filterText.length() > 0;
      if (needsFiltering && !selectedBundle.getName().toLowerCase().contains(filterText))
      {
        continue;
      }
      newModel.addElement(selectedBundle);
    }
    _bundlesList.setModel(newModel);
  }

  private void onOK()
  {
    Object[] selectedValues = _bundlesList.getSelectedValues();
    _selectedBundles = new ArrayList<SelectedBundle>();
    for (Object selectedValue : selectedValues)
    {
      _selectedBundles.add((SelectedBundle) selectedValue);
    }
    dispose();
  }

  private void onCancel()
  {
    _selectedBundles = null;
    dispose();
  }

  public void setUp(@Nullable FrameworkInstanceDefinition usedFramework, @NotNull List<SelectedBundle> hideBundles)
  {
    _usedFramework = usedFramework;
    this._hideBundles = hideBundles;
    createList();
    updateList();
  }

  @Nullable
  public List<SelectedBundle> getSelectedBundles()
  {
    return _selectedBundles;
  }

  /**
   * Comparator for sorting bundles by their type.
   *
   * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
   * @version $Id:$
   */
  public static class TypeComparator implements Comparator<SelectedBundle>
  {
    public int compare(SelectedBundle selectedBundle, SelectedBundle selectedBundle2)
    {
      return selectedBundle.getBundleType().ordinal() - selectedBundle2.getBundleType().ordinal();
    }
  }
}
