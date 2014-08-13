package org.jetbrains.osgi.jps.model.impl;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;

import java.util.List;

/**
 * @author michael.golubev
 */
public class OsmorcGlobalExtensionProperties {
  @AbstractCollection(elementTag = "libraryBundlificationRule")
  public List<LibraryBundlificationRule> myLibraryBundlificationRules = ContainerUtil.newArrayList();
}
