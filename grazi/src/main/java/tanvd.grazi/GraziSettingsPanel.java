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

import com.intellij.openapi.options.*;
import com.intellij.ui.*;
import org.codehaus.plexus.util.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.language.*;

import javax.swing.*;
import java.util.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziApplicationSettings> {
    private JPanel myWholePanel;
    private CheckBoxList<String> enabledLanguages;

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
        return myWholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziApplicationSettings settings) {
        return !allLanguageShortCodes.values().stream().allMatch(shortCode ->
                settings.getState().languages.contains(shortCode) == enabledLanguages.isItemSelected(shortCode)
        );
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
        settings.loadLanguages();
    }

    @Override
    public void reset(@NotNull GraziApplicationSettings settings) {
        for (String shortCode : allLanguageShortCodes.values()) {
            enabledLanguages.setItemSelected(shortCode, settings.getState().languages.contains(shortCode));
        }
    }
}
