package tanvd.grazi;

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.*;
import com.intellij.ui.*;
import org.codehaus.plexus.util.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.language.*;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziApplicationSettings> {
    private JPanel myWholePanel;
    private CheckBoxList<String> enabledLanguages;
    private TextFieldWithBrowseButton graziFolder;

    static private final TreeMap<String, String> allLanguageShortCodes = new TreeMap<>();

    static {
        for (Lang lang : Lang.values()) {
            allLanguageShortCodes.put(StringUtils.capitalise(lang.name().toLowerCase()), lang.getShortCode());
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        for (Map.Entry<String, String> languageShortCode : allLanguageShortCodes.entrySet()) {
            enabledLanguages.addItem(languageShortCode.getValue(), languageShortCode.getKey(), false);
        }

        graziFolder.addBrowseFolderListener("", "Grazi folder", null, new FileChooserDescriptor(true, false, false, false, false, false),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

        return myWholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziApplicationSettings settings) {
        return !allLanguageShortCodes.values().stream().allMatch(shortCode ->
                settings.getState().languages.contains(shortCode) == enabledLanguages.isItemSelected(shortCode)
        ) || !settings.getState().graziFolder.getAbsolutePath().equals(graziFolder.getText());
    }


    @Override
    public void apply(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            if (enabledLanguages.isItemSelected(shortCode)) {
                settings.getState().languages.add(shortCode);
            } else {
                settings.getState().languages.remove(shortCode);
            }
        }
        settings.getState().graziFolder = new File(graziFolder.getText());
        settings.init();
    }

    @Override
    public void reset(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            enabledLanguages.setItemSelected(shortCode, settings.getState().languages.contains(shortCode));
        }
        graziFolder.setText(settings.getState().graziFolder.getAbsolutePath());
    }
}
