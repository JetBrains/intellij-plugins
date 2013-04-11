package com.dmarcotte.handlebars.pages;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HbConfigurationPage implements SearchableConfigurable {
  private JCheckBox myAutoGenerateClosingTagCheckBox;
  private JPanel myWholePanel;
  private JCheckBox myFormattingCheckBox;
  private JComboBox myCommenterLanguage;

  @NotNull
  @Override
  public String getId() {
    return "editor.preferences.handlebarsOptions";
  }

  @Override
  public Runnable enableSearch(String option) {
    return null;
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
    return null;
  }

  @Override
  public JComponent createComponent() {
    return myWholePanel;
  }

  @Override
  public boolean isModified() {
    return myAutoGenerateClosingTagCheckBox.isSelected() != HbConfig.isAutoGenerateCloseTagEnabled()
           || myFormattingCheckBox.isSelected() != HbConfig.isFormattingEnabled()
           || !((Language)myCommenterLanguage.getSelectedItem()).getID().equals(HbConfig.getCommenterLanguage().getID());
  }

  @Override
  public void apply() throws ConfigurationException {
    HbConfig.setAutoGenerateCloseTagEnabled(myAutoGenerateClosingTagCheckBox.isSelected());
    HbConfig.setFormattingEnabled(myFormattingCheckBox.isSelected());
    HbConfig.setCommenterLanguage((Language)myCommenterLanguage.getSelectedItem());
  }

  @Override
  public void reset() {
    myAutoGenerateClosingTagCheckBox.setSelected(HbConfig.isAutoGenerateCloseTagEnabled());
    myFormattingCheckBox.setSelected(HbConfig.isFormattingEnabled());
    resetCommentLanguageCombo(HbConfig.getCommenterLanguage());
  }

  private void resetCommentLanguageCombo(Language commentLanguage) {
    final DefaultComboBoxModel model = (DefaultComboBoxModel)myCommenterLanguage.getModel();
    final List<Language> languages = TemplateDataLanguageMappings.getTemplateableLanguages();

    // add using the native Handlebars commenter as an option
    languages.add(HbLanguage.INSTANCE);

    Collections.sort(languages, new Comparator<Language>() {
      @Override
      public int compare(final Language o1, final Language o2) {
        return o1.getID().compareTo(o2.getID());
      }
    });
    for (Language language : languages) {
      model.addElement(language);
    }

    // When com.intellij.openapi.fileTypes.impl.FileTypePatternDialog#FileTypePatternDialog updates to fix this warning, we make the same update here
    //noinspection GtkPreferredJComboBoxRenderer,deprecation
    myCommenterLanguage.setRenderer(new ListCellRendererWrapper(myCommenterLanguage.getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        setText(value == null ? "" : ((Language)value).getDisplayName());
        if (value != null) {
          final FileType type = ((Language)value).getAssociatedFileType();
          if (type != null) {
            setIcon(type.getIcon());
          }
        }
      }
    });
    myCommenterLanguage.setSelectedItem(commentLanguage);
  }

  @Override
  public void disposeUIResources() {
  }
}
