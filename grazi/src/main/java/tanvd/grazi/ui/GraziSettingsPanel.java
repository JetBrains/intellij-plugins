package tanvd.grazi.ui;

import com.intellij.openapi.options.*;
import com.intellij.ui.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziApplicationSettings> {
    private JPanel myWholePanel;
    private CheckBoxList<String> enabledLanguages;

    static private final HashMap<String, String> allLanguageShortCodes = new HashMap<>();

    static {
        allLanguageShortCodes.put("English", "en");
        allLanguageShortCodes.put("Persian", "fa");
        allLanguageShortCodes.put("French", "fr");
        allLanguageShortCodes.put("German", "de");
        allLanguageShortCodes.put("Simple German", "de-DE-x-simple-language");
        allLanguageShortCodes.put("Polish", "pl");
        allLanguageShortCodes.put("Catalan", "ca");
        allLanguageShortCodes.put("Italian", "it");
        allLanguageShortCodes.put("Breton", "br");
        allLanguageShortCodes.put("Dutch", "nl");
        allLanguageShortCodes.put("Portugues", "pt");
        allLanguageShortCodes.put("Russian", "ru");
        allLanguageShortCodes.put("Asturian", "ast");
        allLanguageShortCodes.put("Belarusian", "be");
        allLanguageShortCodes.put("Chinese", "zh");
        allLanguageShortCodes.put("Danish", "da");
        allLanguageShortCodes.put("Esperanto", "eo");
        allLanguageShortCodes.put("Galician", "gl");
        allLanguageShortCodes.put("Greek", "el");
        allLanguageShortCodes.put("Japanese", "ja");
        allLanguageShortCodes.put("Khmer", "km");
        allLanguageShortCodes.put("Romanian", "ro");
        allLanguageShortCodes.put("Slovak", "sk");
        allLanguageShortCodes.put("Slovenian", "sl");
        allLanguageShortCodes.put("Spanish", "es");
        allLanguageShortCodes.put("Swedish", "sv");
        allLanguageShortCodes.put("Tamil", "ta");
        allLanguageShortCodes.put("Tagalog", "tl");
        allLanguageShortCodes.put("Ukrainian", "uk");
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        for (Map.Entry<String, String> languageShortCode : allLanguageShortCodes.entrySet()) {
            enabledLanguages.addItem(languageShortCode.getValue(), languageShortCode.getKey(), false);
        }
        return myWholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziApplicationSettings settings) {
        return !allLanguageShortCodes.values().stream().allMatch(shortCode ->
                settings.getMyState().getLanguages().contains(shortCode) == enabledLanguages.isItemSelected(shortCode)
        );
    }

    @Override
    public void apply(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            if (enabledLanguages.isItemSelected(shortCode)) {
                settings.getMyState().getLanguages().add(shortCode);
            } else {
                settings.getMyState().getLanguages().remove(shortCode);
            }
        }
        settings.loadLanguages();
    }

    @Override
    public void reset(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            enabledLanguages.setItemSelected(shortCode, settings.getMyState().getLanguages().contains(shortCode));
        }
    }
}
