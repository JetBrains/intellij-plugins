package org.angularjs;

/**
 * Created with IntelliJ IDEA.
 * User: John
 * Date: 10/15/12
 * Time: 11:30 AM
 */

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import com.intellij.openapi.diagnostic.Logger;

public class AngularJSTemplatesProvider implements DefaultLiveTemplatesProvider {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.template.impl.TemplateSettings");

    public AngularJSTemplatesProvider() {
        LOG.info("Hello");
    }

    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return new String[]{"liveTemplates/AngularJS"};
    }

    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return null;
    }
}

