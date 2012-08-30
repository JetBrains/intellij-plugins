package com.intellij.tapestry.core.util;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;
import icons.TapestryCoreIcons;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public interface TapestryIcons extends PlatformIcons {
  Icon TAPESTRY_LOGO_SMALL = TapestryCoreIcons.Tapestry_logo_small;
  Icon PAGES = TapestryCoreIcons.Pages;
  Icon PAGE = TapestryCoreIcons.Page;
  Icon FOLDER = TapestryCoreIcons.Folder;
  Icon COMPONENTS = TapestryCoreIcons.Components;
  Icon COMPONENT = TapestryCoreIcons.Component;
  Icon MIXINS = TapestryCoreIcons.Mixins;
  Icon MIXIN = TapestryCoreIcons.Mixin;
  Icon GROUP_ELEMENT_FILES = TapestryCoreIcons.GroupElementFiles;
  Icon COMPACT_BASE_PACKAGE = TapestryCoreIcons.CompactBasePackage;
  Icon LIBRARY = AllIcons.Modules.Library;
  Icon NAVIGATE = AllIcons.Actions.Browser_externalJavaDoc;
  Icon REFERENCE = AllIcons.Nodes.EjbReference;
  Icon SHOW_LIBRARIES = AllIcons.Nodes.PpLibFolder;
  Icon LOOMY_LOGO = IconLoader.getIcon("/com/intellij/tapestry/core/icons/g5004.png");
}



