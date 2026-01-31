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
package org.osmorc.facet.ui;

import com.intellij.compiler.server.BuildManager;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ManifestEditor;
import org.osmorc.util.OsgiPsiUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * The facet editor tab which is used to set up Osmorc facet settings concerning the generation of the manifest file by
 * Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsmorcFacetManifestGenerationEditorTab extends FacetEditorTab {
  private final FacetEditorContext myEditorContext;
  private JPanel myRootPanel;
  private JTextField myBundleSymbolicName;
  private TextFieldWithBrowseButton myBundleActivator;
  private JTextField myBundleVersion;
  private JPanel myEditorPanel;
  private final ManifestEditor myAdditionalPropertiesEditor;
  private boolean myModified;

  public OsmorcFacetManifestGenerationEditorTab(FacetEditorContext editorContext) {
    myEditorContext = editorContext;

    myAdditionalPropertiesEditor = new ManifestEditor(myEditorContext.getProject(), "");
    myAdditionalPropertiesEditor.setPreferredSize(myAdditionalPropertiesEditor.getComponent().getPreferredSize());
    myEditorPanel.add(myAdditionalPropertiesEditor, BorderLayout.CENTER);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(() -> myModified = true);
    watcher.register(myRootPanel);

    myBundleActivator.addActionListener(e -> onBundleActivatorSelect());
  }

  private void updateGui() {
    boolean isManuallyEdited = myEditorContext.getUserData(OsmorcFacetGeneralEditorTab.MANUAL_MANIFEST_EDITING_KEY) == Boolean.TRUE;
    boolean isToolGenerated = myEditorContext.getUserData(OsmorcFacetGeneralEditorTab.EXT_TOOL_MANIFEST_CREATION_KEY) == Boolean.TRUE;
    boolean enabled = !(isManuallyEdited || isToolGenerated);

    myBundleSymbolicName.setEnabled(enabled);
    myBundleActivator.setEnabled(enabled);
    myBundleVersion.setEnabled(enabled);
    myAdditionalPropertiesEditor.getComponent().setEnabled(enabled);
  }

  private void onBundleActivatorSelect() {
    Project project = myEditorContext.getProject();
    PsiClass activatorClass = OsgiPsiUtil.getActivatorClass(project);
    ClassFilter filter = new TreeJavaClassChooserDialog.InheritanceJavaClassFilterImpl(activatorClass, false, true, null);
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesScope(myEditorContext.getModule());
    TreeJavaClassChooserDialog dialog = new TreeJavaClassChooserDialog(OsmorcBundle.message("facet.editor.select.bundle.activator"), project, scope, filter, null);
    dialog.showDialog();
    PsiClass psiClass = dialog.getSelected();
    if (psiClass != null) {
      myBundleActivator.setText(psiClass.getQualifiedName());
    }
  }

  @Override
  public String getDisplayName() {
    return OsmorcBundle.message("facet.tab.manifest");
  }

  @Override
  public @NotNull JComponent createComponent() {
    return myRootPanel;
  }

  @Override
  public boolean isModified() {
    return myModified;
  }

  @Override
  public void apply() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    configuration.setBundleActivator(myBundleActivator.getText());
    configuration.setBundleSymbolicName(myBundleSymbolicName.getText());
    configuration.setBundleVersion(myBundleVersion.getText());
    configuration.setAdditionalProperties(myAdditionalPropertiesEditor.getText());

    if (myModified) {
      BuildManager.getInstance().clearState(myEditorContext.getProject());
    }
    myModified = false;
  }

  @Override
  public void reset() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    myBundleActivator.setText(configuration.getBundleActivator());
    myBundleSymbolicName.setText(configuration.getBundleSymbolicName());
    myBundleVersion.setText(configuration.getBundleVersion());
    myAdditionalPropertiesEditor.setText(configuration.getAdditionalProperties());
    updateGui();
    myModified = false;
  }

  @Override
  public void onTabEntering() {
    super.onTabEntering();
    updateGui();
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(myAdditionalPropertiesEditor);
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }
}
