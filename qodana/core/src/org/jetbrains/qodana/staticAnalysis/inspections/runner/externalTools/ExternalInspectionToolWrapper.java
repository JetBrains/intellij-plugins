package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionEP;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.JobDescriptor;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public class ExternalInspectionToolWrapper extends InspectionToolWrapper<ExternalInspectionTool, InspectionEP> {
  public ExternalInspectionToolWrapper(@NotNull ExternalInspectionDescriptor descriptor,@NotNull PluginDescriptor pluginDescriptor) {
    super(new ExternalInspectionToolEP(descriptor, pluginDescriptor));
  }

  private ExternalInspectionToolWrapper(@NotNull ExternalInspectionToolWrapper other) {
    super(other);
  }
  @Override
  public @NotNull InspectionToolWrapper<ExternalInspectionTool, InspectionEP> createCopy() {
    return new ExternalInspectionToolWrapper(this);
  }

  @Override
  public JobDescriptor @NotNull [] getJobDescriptors(@NotNull GlobalInspectionContext context) {
    ExternalInspectionTool tool = getTool();
    return ObjectUtils.notNull(tool.getAdditionalJobs(context), JobDescriptor.EMPTY_ARRAY);
  }
}
