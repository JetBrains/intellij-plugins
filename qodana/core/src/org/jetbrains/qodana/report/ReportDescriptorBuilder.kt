package org.jetbrains.qodana.report

/**
 * If you want to create [ReportDescriptor], and creation is non-trivial (e.g. validation is required,
 * or you need to obtain some parameters), spawn notification if failed, consider moving creation into separate builder
 */
interface ReportDescriptorBuilder<T : ReportDescriptor>  {
  suspend fun createReportDescriptor(): T?
}