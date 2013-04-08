package com.dmarcotte.handlebars;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

public class HbIcons {

    // constants class
    private HbIcons() {}

    public static final Icon FILE_ICON = IconLoader.getIcon("/icons/handlebars_icon.png");

    public static final Icon OPEN_BLOCK = IconLoader.getIcon("/icons/elements/openBlock.png");
    public static final Icon OPEN_INVERSE = IconLoader.getIcon("/icons/elements/openInverse.png");
    public static final Icon OPEN_MUSTACHE = IconLoader.getIcon("/icons/elements/openMustache.png");
    public static final Icon OPEN_UNESCAPED = IconLoader.getIcon("/icons/elements/openUnescaped.png");
    public static final Icon OPEN_PARTIAL = IconLoader.getIcon("/icons/elements/openPartial.png");
}
