package com.intellij.tapestry.intellij;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.tapestry.core.TapestryConstants;
import icons.TapestryIcons;

public class TapestryFileTemplateGroupFactory implements FileTemplateGroupDescriptorFactory {

    private static final @NlsSafe String TEMPLATE_GROUP_NAME = "Tapestry";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor root = new FileTemplateGroupDescriptor(TEMPLATE_GROUP_NAME, TapestryIcons.Tapestry_logo_small);

        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.MODULE_BUILDER_CLASS_TEMPLATE_NAME, StdFileTypes.JAVA.getIcon()));
        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.COMPONENT_CLASS_TEMPLATE_NAME, StdFileTypes.JAVA.getIcon()));
        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.COMPONENT_TEMPLATE_TEMPLATE_NAME,
                                                    TapestryIcons.Tapestry_logo_small));
        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.MIXIN_CLASS_TEMPLATE_NAME, StdFileTypes.JAVA.getIcon()));
        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.PAGE_CLASS_TEMPLATE_NAME, StdFileTypes.JAVA.getIcon()));
        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.PAGE_TEMPLATE_TEMPLATE_NAME, TapestryIcons.Tapestry_logo_small));
        root.addTemplate(new FileTemplateDescriptor(TapestryConstants.POM_TEMPLATE_NAME, StdFileTypes.XML.getIcon()));

        return root;
    }
}
