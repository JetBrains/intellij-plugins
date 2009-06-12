package org.osmorc.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author yole
 */
public class OsmorcInspectionToolProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{
        InvalidImportInspection.class,
        MisspelledHeaderNameInspection.class,
        UnregisteredActivatorInspection.class,
        MissingFinalNewlineInspection.class};
  }
}
