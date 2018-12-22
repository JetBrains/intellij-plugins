package com.intellij.tapestry.intellij.actions.createnew;

import com.intellij.javaee.web.WebRoot;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;

import javax.swing.*;

/**
 * Dialog to add a new mixin.
 */
public class AddNewMixinDialog extends JDialog {

    private JPanel _contentPane;
    private JTextField _name;
    private JComboBox _classSourceDirectoryCombo;
    private JCheckBox _replaceExistingFilesCheck;

    public AddNewMixinDialog(Module module, String selectedPackage) {
        setContentPane(_contentPane);
        setModal(true);
        String newClassesSourceDirectory;

        _name.setText(selectedPackage);

        newClassesSourceDirectory = TapestryModuleSupportLoader.getInstance(module).getState().getNewMixinsClassesSourceDirectory();

        SourceFolder[] sourceFolders = ModuleRootManager.getInstance(module).getContentEntries()[0].getSourceFolders();
        for (SourceFolder sourceFolder : sourceFolders) {
            RootFolderWrapper folderWrapper = new RootFolderWrapper(sourceFolder);

            _classSourceDirectoryCombo.addItem(folderWrapper);
            if (folderWrapper.toString().equals(newClassesSourceDirectory))
                _classSourceDirectoryCombo.setSelectedItem(folderWrapper);
        }
    }

    @Override
    public JPanel getContentPane() {
        return _contentPane;
    }

    public String getNewMixinName() {
        return _name.getText();
    }

    public boolean isReplaceExistingFiles() {
        return _replaceExistingFilesCheck.isSelected();
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
