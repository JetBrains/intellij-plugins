package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.ex.JobDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExternalInspectionTool extends InspectionProfileEntry {
  public JobDescriptor @Nullable [] getAdditionalJobs(@NotNull GlobalInspectionContext context) {
    return null;
  }
}
