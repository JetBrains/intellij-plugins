/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tanvd.grazi;

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.*;
import com.intellij.ui.CheckBoxList;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class GraziSettingsPanel implements ConfigurableUi<GraziToolProjectSettings> {
    private JPanel myWholePanel;
    private TextFieldWithBrowseButton myGraziPathField;
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
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);

        myGraziPathField.addBrowseFolderListener(
                "",
                "Grazi home path",
                null,
                fileChooserDescriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        return myWholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziToolProjectSettings settings) {
        return !(Comparing.equal(myGraziPathField.getText(), settings.getGraziHome()) &&
                allLanguageShortCodes.values().stream().allMatch(shortCode ->
                        settings.getState().languages.contains(shortCode) == enabledLanguages.isItemSelected(shortCode)
                ));
    }

    @Override
    public void apply(@NotNull GraziToolProjectSettings settings) {
        settings.setGraziHome(myGraziPathField.getText());
        for (String shortCode : allLanguageShortCodes.values()) {
            if (enabledLanguages.isItemSelected(shortCode))
                settings.getState().languages.add(shortCode);
            else
                settings.getState().languages.remove(shortCode);
        }
        settings.loadLanguages();
    }

    @Override
    public void reset(@NotNull GraziToolProjectSettings settings) {
        myGraziPathField.setText(settings.getGraziHome());
        for (String shortCode : allLanguageShortCodes.values()) {
            enabledLanguages.setItemSelected(shortCode, settings.getState().languages.contains(shortCode));
        }
    }
}
