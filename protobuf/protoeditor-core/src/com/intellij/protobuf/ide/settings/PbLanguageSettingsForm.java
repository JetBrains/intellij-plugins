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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.progress.*;
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
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.scale.JBUIScale;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.intellij.protobuf.ide.settings.PbSettingsUiUtilsKt.findIndexToInsertGroup;

/** The protobuf language settings panel. */
public class PbLanguageSettingsForm implements ConfigurableUi<PbProjectSettings> {
  // TODO(volkman): Add a help topic or something describing the various settings, and how path
  // order matters.

  private static final Pattern LEADING_TRAILING_SLASHES = Pattern.compile("(^/+)|(/+$)");

  private final Project project;
  private JPanel panel;
  private ListTableModel<ImportPath> importPathModel;
  private ComboBox<String> descriptorPathField;
  private JCheckBox isIncludeStandardProtoDirectoriesCheckbox;
  private JCheckBox isIncludeContentRootsCheckbox;
  private JCheckBox autoConfigCheckbox;
  private JCheckBox isIndexBasedResolveEnabledCheckbox;
  private JCheckBox isIncludeWellKnownProtosCheckbox;

  PbLanguageSettingsForm(Project project) {
    this.project = project;
    initComponent();
  }

  @Override
  public void reset(@NotNull PbProjectSettings settings) {
    loadSettings(settings);
  }

  @Override
  public boolean isModified(@NotNull PbProjectSettings settings) {
    return !(getDescriptorPath().equals(settings.getDescriptorPath())
             && isAutoConfigEnabled() == settings.isThirdPartyConfigurationEnabled()
             && isIncludeStandardProtoDirectories() == settings.isIncludeProtoDirectories()
             && isIncludeProjectContentRoots() == settings.isIncludeContentRoots()
             && isIndexBasedResolveEnabled() == settings.isIndexBasedResolveEnabled()
             && isIncludeWellKnownProtos() == settings.isIncludeWellKnownProtos()
             && getImportPathEntries().equals(settings.getImportPathEntries())
    );
  }

  @Override
  public void apply(@NotNull PbProjectSettings settings) {
    applyNoNotify(settings);
    PbProjectSettings.notifyUpdated(project);
  }

  @Override
  public @NotNull JComponent getComponent() {
    return panel;
  }

  private void loadSettings(@NotNull PbProjectSettings settings) {
    autoConfigCheckbox.setSelected(settings.isThirdPartyConfigurationEnabled());
    isIncludeStandardProtoDirectoriesCheckbox.setSelected(settings.isIncludeProtoDirectories());
    isIncludeContentRootsCheckbox.setSelected(settings.isIncludeContentRoots());
    isIndexBasedResolveEnabledCheckbox.setSelected(settings.isIndexBasedResolveEnabled());
    isIncludeWellKnownProtosCheckbox.setSelected(settings.isIncludeWellKnownProtos());
    descriptorPathField.setSelectedItem(settings.getDescriptorPath());
    importPathModel.setItems(new CopyOnWriteArrayList<>());
    importPathModel.addRows(ContainerUtil.map(settings.getImportPathEntries(), ImportPath::new));
  }

  private void applyNoNotify(@NotNull PbProjectSettings settings) {
    settings.setThirdPartyConfigurationEnabled(isAutoConfigEnabled());
    settings.setIncludeContentRoots(isIncludeProjectContentRoots());
    settings.setIncludeProtoDirectories(isIncludeStandardProtoDirectories());
    settings.setIndexBasedResolveEnabled(isIndexBasedResolveEnabled());
    settings.setIncludeWellKnownProtos(isIncludeWellKnownProtos());
    settings.setDescriptorPath(getDescriptorPath());
    settings.setImportPathEntries(getImportPathEntries());
  }

  private void initComponent() {
    JPanel pathsPanel = buildImportPathsPanel();
    JPanel descriptorPanel = buildDescriptorPathPanel();
    JPanel manualConfigurationPanel = new BorderLayoutPanel();
    manualConfigurationPanel.add(pathsPanel, BorderLayout.CENTER);
    manualConfigurationPanel.add(descriptorPanel, BorderLayout.SOUTH);
    autoConfigCheckbox = new JBCheckBox(PbIdeBundle.message("settings.language.autoconfig"));
    isIncludeStandardProtoDirectoriesCheckbox = new JBCheckBox(PbIdeBundle.message("settings.language.include.std.proto.dirs"));
    isIncludeContentRootsCheckbox = new JBCheckBox(PbIdeBundle.message("settings.language.include.project.content.roots"));
    isIndexBasedResolveEnabledCheckbox = new JBCheckBox(PbIdeBundle.message("settings.language.index.based.resolve.enabled"));
    isIncludeWellKnownProtosCheckbox = new JBCheckBox(PbIdeBundle.message("settings.language.well.known.protos.enabled"));
    JPanel autoDetectionPanel = new JPanel(new VerticalLayout(0));
    UIUtil.addBorder(
      autoDetectionPanel,
      IdeBorderFactory.createTitledBorder(PbIdeBundle.message("settings.language.autoconfiguration.section.title"), false));

    autoDetectionPanel.add(autoConfigCheckbox, VerticalLayout.CENTER);
    autoDetectionPanel.add(isIncludeStandardProtoDirectoriesCheckbox, VerticalLayout.CENTER);
    autoDetectionPanel.add(isIncludeContentRootsCheckbox, VerticalLayout.CENTER);
    autoDetectionPanel.add(isIndexBasedResolveEnabledCheckbox, VerticalLayout.CENTER);
    autoDetectionPanel.add(isIncludeWellKnownProtosCheckbox, VerticalLayout.CENTER);

    panel = new BorderLayoutPanel();
    panel.add(autoDetectionPanel, BorderLayout.NORTH);
    panel.add(manualConfigurationPanel, BorderLayout.CENTER);

    autoConfigCheckbox.addActionListener(
      groupItemPresentationUpdater(autoConfiguredGroup, PbImportPathsConfiguration::thirdPartyImportPaths));
    isIncludeStandardProtoDirectoriesCheckbox.addActionListener(
      groupItemPresentationUpdater(protoDirectoriesGroup, PbImportPathsConfiguration::standardProtoDirectories));
    isIncludeContentRootsCheckbox.addActionListener(
      groupItemPresentationUpdater(contentRootsGroup, PbImportPathsConfiguration::projectContentRoots));
    isIndexBasedResolveEnabledCheckbox.addActionListener(
      groupItemPresentationUpdater(filesFromIndexesGroup, PbImportPathsConfiguration::computeImportPathsForAllImportStatements)
    );
    isIncludeWellKnownProtosCheckbox.addActionListener(
      groupItemPresentationUpdater(bundledGoogleStdLibGroup, PbImportPathsConfiguration::computeWellKnownProtos)
    );
    computePreciseAutoConfiguredEntriesCount();
  }

  private static final ImportPathGroup autoConfiguredGroup =
    ImportPathGroup.create((count) -> PbIdeBundle.message("settings.virtual.group.contributed.name", count), 0);
  private static final ImportPathGroup protoDirectoriesGroup =
    ImportPathGroup.create((count) -> PbIdeBundle.message("settings.virtual.group.proto.directory.name", count), 1);
  private static final ImportPathGroup contentRootsGroup =
    ImportPathGroup.create((count) -> PbIdeBundle.message("settings.virtual.group.content.root.name", count), 2);
  private static final ImportPathGroup bundledGoogleStdLibGroup =
    ImportPathGroup.create((count) -> PbIdeBundle.message("settings.virtual.group.std.google.proto.name"), 3);

  private static final ImportPathGroup filesFromIndexesGroup =
    ImportPathGroup.create((count) -> PbIdeBundle.message("settings.virtual.group.from.indexes", count), 4);

  private static final ImportPathGroup loadingStateGroup =
    ImportPathGroup.create((count) -> PbIdeBundle.message("settings.virtual.group.loading"), 100);

  private @NotNull ActionListener groupItemPresentationUpdater(ImportPathGroup group,
                                                               Function<Project, Collection<?>> heavyImportsFetcher) {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        Object changeSource = event.getSource();
        if (changeSource instanceof JCheckBox) {
          computePresentationAndUpdateModel(((JCheckBox)changeSource), group, heavyImportsFetcher);
        }
      }
    };
  }

  private void computePresentationAndUpdateModel(JCheckBox checkBox,
                                                 ImportPathGroup group,
                                                 Function<Project, Collection<?>> heavyImportsFetcher) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      () -> {
        var importsCount = heavyImportsFetcher == null ? 0 : ReadAction.compute(() -> heavyImportsFetcher.apply(project).size());
        SwingUtilities.invokeLater(() -> {
          processVirtualGroupItemPresentation(checkBox, group.copyWithPreciseCount(importsCount));
        });
      },
      PbIdeBundle.message("settings.compute.import.paths.modal.progress.title"),
      true,
      project);
  }

  private void computePreciseAutoConfiguredEntriesCount() {
    SwingUtilities.invokeLater(() -> addVirtualGroup(loadingStateGroup));
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(
      new Task.Backgroundable(project, PbIdeBundle.message("settings.compute.import.paths.bg.progress.title"), false,
                              PerformInBackgroundOption.ALWAYS_BACKGROUND) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          ReadAction.run(() -> {
            int contentRootsSize = PbImportPathsConfiguration.projectContentRoots(project).size();
            int protoDirsSize = PbImportPathsConfiguration.standardProtoDirectories(project).size();
            int contributedPathsSize = PbImportPathsConfiguration.thirdPartyImportPaths(project).size();
            int protoDirectoriesFromIndexSize = PbImportPathsConfiguration.computeImportPathsForAllImportStatements(project).size();
            SwingUtilities.invokeLater(() -> {
              removeVirtualGroup(loadingStateGroup);
              processVirtualGroupItemPresentation(autoConfigCheckbox, autoConfiguredGroup.copyWithPreciseCount(contributedPathsSize));
              processVirtualGroupItemPresentation(isIncludeStandardProtoDirectoriesCheckbox,
                                                  protoDirectoriesGroup.copyWithPreciseCount(protoDirsSize));
              processVirtualGroupItemPresentation(isIncludeContentRootsCheckbox,
                                                  contentRootsGroup.copyWithPreciseCount(contentRootsSize));
              processVirtualGroupItemPresentation(isIndexBasedResolveEnabledCheckbox,
                                                  filesFromIndexesGroup.copyWithPreciseCount(protoDirectoriesFromIndexSize));
              processVirtualGroupItemPresentation(isIncludeWellKnownProtosCheckbox,
                                                  bundledGoogleStdLibGroup);
            });
          });
        }
      },
      EmptyProgressIndicator.notNullize(ProgressIndicatorProvider.getGlobalProgressIndicator())
    );
  }

  private void processVirtualGroupItemPresentation(JCheckBox checkBox, ImportPathGroup group) {
    boolean isGroupPresent = importPathModel.indexOf(group) != -1;
    if (checkBox.isSelected()) {
      if (!isGroupPresent) {
        addVirtualGroup(group);
      }
      else {
        removeVirtualGroup(group);
        addVirtualGroup(group);
      }
    }
    else if (isGroupPresent) {
      removeVirtualGroup(group);
    }
  }

  private void addVirtualGroup(ImportPathGroup group) {
    importPathModel.insertRow(findIndexToInsertGroup(importPathModel, group), group);
  }

  private void removeVirtualGroup(ImportPathGroup group) {
    int index = importPathModel.indexOf(group);
    if (index == -1) return;
    importPathModel.removeRow(index);
  }

  private JPanel buildDescriptorPathPanel() {
    List<String> descriptorOptions =
      new ArrayList<>(
        PbImportPathsConfiguration.getDescriptorPathSuggestions(project));
    descriptorPathField = new ComboBox<>(new CollectionComboBoxModel<>(descriptorOptions));
    descriptorPathField.setEditable(true);
    JTextField editorComponent = (JTextField)descriptorPathField.getEditor().getEditorComponent();
    editorComponent
      .getDocument()
      .addDocumentListener(
        new DocumentAdapter() {
          @Override
          protected void textChanged(@NotNull DocumentEvent e) {
            scheduleDescriptorValidation();
          }
        });
    importPathModel.addTableModelListener(event -> scheduleDescriptorValidation());
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
      new ColumnInfo[]{
        new LocationColumn(PbIdeBundle.message("location")),
        new PrefixColumn(PbIdeBundle.message("prefix"))
      },
      new CopyOnWriteArrayList<>()
    );
    TableView<ImportPath> importPathTable = new TableView<>(importPathModel);
    importPathTable.setStriped(true);

    // Without giving the rows some more room, the LocalPathCellEditor component is really squished.
    importPathTable.setMinRowHeight(25);

    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(importPathTable, null);

    decorator.setRemoveActionUpdater(allowOnlyEffectivePathEditing(importPathTable));
    decorator.setEditActionUpdater(allowOnlyEffectivePathEditing(importPathTable));
    decorator.setMoveDownActionUpdater(allowOnlyEffectivePathEditing(importPathTable));
    decorator.setMoveUpActionUpdater(allowOnlyEffectivePathEditing(importPathTable));

    decorator.setAddAction(
      (button) -> {
        VirtualFile selectedFile =
          FileChooser.chooseFile(getFileChooserDescriptor(null), project, null);
        if (selectedFile != null) {
          importPathModel.insertRow(0, new ImportPath(selectedFile.getUrl()));
        }
      });
    decorator.setEditAction((button) -> importPathTable.editCellAt(importPathTable.getSelectedRow(), importPathTable.getSelectedColumn()));
    decorator.addExtraAction(new PbExportSettingsAsCliCommandAction());

    panel.add(decorator.createPanel(), BorderLayout.CENTER);
    return panel;
  }

  private static @NotNull AnActionButtonUpdater allowOnlyEffectivePathEditing(TableView<ImportPath> importPathTable) {
    return e -> {
      int selectedRow = importPathTable.getSelectedRow();
      if (selectedRow == -1) return false;
      return !(importPathTable.getListTableModel().getItem(selectedRow) instanceof ImportPathGroup);
    };
  }

  private List<ImportPathEntry> getImportPathEntries() {
    return importPathModel.getItems().stream()
      .filter(PbLanguageSettingsForm::isEffectivePath)
      .map(ImportPath::toEntry)
      .toList();
  }

  private String getDescriptorPath() {
    return (String)descriptorPathField.getEditor().getItem();
  }

  private boolean isAutoConfigEnabled() {
    return autoConfigCheckbox.isSelected();
  }

  private boolean isIncludeStandardProtoDirectories() {
    return isIncludeStandardProtoDirectoriesCheckbox.isSelected();
  }

  private boolean isIncludeProjectContentRoots() {
    return isIncludeContentRootsCheckbox.isSelected();
  }

  private boolean isIndexBasedResolveEnabled() {
    return isIndexBasedResolveEnabledCheckbox.isSelected();
  }

  private boolean isIncludeWellKnownProtos() {
    return isIncludeWellKnownProtosCheckbox.isSelected();
  }

  private boolean isDescriptorPathValid() {
    PbProjectSettings tempSettings = new PbProjectSettings(project);
    applyNoNotify(tempSettings);
    return ReadAction.compute(
      () -> SettingsFileResolveProvider.findFileWithSettings(tempSettings.getDescriptorPath(), project, tempSettings) != null);
  }

  private void scheduleDescriptorValidation() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (isDescriptorPathValid()) {
        SwingUtilities.invokeLater(() -> {
          descriptorPathField
            .getEditor()
            .getEditorComponent()
            .setForeground(UIUtil.getTextFieldForeground());
        });
      }
      else {
        SwingUtilities.invokeLater(() -> {
          descriptorPathField.getEditor().getEditorComponent().setForeground(JBColor.RED);
        });
      }
    });
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

    @Override
    public @Nullable String valueOf(ImportPath o) {
      return o.location;
    }

    @Override
    public void setValue(ImportPath o, String value) {
      o.location = value;
    }

    @Override
    public boolean isCellEditable(final ImportPath item) {
      return isEffectivePath(item);
    }

    @Override
    public TableCellRenderer getRenderer(final ImportPath path) {
      return new IconTableCellRenderer<String>() {
        @Override
        protected @NotNull Icon getIcon(@NotNull String value, JTable table, int row) {
          if (path instanceof ImportPathGroup) {
            return General.ShowInfos;
          }
          VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(value);
          if (file != null && file.isDirectory()) {
            return PlatformIcons.FOLDER_ICON;
          }
          return General.Error;
        }

        @Override
        protected void setValue(Object value) {
          String url = (String)value;
          VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
          if (!(path instanceof ImportPathGroup) && (file == null || !file.isDirectory())) {
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

    @Override
    public @Nullable String valueOf(ImportPath o) {
      return o.prefix;
    }

    @Override
    public void setValue(ImportPath o, String value) {
      // Remove leading and trailing slashes, duplicate slashes, and leading and trailing spaces.
      o.prefix = LEADING_TRAILING_SLASHES.matcher(value.trim()).replaceAll("");
    }

    @Override
    public boolean isCellEditable(ImportPath item) {
      return isEffectivePath(item);
    }

    @Override
    public TableCellEditor getEditor(final ImportPath path) {
      DefaultCellEditor editor = new DefaultCellEditor(new JBTextField(path.prefix));
      editor.setClickCountToStart(2);
      return editor;
    }

    @Override
    public int getWidth(JTable table) {
      return JBUIScale.scale(200);
    }
  }

  static class ImportPath {
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

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ImportPath path)) return false;
      return Objects.equals(location, path.location) && Objects.equals(prefix, path.prefix);
    }

    @Override
    public int hashCode() {
      return Objects.hash(location, prefix);
    }

    ImportPathEntry toEntry() {
      return new ImportPathEntry(location, prefix);
    }
  }

  static class ImportPathGroup extends ImportPath {

    ImportPathGroup(Function<Integer, String> lazyMessage, String emptyMessage, int order) {
      super(emptyMessage);
      this.lazyMessage = lazyMessage;
      myOrder = order;
    }

    ImportPathGroup copyWithPreciseCount(int groupItemsCount) {
      var effectiveMessage = lazyMessage.apply(groupItemsCount);
      return new ImportPathGroup(lazyMessage, effectiveMessage, this.myOrder);
    }

    static ImportPathGroup create(Function<Integer, String> lazyMessage, int order) {
      return new ImportPathGroup(lazyMessage, lazyMessage.apply(0), order);
    }

    private final int myOrder;
    private final Function<Integer, String> lazyMessage;

    int getOrder() {
      return myOrder;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ImportPathGroup group)) return false;
      return myOrder == group.myOrder;
    }

    @Override
    public int hashCode() {
      return Objects.hash(myOrder);
    }
  }

  private static class LocationCellEditor extends AbstractTableCellEditor {
    private final @NlsContexts.DialogTitle String title;
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
      component.getChildComponent().setText((String)value);
      component.setFocusable(false);
      return component;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
      return !(e instanceof MouseEvent) || ((MouseEvent)e).getClickCount() >= 2;
    }

    private ActionListener createActionListener(final JTable table) {
      return e -> {
        String initialValue = (String)getCellEditorValue();
        VirtualFile initialFile = !StringUtil.isEmpty(initialValue) ? VirtualFileManager.getInstance().findFileByUrl(initialValue) : null;
        FileChooser.chooseFile(getFileChooserDescriptor(title), project, table, initialFile,
                               file -> component.getChildComponent().setText(file.getUrl()));
      };
    }
  }

  private static boolean isEffectivePath(ImportPath item) {
    return !(item instanceof ImportPathGroup);
  }
}
