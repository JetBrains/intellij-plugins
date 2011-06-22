package com.intellij.tapestry.intellij.util;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

/**
 * Icons used by Loomy.
 */
public abstract class Icons implements com.intellij.tapestry.core.util.Icons, PlatformIcons {

    public static final Icon REFRESH = IconLoader.getIcon("/actions/sync.png");

    public static final Icon FILTER = IconLoader.getIcon("/ant/filter.png");

    public static final Icon DELETE = IconLoader.getIcon("/actions/cancel.png");

    public static final Icon LIBRARY = IconLoader.getIcon("/modules/library.png");

    public static final Icon NAVIGATE = IconLoader.getIcon("/actions/browser-externalJavaDoc.png");

    public static final Icon REFERENCE = IconLoader.getIcon("/nodes/ejbReference.png");

    public static final Icon SHOW_LIBRARIES = IconLoader.getIcon("/nodes/ppLibClosed.png");
}
