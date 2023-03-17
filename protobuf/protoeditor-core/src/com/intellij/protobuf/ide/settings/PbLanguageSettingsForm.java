/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.settings;

import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.ide.actions.PbExportSettingsAsCliCommandAction;
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry;
import com.intellij.protobuf.lang.resolve.FileResolveProvider;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.TableView;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.*;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.table.IconTableCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.regex.Pattern;

/** The protobuf language settings panel. */
public class PbLanguageSettingsForm implements ConfigurableUi<PbProjectSettings> {
  // TODO(volkman): Add a help topic or something describing the various settings, and how path
  // order matters.

  private static final Pattern LEADING_TRAILING_SLASHES = Pattern.compile("(^/+)|(/+$)");

  private final Project project;
  private PbProjectSettings originalSettings;
  private JPanel panel;
  private ListTableModel<ImportPath> importPathModel;
  private ComboBox<String> descriptorPathField;
  private JCheckBox autoConfigCheckbox;

  // A list of components that are enabled/disabled based on the auto config check box.
  private final List<JComponent> manualConfigComponents = new ArrayList<>();

  PbLanguageSettingsForm(Project project) {
    this.project = project;
    initComponent();
  }

  @Override
  public void reset(@NotNull PbProjectSettings settings) {
    originalSettings = settings;
    loadSettings(settings);
  }

  @Override
  public boolean isModified(@NotNull PbProjectSettings settings) {
    return !(getImportPathEntries().equals(settings.getImportPathEntries())
        && getDescriptorPath().equals(settings.getDescriptorPath())
        && isAutoConfigEnabled() == settings.isAutoConfigEnabled());
  }

  @Override
  public void apply(@NotNull PbProjectSettings settings) {
    applyNoNotify(settings);
    PbProjectSettings.notifyUpdated(project);
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return panel;
  }

  private void loadSettings(@NotNull PbProjectSettings settings) {
    autoConfigCheckbox.setSelected(settings.isAutoConfigEnabled());
    descriptorPathField.setSelectedItem(settings.getDescriptorPath());
    importPathModel.setItems(new ArrayList<>());
    importPathModel.addRows(ContainerUtil.map(settings.getImportPathEntries(), ImportPath::new));
  }

  private void applyNoNotify(@NotNull PbProjectSettings settings) {
    settings.setAutoConfigEnabled(isAutoConfigEnabled());
    settings.setDescriptorPath(getDescriptorPath());
    settings.setImportPathEntries(getImportPathEntries());
  }

  private void initComponent() {
    JPanel pathsPanel = buildImportPathsPanel();
    manualConfigComponents.add(pathsPanel);
    JPanel descriptorPanel = buildDescriptorPathPanel();
    manualConfigComponents.add(descriptorPanel);
    JPanel manualConfigurationPanel = new BorderLayoutPanel();
    manualConfigurationPanel.add(pathsPanel, BorderLayout.CENTER);
    manualConfigurationPanel.add(descriptorPanel, BorderLayout.SOUTH);
    autoConfigCheckbox = new JBCheckBox(PbIdeBundle.message("settings.language.autoconfig"));
    panel = new BorderLayoutPanel();
    panel.add(autoConfigCheckbox, BorderLayout.NORTH);
    panel.add(manualConfigurationPanel, BorderLayout.CENTER);

    // Component enable/disable happens in an ItemListener so that setSelected() as well as clicking
    // toggles the state.
    autoConfigCheckbox.addItemListener(
        event -> {
          boolean autoConfig = ((JCheckBox) event.getSource()).isSelected();
          manualConfigComponents.forEach(component -> component.setEnabled(!autoConfig));
        });
    // Re-configuration happens in an ActionListener which is not triggered by setSelected(). This
    // prevents the recursive sequence:
    //   loadSettings() -> setSelected(true) -> applyAutomaticConfiguration() -> loadSettings() ...
    autoConfigCheckbox.addActionListener(
        event -> {
          boolean autoConfig = ((JCheckBox) event.getSource()).isSelected();
          if (autoConfig) {
            applyAutomaticConfiguration();
          }
        });
  }

  private JPanel buildDescriptorPathPanel() {
    List<String> descriptorOptions =
        new ArrayList<>(
            ProjectSettingsConfiguratorManager.getInstance(project).getDescriptorPathSuggestions());
    descriptorPathField = new ComboBox<>(new CollectionComboBoxModel<>(descriptorOptions));
    descriptorPathField.setEditable(true);
    JTextField editorComponent = (JTextField) descriptorPathField.getEditor().getEditorComponent();
    editorComponent
        .getDocument()
        .addDocumentListener(
            new DocumentAdapter() {
              @Override
              protected void textChanged(@NotNull DocumentEvent e) {
                updateDescriptorPathColor();
              }
            });
    importPathModel.addTableModelListener(event -> updateDescriptorPathColor());
    return LabeledComponent.create(
        descriptorPathField,
        PbIdeBundle.message("settings.language.descriptor.path"),
        BorderLayout.WEST);
  }

  private JPanel buildImportPathsPanel() {
    JPanel panel = new BorderLayoutPanel();
    UIUtil.addBorder(
        panel,
        IdeBorderFactory.createTitledBorder(
            PbIdeBundle.message("settings.language.import.paths"), false));

    importPathModel = new ListTableModel<>(
        new LocationColumn(PbIdeBundle.message("location")),
        new PrefixColumn(PbIdeBundle.message("prefix"))
    );
    TableView<ImportPath> importPathTable = new TableView<>(importPathModel);
    importPathTable.setStriped(true);
    manualConfigComponents.add(importPathTable);

    // Without giving the rows some more room, the LocalPathCellEditor component is really squished.
    importPathTable.setMinRowHeight(25);

    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(importPathTable, null);
    decorator.setAddAction(
        (button) -> {
          VirtualFile selectedFile =
              FileChooser.chooseFile(getFileChooserDescriptor(null), project, null);
          if (selectedFile != null) {
            importPathModel.addRow(new ImportPath(selectedFile.getUrl()));
          }
        });
    decorator.setEditAction(
        (button) ->
            importPathTable.editCellAt(
                importPathTable.getSelectedRow(), importPathTable.getSelectedColumn()));
    decorator.addExtraAction(new PbExportSettingsAsCliCommandAction());

    panel.add(decorator.createPanel(), BorderLayout.CENTER);
    return panel;
  }

  private List<ImportPathEntry> getImportPathEntries() {
    return ContainerUtil.map(importPathModel.getItems(), ImportPath::toEntry);
  }

  private String getDescriptorPath() {
    return (String) descriptorPathField.getEditor().getItem();
  }

  private boolean isAutoConfigEnabled() {
    return autoConfigCheckbox.isSelected();
  }

  private boolean isDescriptorPathValid() {
    PbProjectSettings tempSettings = new PbProjectSettings(project);
    applyNoNotify(tempSettings);
    FileResolveProvider provider = new SettingsFileResolveProvider(tempSettings);
    return provider.getDescriptorFile(project) != null;
  }

  private void updateDescriptorPathColor() {
    if (isDescriptorPathValid()) {
      descriptorPathField
          .getEditor()
          .getEditorComponent()
          .setForeground(UIUtil.getTextFieldForeground());
    } else {
      descriptorPathField.getEditor().getEditorComponent().setForeground(JBColor.RED);
    }
  }

  private void applyAutomaticConfiguration() {
    PbProjectSettings currentSettings = originalSettings.copy();
    applyNoNotify(currentSettings);
    PbProjectSettings newSettings =
      ProjectSettingsConfiguratorManager.getInstance(project).configure(currentSettings.copy());
    loadSettings(newSettings != null ? newSettings : currentSettings);
  }

  /**
   * Return a descriptor that can select folders and jar file contents.
   */
  private static FileChooserDescriptor getFileChooserDescriptor(@NlsContexts.DialogTitle String title) {
    FileChooserDescriptor descriptor =
      new FileChooserDescriptor(
        false, // chooseFiles
        true, // chooseFolders
        true, // chooseJars
        false, // chooseJarsAsFiles
        true, // chooseJarContents
        false) // chooseMultiple
        .withShowFileSystemRoots(true)
        .withShowHiddenFiles(true);
    if (title != null) {
      descriptor.setTitle(title);
    }
    return descriptor;
  }

  private class LocationColumn extends ColumnInfo<ImportPath, String> {

    LocationColumn(@NlsContexts.ColumnName String name) {
      super(name);
    }

    @Nullable
    @Override
    public String valueOf(ImportPath o) {
      return o.location;
    }

    @Override
    public void setValue(ImportPath o, String value) {
      o.location = value;
    }

    @Override
    public boolean isCellEditable(final ImportPath path) {
      return true;
    }

    @Override
    public TableCellRenderer getRenderer(final ImportPath path) {
      return new IconTableCellRenderer<String>() {
        @Nullable
        @Override
        protected Icon getIcon(@NotNull String value, JTable table, int row) {
          VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(value);
          if (file != null && file.isDirectory()) {
            return PlatformIcons.FOLDER_ICON;
          }
          return General.Error;
        }

        @Override
        protected void setValue(Object value) {
          String url = (String) value;
          VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
          if (file == null || !file.isDirectory()) {
            setForeground(JBColor.RED);
          }
          setText(file != null ? file.getPresentableUrl() : url);
        }
      };
    }

    @Override
    public TableCellEditor getEditor(final ImportPath path) {
      return new LocationCellEditor(project);
    }
  }

  private static class PrefixColumn extends ColumnInfo<ImportPath, String> {
    PrefixColumn(@NlsContexts.ColumnName String name) {
      super(name);
    }

    @Nullable
    @Override
    public String valueOf(ImportPath o) {
      return o.prefix;
    }

    @Override
    public void setValue(ImportPath o, String value) {
      // Remove leading and trailing slashes, duplicate slashes, and leading and trailing spaces.
      o.prefix = LEADING_TRAILING_SLASHES.matcher(value.trim()).replaceAll("");
    }

    @Override
    public boolean isCellEditable(ImportPath item) {
      return true;
    }

    @Override
    public TableCellEditor getEditor(final ImportPath path) {
      DefaultCellEditor editor = new DefaultCellEditor(new JBTextField(path.prefix));
      editor.setClickCountToStart(2);
      return editor;
    }
  }

  private static class ImportPath {
    @NlsSafe
    String location;
    @NlsSafe
    String prefix;

    ImportPath(ImportPathEntry entry) {
      this.location = entry.getLocation();
      this.prefix = entry.getPrefix();
    }

    ImportPath(String location) {
      this.location = location;
      this.prefix = null;
    }

    ImportPathEntry toEntry() {
      return new ImportPathEntry(location, prefix);
    }
  }

  private static class LocationCellEditor extends AbstractTableCellEditor {
    @NlsContexts.DialogTitle
    private final String title;
    private final Project project;
    private CellEditorComponentWithBrowseButton<JTextField> component = null;

    LocationCellEditor(@Nullable @NlsContexts.DialogTitle String title, @Nullable Project project) {
      this.title = title;
      this.project = project;
    }

    LocationCellEditor(@Nullable Project project) {
      this(null, project);
    }

    @Override
    public Object getCellEditorValue() {
      return component != null ? component.getChildComponent().getText() : "";
    }

    @Override
    public Component getTableCellEditorComponent(
        final JTable table, Object value, boolean isSelected, final int row, int column) {
      component =
          new CellEditorComponentWithBrowseButton<>(
              new TextFieldWithBrowseButton(createActionListener(table)), this);
      component.getChildComponent().setText((String) value);
      component.setFocusable(false);
      return component;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
      return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 2;
    }

    private ActionListener createActionListener(final JTable table) {
      return e -> {
        String initialValue = (String) getCellEditorValue();
        VirtualFile initialFile =
            !StringUtil.isEmpty(initialValue)
                ? VirtualFileManager.getInstance().findFileByUrl(initialValue)
                : null;
        FileChooser.chooseFile(
            getFileChooserDescriptor(title),
            project,
            table,
            initialFile,
            file -> component.getChildComponent().setText(file.getUrl()));
      };
    }
  }
}
