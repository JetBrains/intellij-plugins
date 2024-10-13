package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy


class QodanaQuickFixesCleanupTest: QodanaQuickFixesCommonTests(FixesStrategy.CLEANUP)