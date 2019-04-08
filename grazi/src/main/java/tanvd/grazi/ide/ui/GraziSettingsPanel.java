package tanvd.grazi.ide.ui;

import com.intellij.openapi.options.*;
import com.intellij.ui.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.*;
import tanvd.grazi.language.*;

import javax.swing.*;
import java.util.*;

@SuppressWarnings("unused")
public class GraziSettingsPanel implements ConfigurableUi<GraziConfig> {
    private JPanel wholePanel;
    private CheckBoxList<String> enabledLanguages;
    private JTabbedPane tabMenu;
    private JCheckBox enableGraziSpellcheckCheckBox;
    private JComboBox<Lang> nativeLanguage;


    @NotNull
    @Override
    public JComponent getComponent() {
        for (Lang lang : Lang.Companion.getSortedValues()) {
            enabledLanguages.addItem(lang.name(), lang.getDisplayName(), false);
            nativeLanguage.addItem(lang);
        }
        return wholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziConfig settings) {
        boolean sameLanguagesEnabled = Arrays.stream(Lang.values()).allMatch(lang -> settings.getState().getEnabledLanguages().contains(lang) == enabledLanguages.isItemSelected(lang.name()));
        boolean sameNativeLanguage = settings.getState().getNativeLanguage().equals(nativeLanguage.getSelectedItem());
        boolean sameSpellCheck = settings.getState().getEnabledSpellcheck() == enableGraziSpellcheckCheckBox.isSelected();
        return !sameLanguagesEnabled || !sameNativeLanguage || !sameSpellCheck;
    }


    @Override
    public void apply(@NotNull GraziConfig settings) {
        for (Lang lang : Lang.values()) {
            if (enabledLanguages.isItemSelected(lang.name())) {
                settings.getState().getEnabledLanguages().add(lang);
            } else {
                settings.getState().getEnabledLanguages().remove(lang);
            }
        }
        if (nativeLanguage.getSelectedItem() != null) {
            settings.getState().setNativeLanguage((Lang) nativeLanguage.getSelectedItem());
        }

        settings.getState().setEnabledSpellcheck(enableGraziSpellcheckCheckBox.isSelected());

        GraziPlugin.Companion.reinit();
    }

    @Override
    public void reset(@NotNull GraziConfig settings) {
        for (Lang lang : Lang.values()) {
            enabledLanguages.setItemSelected(lang.name(), settings.getState().getEnabledLanguages().contains(lang));
        }

        for (Lang lang : Lang.values()) {
            if (lang.equals(settings.getState().getNativeLanguage())) {
                nativeLanguage.setSelectedItem(lang);
            }
        }

        enableGraziSpellcheckCheckBox.setSelected(settings.getState().getEnabledSpellcheck());
    }
}
