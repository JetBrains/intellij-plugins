// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.pages;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;

public class HbConfigurationPage implements SearchableConfigurable {
  private JCheckBox myAutoGenerateClosingTagCheckBox;
  private JPanel myWholePanel;
  private JCheckBox myFormattingCheckBox;
  private JComboBox<Language> myCommenterLanguage;
  private JCheckBox myAutocompleteMustaches;
  private JCheckBox htmlAsHb;
  private final Project myProject;

  public HbConfigurationPage(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public String getId() {
    return "editor.preferences.handlebarsOptions";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return HbBundle.message("hb.pages.options.title");
  }

  @SuppressWarnings({"UnusedDeclaration", "SameReturnValue"}) // this  can probably be deleted eventually; IDEA 11 expects it to be here
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.ide.settings.handlebars.mustache";
  }

  @Override
  public JComponent createComponent() {
    return myWholePanel;
  }

  @Override
  public boolean isModified() {
    return myAutoGenerateClosingTagCheckBox.isSelected() != HbConfig.isAutoGenerateCloseTagEnabled()
           || myAutocompleteMustaches.isSelected() != HbConfig.isAutocompleteMustachesEnabled()
           || myFormattingCheckBox.isSelected() != HbConfig.isFormattingEnabled()
           || htmlAsHb.isSelected() != HbConfig.shouldOpenHtmlAsHandlebars(myProject)
           || !HbConfig.getCommenterLanguage().getID().equals(getSelectedLanguageId());
  }

  private String getSelectedLanguageId() {
    final Object item = myCommenterLanguage.getSelectedItem();
    return item == null ? null : ((Language)item).getID();
  }

  @Override
  public void apply() {
    HbConfig.setAutoGenerateCloseTagEnabled(myAutoGenerateClosingTagCheckBox.isSelected());
    HbConfig.setAutocompleteMustachesEnabled(myAutocompleteMustaches.isSelected());
    HbConfig.setFormattingEnabled(myFormattingCheckBox.isSelected());
    HbConfig.setCommenterLanguage((Language)myCommenterLanguage.getSelectedItem());

    if (HbConfig.setShouldOpenHtmlAsHandlebars(htmlAsHb.isSelected(), myProject)) {
      ApplicationManager.getApplication().runWriteAction(() -> FileTypeManagerEx.getInstanceEx().makeFileTypesChange("hb config updated",
                                                                                                                     EmptyRunnable.getInstance()));
    }
  }

  @Override
  public void reset() {
    myAutoGenerateClosingTagCheckBox.setSelected(HbConfig.isAutoGenerateCloseTagEnabled());
    myAutocompleteMustaches.setSelected(HbConfig.isAutocompleteMustachesEnabled());
    myFormattingCheckBox.setSelected(HbConfig.isFormattingEnabled());
    htmlAsHb.setSelected(HbConfig.shouldOpenHtmlAsHandlebars(myProject));
    resetCommentLanguageCombo(HbConfig.getCommenterLanguage());
  }

  private void resetCommentLanguageCombo(@NotNull Language commentLanguage) {
    final DefaultComboBoxModel<Language> model = (DefaultComboBoxModel<Language>)myCommenterLanguage.getModel();
    final List<Language> languages = TemplateDataLanguageMappings.getTemplateableLanguages();

    // add using the native Handlebars commenter as an option
    languages.add(HbLanguage.INSTANCE);

    languages.sort(Comparator.comparing(Language::getID));
    for (Language language : languages) {
      model.addElement(language);
    }

    myCommenterLanguage.setRenderer(SimpleListCellRenderer.create((label, value, index) -> {
      label.setText(value == null ? "" : value.getDisplayName());
      if (value != null) {
        FileType type = value.getAssociatedFileType();
        if (type != null) {
          label.setIcon(type.getIcon());
        }
      }
    }));
    myCommenterLanguage.setSelectedItem(commentLanguage);
  }
}
