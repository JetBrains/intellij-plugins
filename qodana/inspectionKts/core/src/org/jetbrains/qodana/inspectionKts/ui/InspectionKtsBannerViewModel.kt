package org.jetbrains.qodana.inspectionKts.ui

import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.flow.StateFlow
import javax.swing.Icon

interface InspectionKtsBannerViewModel {
  class ExecutionError(
    val openExceptionInLogAction: () -> Unit,
    val ignoreExceptionAction: () -> Unit
  )

  sealed interface CompilationStatus {

    val recompileAction: () -> Unit

    class Compiled(
      val executionErrorDuringAnalysis: StateFlow<ExecutionError?>,
      val isOutdated: Boolean,
      override val recompileAction: () -> Unit
    ) : CompilationStatus

    class Failed(
      val isOutdated: Boolean,
      val openExceptionInLogAction: () -> Unit,
      override val recompileAction: () -> Unit
    ) : CompilationStatus

    class Compiling(override val recompileAction: () -> Unit) : CompilationStatus

    class Cancelled(override val recompileAction: () -> Unit) : CompilationStatus
  }

  class PsiViewerOpener(val openAction: () -> Unit)

  class Example(
    val icon: Icon,
    @NlsContexts.ListItem val text: String,
    val openExampleAction: () -> Unit
  )

  val compilationStatus: StateFlow<CompilationStatus?>

  val psiViewerOpener: PsiViewerOpener?

  val examples: List<Example>
}