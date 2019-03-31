package tanvd.grazi.ide.ui;

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.*;
import com.intellij.ui.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.language.*;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziApplicationSettings> {
    private JPanel wholePanel;
    private CheckBoxList<String> enabledLanguages;
    private TextFieldWithBrowseButton graziFolder;
    private JTabbedPane tabMenu;
    private JCheckBox enableGraziSpellcheckCheckBox;
    private JComboBox motherTongue;

    static private final TreeMap<String, String> allLanguageShortCodes = new TreeMap<>();

    static {
        for (Lang lang : Lang.values()) {
            allLanguageShortCodes.put(lang.getDisplayName(), lang.getShortCode());
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        for (Map.Entry<String, String> languageShortCode : allLanguageShortCodes.entrySet()) {
            enabledLanguages.addItem(languageShortCode.getValue(), languageShortCode.getKey(), false);
        }

        graziFolder.addBrowseFolderListener("", "Grazi folder", null, new FileChooserDescriptor(false, true, false, false, false, false),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

        return wholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziApplicationSettings settings) {
        return !allLanguageShortCodes.values().stream()
                .allMatch(shortCode -> settings.getState().getEnabledLanguages().contains(Lang.Companion.get(shortCode)) == enabledLanguages.isItemSelected(shortCode))
                || !settings.getState().getGraziFolder().getAbsolutePath().equals(graziFolder.getText())
                || !settings.getState().getMotherTongue().equals(motherTongue.getSelectedItem())
                || settings.getState().getEnabledSpellcheck() != enableGraziSpellcheckCheckBox.isSelected();
    }


    @Override
    public void apply(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            if (enabledLanguages.isItemSelected(shortCode)) {
                settings.getState().getEnabledLanguages().add(Lang.Companion.get(shortCode));
            } else {
                settings.getState().getEnabledLanguages().remove(Lang.Companion.get(shortCode));
            }
        }
        settings.getState().setGraziFolder(new File(graziFolder.getText()));
        settings.getState().setMotherTongue((Lang) motherTongue.getSelectedItem());
        settings.getState().setEnabledSpellcheck(enableGraziSpellcheckCheckBox.isSelected());
        settings.init();
    }

    @Override
    public void reset(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            enabledLanguages.setItemSelected(shortCode, settings.getState().getEnabledLanguages().contains(Lang.Companion.get(shortCode)));
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
