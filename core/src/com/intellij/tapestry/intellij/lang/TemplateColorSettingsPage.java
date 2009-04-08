package com.intellij.tapestry.intellij.lang;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.pages.HTMLColorsPage;
import com.intellij.tapestry.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Tapestry template color settings.
 */
public class TemplateColorSettingsPage extends HTMLColorsPage {

    /**
     * The Tapestry component tag color settings key.
     */
    public static final String TAPESTRY_COMPONENT_TAG_KEY = "TAPESTRY_COMPONENT_TAG";

    /**
     * The Tapestry component parameter color settings key.
     */
    public static final String TAPESTRY_COMPONENT_PARAMETER_KEY = "TAPESTRY_COMPONENT_PARAMATER";

    private static Map<String, TextAttributesKey> _additionalHighlightingTagToDescriptorMap;
    private static TextAttributesKey _tapestryComponentTagNameAttributesKey = TextAttributesKey.createTextAttributesKey(TAPESTRY_COMPONENT_TAG_KEY, new TextAttributes());
    private static TextAttributesKey _tapestryComponentAttributeAttributesKey = TextAttributesKey.createTextAttributesKey(TAPESTRY_COMPONENT_PARAMETER_KEY, new TextAttributes());
    private static String _colorSettingsText;
    private static AttributesDescriptor _attributeDescriptors[];

    private final static Logger _logger = Logger.getInstance(TemplateColorSettingsPage.class.getName());

    static {
        byte[] templateBytes = null;
        try {
            InputStream templateStream = TemplateColorSettingsPage.class.getResourceAsStream("templateColorSettingsText.html");
            templateBytes = new byte[templateStream.available()];
            if (templateStream.read(templateBytes) == 0) {
                _logger.warn("Template color settings demo text is empty");
            }
        } catch (IOException ex) {
            _logger.error(ex);
        }

        _colorSettingsText = new String(templateBytes != null ? templateBytes : new byte[0]).replace('\r', ' ');

        _attributeDescriptors = (new AttributesDescriptor[]{
                new AttributesDescriptor("Component tag", _tapestryComponentTagNameAttributesKey), new AttributesDescriptor("Component parameter", _tapestryComponentAttributeAttributesKey)
        });

        _additionalHighlightingTagToDescriptorMap = new HashMap<String, TextAttributesKey>();
        _additionalHighlightingTagToDescriptorMap.put("componenTagName", _tapestryComponentTagNameAttributesKey);
        _additionalHighlightingTagToDescriptorMap.put("componenTagAttribute", _tapestryComponentAttributeAttributesKey);
    }

    public String getDisplayName() {
        return "Tapestry";
    }

    public Icon getIcon() {
        return Icons.TAPESTRY_LOGO_SMALL;
    }

    @NotNull
    public String getDemoText() {
        return _colorSettingsText;
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return _attributeDescriptors;
    }

    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return _additionalHighlightingTagToDescriptorMap;
    }
}