package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.tapestry.core.events.FileSystemListenerAdapter;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TapestryToolWindow extends FileSystemListenerAdapter {

    private JPanel              _mainPanel;
    private JTabbedPane         _tabbedPane;
    private final DocumentationTab    _documentationTab;
   private final DependenciesTab _dependenciesTab;

    private final List<IJavaClassType> _updateOnChangeFiles = new ArrayList<>();
    private Module _module;
    private Object _element;
    private final Project _project;

    public TapestryToolWindow(final Project project) {
        _project = project;
        _tabbedPane.removeAll();

        _documentationTab = new DocumentationTab(_project);
        _tabbedPane.addTab("Live Documentation", _documentationTab.getMainPanel());

        _dependenciesTab = new DependenciesTab();
       _tabbedPane.addTab("Dependencies", _dependenciesTab.getMainPanel());

        ModuleListener moduleListener = new ModuleListener() {
            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                reload();
            }

            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                reload();
            }
        };

        MessageBusConnection messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(ProjectTopics.MODULES, moduleListener);

        reload();
    }


    @Override
    public void fileDeleted(String path) {
        if (_element == null || _module == null) {
            return;
        }

        _documentationTab.showDocumentation(_element, _project);
        _documentationTab.setElement(_element);
        _dependenciesTab.showDependencies(_module, _element);
    }

    @Override
    public void fileContentsChanged(IResource changedFile) {
        if (_element == null || _module == null) {
            return;
        }

        for (IJavaClassType classType : _updateOnChangeFiles) {
            IResource resource = classType.getFile();

            if (resource == null) {
                continue;
            }

            if (resource.getFile() != null && resource.getFile().getAbsolutePath().endsWith(changedFile.getFile().getAbsolutePath())) {
                _documentationTab.showDocumentation(_element, _project);
                _documentationTab.setElement(_element);
                _dependenciesTab.showDependencies(_module, _element);
            }
        }
    }

    /**
     * Updates the toolwindow state.
     *
     * @param module              the module the element belongs to.
     * @param element             the element to update for.
     * @param updateOnChangeFiles the list of files to update
     */
    public void update(Module module, Object element, final List<IJavaClassType> updateOnChangeFiles) {
        _module = module;
        _element = element;

        _documentationTab.showDocumentation(element, _project);
        _documentationTab.setElement(_element);
        _dependenciesTab.showDependencies(module, element);

        if (element != null) {
            _updateOnChangeFiles.clear();
            _updateOnChangeFiles.addAll(updateOnChangeFiles);
        }
    }

    public JPanel getMainPanel() {
        return _mainPanel;
    }

    public DocumentationTab getDocumentationTab() {
        return _documentationTab;
    }

    public DependenciesTab getDependenciesTab() {
        return _dependenciesTab;
    }

    private void reload() {
        for (Module module : ModuleManager.getInstance(_project).getModules()) {
            TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().removeFileSystemListener(this);

            TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().addFileSystemListener(this);
        }
    }
}
