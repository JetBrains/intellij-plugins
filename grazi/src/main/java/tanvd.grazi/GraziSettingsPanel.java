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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GraziSettingsPanel implements ConfigurableUi<GraziToolProjectSettings> {
    private JPanel myWholePanel;
    private TextFieldWithBrowseButton myGraziPathField;
    private JCheckBox ukrainianCheckBox;

    @NotNull
    @Override
    public JComponent getComponent() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);

        myGraziPathField.addBrowseFolderListener(
                "",
                "Grazi executable path",
                null,
                fileChooserDescriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        return myWholePanel;
    }

    @Override
    public boolean isModified(@NotNull GraziToolProjectSettings settings) {
        return !(Comparing.equal(myGraziPathField.getText(), settings.getGraziHome()) &&
                ukrainianCheckBox.isSelected() == settings.getUaEnabled());
    }

    @Override
    public void apply(@NotNull GraziToolProjectSettings settings) {
        settings.setGraziHome(myGraziPathField.getText());
        settings.setUaEnabled(ukrainianCheckBox.isSelected());
        settings.loadLanguages();
    }

    @Override
    public void reset(@NotNull GraziToolProjectSettings settings) {
        myGraziPathField.setText(settings.getGraziHome());
        ukrainianCheckBox.setSelected(settings.getUaEnabled());
    }
}
