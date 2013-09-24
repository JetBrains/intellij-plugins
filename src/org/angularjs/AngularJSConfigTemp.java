package org.angularjs;

import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by johnlindquist on 7/11/13.
 *
 * This class is a temporary fix until Kotlin supports nested annotations
 */
@State(
        name="AngularJS",
        storages= {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/angularjs.xml"
                )}
)
public class AngularJSConfigTemp extends AngularJSConfig implements Configurable, PersistentStateComponent<AngularJSConfig>, ApplicationComponent {
    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @Nls
    @Override
    public String getDisplayName() {
        return "AngularJS";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "settings.angularjs";
    }

    @NotNull
    @Override
    public String getComponentName() {
        return AngularJSConfig.object$.getComponentName();
    }

    @Nullable
    @Override
    public AngularJSConfig getState() {
        return this;
    }

    @Override
    public void loadState(AngularJSConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
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

    public static class UIImpl extends JPanel implements Configurable {


        private final JCheckBox addWhitespaceBetweenBraces;


        public UIImpl() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            addWhitespaceBetweenBraces = new JCheckBox("Add Whitespace Between Braces");
            addWhitespaceBetweenBraces.setMnemonic('A');
            addWhitespaceBetweenBraces.setSelected(AngularJSConfig.object$.getWhiteSpace());

            add(addWhitespaceBetweenBraces);

            final JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);

            final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            jPanel.add(panel, BorderLayout.SOUTH);
            jPanel.setAlignmentX(0);
            add(jPanel);
        }

        @Nls
        public String getDisplayName() {
            return "AngularJS";
        }

        @Nullable
        @NonNls
        public String getHelpTopic() {
            return "settings.angularjs";
        }

        public void disposeUIResources() {
        }

        public JComponent createComponent() {
            return this;
        }

        public boolean isModified() {
            return AngularJSConfig.object$.getWhiteSpace() != addWhitespaceBetweenBraces.isSelected();
        }

        public void apply() {
            boolean oldValue = AngularJSConfig.object$.getWhiteSpace();

            AngularJSConfig.object$.setWhiteSpace(addWhitespaceBetweenBraces.isSelected());

            // TODO: make this a ConfigListener
            if (oldValue != AngularJSConfig.object$.getWhiteSpace()) {
            }
        }

        public void reset() {
        }

        @NotNull
        public String getId() {
            return getHelpTopic();
        }

        public Runnable enableSearch(String option) {
            return null;
        }
    }
}
