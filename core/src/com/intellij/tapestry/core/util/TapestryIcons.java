package com.intellij.tapestry.core.util;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public interface TapestryIcons extends PlatformIcons {
  Icon TAPESTRY_LOGO_SMALL = IconLoader.getIcon("/com/intellij/tapestry/core/icons/tapestry_logo_small.png");
  Icon PAGES = IconLoader.getIcon("/com/intellij/tapestry/core/icons/pages.png");
  Icon PAGE = IconLoader.getIcon("/com/intellij/tapestry/core/icons/page.png");
  Icon FOLDER = IconLoader.getIcon("/com/intellij/tapestry/core/icons/folder.png");
  Icon COMPONENTS = IconLoader.getIcon("/com/intellij/tapestry/core/icons/components.png");
  Icon COMPONENT = IconLoader.getIcon("/com/intellij/tapestry/core/icons/component.png");
  Icon MIXINS = IconLoader.getIcon("/com/intellij/tapestry/core/icons/mixins.png");
  Icon MIXIN = IconLoader.getIcon("/com/intellij/tapestry/core/icons/mixin.png");
  Icon GROUP_ELEMENT_FILES = IconLoader.getIcon("/com/intellij/tapestry/core/icons/groupElementFiles.png");
  Icon COMPACT_BASE_PACKAGE = IconLoader.getIcon("/com/intellij/tapestry/core/icons/compactBasePackage.png");
  Icon LIBRARY = AllIcons.Modules.Library;
  Icon NAVIGATE = AllIcons.Actions.Browser_externalJavaDoc;
  Icon REFERENCE = AllIcons.Nodes.EjbReference;
  Icon SHOW_LIBRARIES = AllIcons.Nodes.PpLibFolder;
  Icon LOOMY_LOGO = IconLoader.getIcon("/com/intellij/tapestry/core/icons/g5004.png");
}



