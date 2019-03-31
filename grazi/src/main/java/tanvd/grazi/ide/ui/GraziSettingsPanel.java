package tanvd.grazi.ide.ui;

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.*;
import com.intellij.ui.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.*;
import tanvd.grazi.language.*;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziConfig> {
    private JPanel wholePanel;
    private CheckBoxList<String> enabledLanguages;
    private TextFieldWithBrowseButton graziFolder;
    private JTabbedPane tabMenu;
    private JCheckBox enableGraziSpellcheckCheckBox;
    private JComboBox motherTongue;


    @NotNull
    @Override
    public JComponent getComponent() {
        for (Lang lang : Lang.Companion.sortedValues()) {
            enabledLanguages.addItem(lang.getShortCode(), lang.getDisplayName(), false);
        }

        graziFolder.addBrowseFolderListener("", "Grazi folder", null, new FileChooserDescriptor(false, true, false, false, false, false),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

        return wholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziConfig settings) {
        return !Arrays.stream(Lang.values()).allMatch(lang -> settings.getState().getEnabledLanguages().contains(lang) == enabledLanguages.isItemSelected(lang.getShortCode()))
                || !settings.getState().getGraziFolder().getAbsolutePath().equals(graziFolder.getText())
                || !settings.getState().getMotherTongue().equals(motherTongue.getSelectedItem())
                || settings.getState().getEnabledSpellcheck() != enableGraziSpellcheckCheckBox.isSelected();
    }


    @Override
    public void apply(@NotNull GraziConfig settings) {
        for (Lang lang : Lang.values()) {
            if (enabledLanguages.isItemSelected(lang.getShortCode())) {
                settings.getState().getEnabledLanguages().add(lang);
            } else {
                settings.getState().getEnabledLanguages().remove(lang);
            }
        }
        settings.getState().setGraziFolder(new File(graziFolder.getText()));
        settings.getState().setMotherTongue((Lang) motherTongue.getSelectedItem());
        settings.getState().setEnabledSpellcheck(enableGraziSpellcheckCheckBox.isSelected());

        GraziPlugin.Companion.init();
    }

    @Override
    public void reset(@NotNull GraziConfig settings) {
        for (Lang lang : Lang.values()) {
            enabledLanguages.setItemSelected(lang.getShortCode(), settings.getState().getEnabledLanguages().contains(lang));
        }

        graziFolder.setText(settings.getState().getGraziFolder().getAbsolutePath());

        for (Lang lang : settings.getState().getEnabledLanguages()) {
            motherTongue.addItem(lang);
            if (lang.equals(settings.getState().getMotherTongue())) {
                motherTongue.setSelectedItem(lang);
            }
        }

        enableGraziSpellcheckCheckBox.setSelected(settings.getState().getEnabledSpellcheck());
    }
}
