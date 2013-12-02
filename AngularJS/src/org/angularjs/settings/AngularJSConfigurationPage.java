package org.angularjs.settings;

import com.intellij.openapi.options.BeanConfigurable;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Created by denofevil on 26/11/13.
 */
public class AngularJSConfigurationPage extends BeanConfigurable<AngularJSConfig> implements UnnamedConfigurable {
    protected AngularJSConfigurationPage() {
        super(AngularJSConfig.getInstance());

        checkBox("INSERT_WHITESPACE", "Auto-insert whitespace in the interpolations");
    }

    @Override
    public JComponent createComponent() {
        JComponent result = super.createComponent();
        assert result != null;
        result.setBorder(IdeBorderFactory.createTitledBorder("AngularJS"));
        return result;
    }
}
