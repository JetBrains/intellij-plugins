package tanvd.grazi.ide.ui;

import com.intellij.openapi.options.*;
import com.intellij.ui.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.*;
import tanvd.grazi.language.*;

import javax.swing.*;
import java.util.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziConfig> {
    private JPanel wholePanel;
    private CheckBoxList<String> enabledLanguages;
    private JTabbedPane tabMenu;
    private JCheckBox enableGraziSpellcheckCheckBox;
    private JComboBox nativeLanguage;


    @NotNull
    @Override
    public JComponent getComponent() {
        for (Lang lang : Lang.Companion.sortedValues()) {
            enabledLanguages.addItem(lang.getFullCode(), lang.getDisplayName(), false);
        }
        return wholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziConfig settings) {
        return !Arrays.stream(Lang.values()).allMatch(lang -> settings.getState().getEnabledLanguages().contains(lang) == enabledLanguages.isItemSelected(lang.getFullCode()))
                || !settings.getState().getNativeLanguage().equals(nativeLanguage.getSelectedItem())
                || settings.getState().getEnabledSpellcheck() != enableGraziSpellcheckCheckBox.isSelected();
    }


    @Override
    public void apply(@NotNull GraziConfig settings) {
        for (Lang lang : Lang.values()) {
            if (enabledLanguages.isItemSelected(lang.getFullCode())) {
                settings.getState().getEnabledLanguages().add(lang);
            } else {
                settings.getState().getEnabledLanguages().remove(lang);
            }
        }
        settings.getState().setNativeLanguage((Lang) nativeLanguage.getSelectedItem());
        settings.getState().setEnabledSpellcheck(enableGraziSpellcheckCheckBox.isSelected());

        GraziPlugin.Companion.init();
    }

    @Override
    public void reset(@NotNull GraziConfig settings) {
        for (Lang lang : Lang.values()) {
            enabledLanguages.setItemSelected(lang.getFullCode(), settings.getState().getEnabledLanguages().contains(lang));
        }

        for (Lang lang : settings.getState().getEnabledLanguages()) {
            nativeLanguage.addItem(lang);
            if (lang.equals(settings.getState().getNativeLanguage())) {
                nativeLanguage.setSelectedItem(lang);
            }
        }

        enableGraziSpellcheckCheckBox.setSelected(settings.getState().getEnabledSpellcheck());
    }
}
