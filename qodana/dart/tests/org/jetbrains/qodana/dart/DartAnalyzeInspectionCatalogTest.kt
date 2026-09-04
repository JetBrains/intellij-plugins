package org.jetbrains.qodana.dart

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DartAnalyzeInspectionCatalogTest {
  @Test
  fun `uses the uncategorized inspection for diagnostic codes outside the catalog`() {
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, DartAnalyzeInspectionCatalog.inspectionIdFor("future_diagnostic"))
  }

  @Test
  fun `uses an individual inspection for catalogued lint rules`() {
    assertEquals("DartAnalyze.always_declare_return_types", DartAnalyzeInspectionCatalog.inspectionIdFor("always_declare_return_types"))
  }

  @Test
  fun `declares the uncategorized inspection disabled by default`() {
    val descriptor = DartAnalyzeInspectionCatalog.descriptors().single { it.shortName == DART_ANALYZE_UNCATEGORIZED_SHORT_NAME }

    assertFalse(descriptor.isEnabledByDefault)
    assertTrue(DartAnalyzeInspectionCatalog.descriptors().any { it.shortName == DART_PROJECT_SETUP_SHORT_NAME })
    assertFalse(DartAnalyzeInspectionCatalog.descriptors().any { it.shortName == DART_ANALYZE_SHORT_NAME })
  }

}
