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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.AbstractTableCellEditor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class FileSelectorTableCellEditor extends AbstractTableCellEditor {

    private TextFieldWithBrowseButton editor;

    public FileSelectorTableCellEditor(final Project project, final Module module) {
        editor = new TextFieldWithBrowseButton(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, true, false, true);
                descriptor.setTitle("Choose source file or folder");
                FileChooserDialog fileChooserDialog =
                        FileChooserFactory.getInstance().createFileChooser(descriptor, project);
                VirtualFile rootFolder = null;
                VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
                if (contentRoots.length > 0) {
                    rootFolder = contentRoots[0];
                } else if (project.getBaseDir() != null) {
                    rootFolder = project.getBaseDir();
                }
                VirtualFile[] files = fileChooserDialog.choose(rootFolder, project);
                if (files.length > 0) {
                    editor.setText(files[0].getPath());
                }
            }
        });

    }


    public Object getCellEditorValue() {
        return editor.getText();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Component c = renderer.getTableCellRendererComponent(table, value,
                isSelected, true, row, column);
        if (c != null) {
            if (c instanceof JComponent) {
                editor.getTextField().setBorder(((JComponent) c).getBorder());
            }
        }
        editor.setText(value.toString());
        return editor;
    }
}
