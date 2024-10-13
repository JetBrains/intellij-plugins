package org.jetbrains.qodana.coverage

/*
  Supported coverage engine types. New ones should be listed here
 */
enum class CoverageEngineType {
  JavaCoverageEngine,
  XMLReportEngine,
  PhpUnitCoverageEngine,
  JestCoverageEngine,
  MochaCoverageEngine,
  PyCoverageEngine,
  GoCoverageEngine,
  Other;

  companion object {
    fun tryCompute(input: String): CoverageEngineType {
      try {
        return CoverageEngineType.valueOf(input)
      } catch (e: IllegalArgumentException) {
        return Other
      }
    }
  }
}

/*
  Languages that support coverage.
 */
enum class CoverageLanguage {
  JVM,
  PHP,
  JavaScript,
  Python,
  Go,
  None, // when no coverage was received
  Other;

  companion object {
    fun mapEngine(engine: String) = mapEngine(CoverageEngineType.tryCompute(engine))

    private fun mapEngine(engine: CoverageEngineType) = when(engine) {
      CoverageEngineType.JavaCoverageEngine, CoverageEngineType.XMLReportEngine -> JVM
      CoverageEngineType.PhpUnitCoverageEngine -> PHP
      CoverageEngineType.JestCoverageEngine, CoverageEngineType.MochaCoverageEngine -> JavaScript
      CoverageEngineType.PyCoverageEngine -> Python
      CoverageEngineType.GoCoverageEngine -> Go
      else -> Other
    }
  }
}