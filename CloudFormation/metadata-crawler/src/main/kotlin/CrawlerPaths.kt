import java.io.File

object CrawlerPaths {
  val metadataOutputDir: File = resolvePath(
    propertyName = "cloudformation.metadata.outputDir",
    envName = "METADATA_OUTPUT_DIR",
    defaultPath = "build/generated/metadata"
  )

  val metadataResourceDir: File = File(metadataOutputDir, "com/intellij/aws/meta")

  val officialExamplesOutputDir: File = resolvePath(
    propertyName = "cloudformation.examples.official.outputDir",
    envName = "OFFICIAL_EXAMPLES_OUTPUT_DIR",
    defaultPath = "build/generated/examples/officialExamples/src"
  )

  val serverlessExamplesOutputDir: File = resolvePath(
    propertyName = "cloudformation.examples.serverless.outputDir",
    envName = "SERVERLESS_EXAMPLES_OUTPUT_DIR",
    defaultPath = "build/generated/examples/serverless-application-model/src"
  )

  private fun resolvePath(propertyName: String, envName: String, defaultPath: String): File {
    val customPath = System.getProperty(propertyName)?.takeIf { it.isNotBlank() }
                     ?: System.getenv(envName)?.takeIf { it.isNotBlank() }
                     ?: defaultPath
    return File(customPath).absoluteFile
  }
}
