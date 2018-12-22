package com.intellij.tapestry.intellij.actions.createnew;

import com.intellij.javaee.web.WebRoot;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.ui.DialogWrapperPeer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.IdeaUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Dialog to create a new component.
 */
public class AddNewComponentDialog extends JDialog {

    private JPanel _contentPane;
    private JTextField _name;
    private JComboBox _templateSourceDirectoryCombo;
    private JComboBox _classSourceDirectoryCombo;
    private JCheckBox _replaceExistingFilesCheck;
    private JCheckBox _createTemplateCheck;

    public AddNewComponentDialog(Module module, String selectedPackage, boolean isPage) {
        setContentPane(_contentPane);
        setModal(true);
        String newTemplatesSourceDirectory;
        String newClassesSourceDirectory;

        _name.setText(selectedPackage);
        _name.putClientProperty(DialogWrapperPeer.HAVE_INITIAL_SELECTION, Boolean.FALSE);

        if (isPage) {
            newTemplatesSourceDirectory = TapestryModuleSupportLoader.getInstance(module).getState().getNewPagesTemplatesSourceDirectory();
            newClassesSourceDirectory = TapestryModuleSupportLoader.getInstance(module).getState().getNewPagesClassesSourceDirectory();
        } else {
            newTemplatesSourceDirectory = TapestryModuleSupportLoader.getInstance(module).getState().getNewComponentsTemplatesSourceDirectory();
            newClassesSourceDirectory = TapestryModuleSupportLoader.getInstance(module).getState().getNewComponentsClassesSourceDirectory();
        }

        SourceFolder[] sourceFolders = ModuleRootManager.getInstance(module).getContentEntries()[0].getSourceFolders();
        for (SourceFolder sourceFolder : sourceFolders) {
            if (sourceFolder.getFile() == null)
                continue;

            RootFolderWrapper folderWrapper = new RootFolderWrapper(sourceFolder);

            _templateSourceDirectoryCombo.addItem(folderWrapper);
            if (folderWrapper.toString().equals(newTemplatesSourceDirectory))
                _templateSourceDirectoryCombo.setSelectedItem(folderWrapper);

            _classSourceDirectoryCombo.addItem(folderWrapper);
            if (folderWrapper.toString().equals(newClassesSourceDirectory))
                _classSourceDirectoryCombo.setSelectedItem(folderWrapper);
        }

        List<WebRoot> webRoots = IdeaUtils.findWebRoots(module);
        for (WebRoot webRoot : webRoots) {
            if (webRoot.getFile() == null)
                continue;

            RootFolderWrapper folderWrapper = new RootFolderWrapper(webRoot);

            if (isPage)
                _templateSourceDirectoryCombo.addItem(folderWrapper);

            if (folderWrapper.toString().equals(newTemplatesSourceDirectory))
                _templateSourceDirectoryCombo.setSelectedItem(folderWrapper);
        }

        _createTemplateCheck.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setNotCreatingTemplate(!_createTemplateCheck.isSelected());
                    }
                }
        );
    }

    @Override
    public JPanel getContentPane() {
        return _contentPane;
    }

    public String getNewComponentName() {
        return _name.getText();
    }

    public boolean isReplaceExistingFiles() {
        return _replaceExistingFilesCheck.isSelected();
    }

    public boolean isNotCreatingTemplate() {
        return _createTemplateCheck.isSelected();
    }

    public void setNotCreatingTemplate(boolean enabled) {
        _templateSourceDirectoryCombo.setEnabled(enabled);
    }

    public VirtualFile getTemplateSourceDirectory() {
        return ((RootFolderWrapper) _templateSourceDirectoryCombo.getSelectedItem()).getFolder();
    }

    public VirtualFile getClassSourceDirectory() {
        return ((RootFolderWrapper) _classSourceDirectoryCombo.getSelectedItem()).getFolder();
    }

    public JComponent getNameComponent() {
        return _name;
    }

    /**
     * A wrapper class for the root folder.
     */
    static class RootFolderWrapper {

        SourceFolder _javaRootFolder;
        WebRoot _webRootFolder;

        RootFolderWrapper(SourceFolder javaRootFolder) {
            _javaRootFolder = javaRootFolder;
        }

        RootFolderWrapper(WebRoot webRootFolder) {
            _webRootFolder = webRootFolder;
        }

        public VirtualFile getFolder() {
            return _javaRootFolder != null ? _javaRootFolder.getFile() : _webRootFolder.getFile();
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _javaRootFolder != null ? _javaRootFolder.getFile().getPath() : _webRootFolder.getFile().getPath();
        }
    }
}
