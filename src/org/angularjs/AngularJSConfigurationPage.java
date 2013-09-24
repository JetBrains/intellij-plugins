package org.angularjs;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by johnlindquist on 9/23/13.
 */
public class AngularJSConfigurationPage implements SearchableConfigurable {
    private JCheckBox includeWhitespaceBetweenBracesCheckBox;
    private JPanel myPanel;

    @NotNull
    @Override
    public String getId() {
        return "editor.preferences.AngularJS";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "AngularJS";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        includeWhitespaceBetweenBracesCheckBox.setSelected(AngularJSConfig.object$.getWhiteSpace());
        includeWhitespaceBetweenBracesCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                boolean selected = includeWhitespaceBetweenBracesCheckBox.isSelected();
                AngularJSConfig.object$.setWhiteSpace(selected);
            }
        });


        return myPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
