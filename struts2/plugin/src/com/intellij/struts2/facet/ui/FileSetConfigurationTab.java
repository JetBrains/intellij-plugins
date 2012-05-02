/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.*;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Struts2 facet tab "File Sets".
 *
 * @author Yann C&eacute;bron
 */
public class FileSetConfigurationTab extends FacetEditorTab implements Disposable {

  // GUI components -----------------------
  private JPanel myPanel;
  private JPanel headerPanel;

  private SimpleTree myTree;
  private AnActionButton myRemoveButton;
  private AnActionButton myEditButton;
  private JPanel myTreePanel;

  // GUI helpers
  private final SimpleTreeBuilder myBuilder;
  private final SimpleNode myRootNode = new SimpleNode() {
    public SimpleNode[] getChildren() {
      final List<SimpleNode> nodes = new ArrayList<SimpleNode>(myBuffer.size());
      for (final StrutsFileSet entry : myBuffer) {
        if (!entry.isRemoved()) {
          final FileSetNode setNode = new FileSetNode(entry);
          nodes.add(setNode);
        }
      }
      return ArrayUtil.toObjectArray(nodes, SimpleNode.class);
    }

    public boolean isAutoExpandNode() {
      return true;
    }

  };

  private final StrutsConfigsSearcher myConfigsSearcher;

  // original config
  private final StrutsFacetConfiguration originalConfiguration;
  private final Module module;

  // local config
  private final Set<StrutsFileSet> myBuffer = new LinkedHashSet<StrutsFileSet>();
  private boolean myModified;

  public FileSetConfigurationTab(@NotNull final StrutsFacetConfiguration strutsFacetConfiguration,
                                 @NotNull final FacetEditorContext facetEditorContext) {
    originalConfiguration = strutsFacetConfiguration;
    module = facetEditorContext.getModule();
    myConfigsSearcher = new StrutsConfigsSearcher(module);


    final HyperlinkLabel linkLabel = new HyperlinkLabel("Open Struts 2 plugin documentation…");
    linkLabel.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          BrowserUtil.launchBrowser("http://confluence.jetbrains.net/pages/viewpage.action?pageId=35367");
        }
      }
    });
    
    headerPanel.setLayout(new BorderLayout());
    headerPanel.add(linkLabel);

    // init tree
    final SimpleTreeStructure structure = new SimpleTreeStructure() {
      public Object getRootElement() {
        return myRootNode;
      }
    };

    myTree = new SimpleTree();
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true); // show expand/collapse handles
    myBuilder = new SimpleTreeBuilder(myTree, (DefaultTreeModel) myTree.getModel(), structure, null);
    myBuilder.initRoot();

    final DumbService dumbService = DumbService.getInstance(facetEditorContext.getProject());
    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(final TreeSelectionEvent e) {
        final StrutsFileSet fileSet = getCurrentFileSet();
        myEditButton.setEnabled(fileSet != null && !dumbService.isDumb());
        myRemoveButton.setEnabled(fileSet != null);
      }
    });

    myTreePanel.add(
      ToolbarDecorator.createDecorator(myTree)
        .setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            final StrutsFileSet fileSet =
              new StrutsFileSet(StrutsFileSet.getUniqueId(myBuffer),
                                StrutsFileSet.getUniqueName(StrutsBundle.message("facet.fileset.my.fileset"), myBuffer),
                                originalConfiguration) {
                public boolean isNew() {
                  return true;
                }
              };

            final FileSetEditor editor = new FileSetEditor(myPanel,
                                                           fileSet,
                                                           facetEditorContext,
                                                           myConfigsSearcher);
            editor.show();
            if (editor.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
              final StrutsFileSet editedFileSet = editor.getEditedFileSet();
              Disposer.register(strutsFacetConfiguration, editedFileSet);
              myBuffer.add(editedFileSet);
              myModified = true;
              myBuilder.updateFromRoot();
              selectFileSet(fileSet);
            }
            myTree.requestFocus();
          }
        }).setRemoveAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          remove();
          myModified = true;
          myBuilder.updateFromRoot();
          myTree.requestFocus();
        }
      }).setEditAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          final StrutsFileSet fileSet = getCurrentFileSet();
          if (fileSet != null) {
            final FileSetEditor editor = new FileSetEditor(myPanel,
                                                           fileSet,
                                                           facetEditorContext,
                                                           myConfigsSearcher);
            editor.show();
            if (editor.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
              myModified = true;
              myBuffer.remove(fileSet);
              final StrutsFileSet edited = editor.getEditedFileSet();
              Disposer.register(strutsFacetConfiguration, edited);
              myBuffer.add(edited);
              edited.setAutodetected(false);
              myBuilder.updateFromRoot();
              selectFileSet(edited);
            }
            myTree.requestFocus();
          }
        }
      }).disableUpDownActions().createPanel());

    myEditButton = ToolbarDecorator.findEditButton(myTreePanel);
    myRemoveButton = ToolbarDecorator.findRemoveButton(myTreePanel);

    AnActionButton addButton = ToolbarDecorator.findAddButton(myTreePanel);
    assert addButton != null;
    dumbService.makeDumbAware(addButton.getContextComponent(), this);
    dumbService.makeDumbAware(myEditButton.getContextComponent(), this);
  }

  @Nullable
  private StrutsFileSet getCurrentFileSet() {
    final FileSetNode currentFileSetNode = getCurrentFileSetNode();
    return currentFileSetNode == null ? null : currentFileSetNode.mySet;
  }

  @Nullable
  private FileSetNode getCurrentFileSetNode() {
    final SimpleNode selectedNode = myTree.getSelectedNode();
    if (selectedNode == null) {
      return null;
    }
    if (selectedNode instanceof FileSetNode) {
      return (FileSetNode) selectedNode;
    } else if (selectedNode.getParent() instanceof FileSetNode) {
      return (FileSetNode) selectedNode.getParent();
    } else {
      final SimpleNode parent = selectedNode.getParent();
      if (parent != null && parent.getParent() instanceof FileSetNode) {
        return (FileSetNode) selectedNode.getParent().getParent();
      }
    }
    return null;
  }

  private void selectFileSet(final StrutsFileSet fileSet) {
    myTree.select(myBuilder, new SimpleNodeVisitor() {
      public boolean accept(final SimpleNode simpleNode) {
        if (simpleNode instanceof FileSetNode) {
          if (((FileSetNode) simpleNode).mySet.equals(fileSet)) {
            return true;
          }
        }
        return false;
      }
    }, false);
  }

  private void remove() {
    final SimpleNode[] nodes = myTree.getSelectedNodesIfUniform();
    for (final SimpleNode node : nodes) {

      if (node instanceof FileSetNode) {
        final StrutsFileSet fileSet = ((FileSetNode) node).mySet;
        if (fileSet.getFiles().isEmpty()) {
          myBuffer.remove(fileSet);
          return;
        }

        final int result = Messages.showYesNoDialog(myPanel,
                                                    StrutsBundle.message("facet.fileset.remove.fileset.question",
                                                                         fileSet.getName()),
                                                    StrutsBundle.message("facet.fileset.remove.fileset.title"),
                                                    Messages.getQuestionIcon());
        if (result == DialogWrapper.OK_EXIT_CODE) {
          if (fileSet.isAutodetected()) {
            fileSet.setRemoved(true);
            myBuffer.add(fileSet);
          } else {
            myBuffer.remove(fileSet);
          }
        }
      } else if (node instanceof ConfigFileNode) {
        final VirtualFilePointer filePointer = ((ConfigFileNode) node).myFilePointer;
        final StrutsFileSet fileSet = ((FileSetNode) node.getParent()).mySet;
        fileSet.removeFile(filePointer);
      }
    }

  }

  @Nullable
  public Icon getIcon() {
    return StrutsIcons.STRUTS_CONFIG_FILE;
  }

  @Nls
  public String getDisplayName() {
    return StrutsBundle.message("facet.fileset.title");
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public boolean isModified() {
    return myModified;
  }

  public void apply() {
    final Set<StrutsFileSet> fileSets = originalConfiguration.getFileSets();
    fileSets.clear();
    for (final StrutsFileSet fileSet : myBuffer) {
      if (!fileSet.isAutodetected() || fileSet.isRemoved()) {
        fileSets.add(fileSet);
      }
    }
    originalConfiguration.setModified();
  }

  public void reset() {
    myBuffer.clear();
    final Set<StrutsFileSet> sets = StrutsManager.getInstance(module.getProject()).getAllConfigFileSets(module);
    for (final StrutsFileSet fileSet : sets) {
      myBuffer.add(/*new StrutsFileSet(fileSet)*/fileSet);
    }

    myBuilder.updateFromRoot();
    myTree.setSelectionRow(0);
  }

  public void disposeUIResources() {
    Disposer.dispose(myBuilder);
    Disposer.dispose(this);
  }

  public void dispose() {
  }

  private static class FileSetNode extends SimpleNode {

    protected final StrutsFileSet mySet;

    FileSetNode(final StrutsFileSet fileSet) {
      mySet = fileSet;

      final PresentationData presentationData = getPresentation();
      final String name = mySet.getName();

      if (fileSet.getFiles().isEmpty()) {
        presentationData.addText(name, getErrorAttributes());
        presentationData.setTooltip(StrutsBundle.message("facet.fileset.no.files.attached"));
      } else {
        presentationData.addText(name, getPlainAttributes());
        presentationData.setLocationString(Integer.toString(fileSet.getFiles().size()));
      }
    }

    public SimpleNode[] getChildren() {
      final List<SimpleNode> nodes = new ArrayList<SimpleNode>();

      for (final VirtualFilePointer file : mySet.getFiles()) {
        nodes.add(new ConfigFileNode(file, this));
      }
      return ArrayUtil.toObjectArray(nodes, SimpleNode.class);
    }

    public boolean isAutoExpandNode() {
      return true;
    }

    @NotNull
    public Object[] getEqualityObjects() {
      return new Object[]{mySet, mySet.getName(), mySet.getFiles()};
    }
  }


  private static final class ConfigFileNode extends SimpleNode {

    private final VirtualFilePointer myFilePointer;

    ConfigFileNode(final VirtualFilePointer name, final SimpleNode parent) {
      super(parent);
      myFilePointer = name;
      setIcons(StrutsIcons.STRUTS_CONFIG_FILE, StrutsIcons.STRUTS_CONFIG_FILE);
    }

    @Override
    public boolean isAlwaysLeaf() {
      return true;
    }

    protected void doUpdate() {
      final VirtualFile file = myFilePointer.getFile();
      if (file != null) {
        renderFile(file, getPlainAttributes(), null);
      } else {
        renderFile(file, getErrorAttributes(), StrutsBundle.message("facet.fileset.file.not.found"));
      }
    }

    private void renderFile(final VirtualFile file,
                            final SimpleTextAttributes textAttributes,
                            @Nullable final String toolTip) {
      final PresentationData presentation = getPresentation();
      presentation.setTooltip(toolTip);
      presentation.addText(myFilePointer.getFileName(), textAttributes);

      if (file != null) {
        presentation.setLocationString(file.getPath());
      }
    }

    public SimpleNode[] getChildren() {
      return NO_CHILDREN;
    }

  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts2.facet";
  }

}