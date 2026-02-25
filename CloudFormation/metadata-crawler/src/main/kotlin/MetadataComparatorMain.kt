import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypeDescription
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile
import kotlin.system.exitProcess

private const val METADATA_RESOURCE_DIR = "com/intellij/aws/meta"
private const val METADATA_FILE_NAME = "cloudformation-metadata.xml"
private const val DESCRIPTIONS_FILE_NAME = "cloudformation-descriptions.xml"
private const val METADATA_ENTRY_PATH = "$METADATA_RESOURCE_DIR/$METADATA_FILE_NAME"
private const val DESCRIPTIONS_ENTRY_PATH = "$METADATA_RESOURCE_DIR/$DESCRIPTIONS_FILE_NAME"
private const val MAX_NAMES_IN_DIFF_LINE = 10
private const val SUMMARY_NAMES_PREVIEW_LIMIT = 20
private val ONLY_IN_SIDE_REGEX = Regex("""^(.+?) only in (left|right) \((\d+)\):\s*(.*)$""")
private val SUMMARY_INDEX_REGEX = Regex("""\[[^]]*]""")
private val SUMMARY_TRUNCATED_SUFFIX_REGEX = Regex(""", \.\.\. \(\+\d+ more\)$""")
private val NON_TRUNCATED_DIFF_PATHS = setOf(
  "metadata.predefinedParameters",
  "metadata.resourceTypes",
  "descriptions.resourceTypes",
)

object MetadataComparatorMain {
  private data class CliOptions(
    val paths: List<String>,
    val showDetails: Boolean,
    val showHelp: Boolean,
  )

  private data class DifferenceSummary(
    val leftOnlyCounts: MutableMap<String, Int> = HashMap(),
    val rightOnlyCounts: MutableMap<String, Int> = HashMap(),
    var leftOnlyResourceTypes: List<String> = emptyList(),
    var rightOnlyResourceTypes: List<String> = emptyList(),
    var leftOnlyPredefinedParameters: List<String> = emptyList(),
    var rightOnlyPredefinedParameters: List<String> = emptyList(),
  )

  private data class MetadataBundle(
    val metadata: CloudFormationMetadata,
    val descriptions: CloudFormationResourceTypesDescription,
  )

  @JvmStatic
  fun main(args: Array<String>) {
    if (args.isEmpty()) {
      printUsage()
      return
    }

    val options = parseCliOptions(args)
    if (options.showHelp) {
      printUsage()
      return
    }
    if (options.paths.isEmpty()) {
      printUsage()
      exitProcess(2)
    }

    val exitCode = try {
      compareByInferredMode(options.paths, options.showDetails)
    }
    catch (e: IllegalArgumentException) {
      System.err.println(e.message)
      printUsage()
      2
    }
    catch (e: Throwable) {
      System.err.println(e.message ?: "Unexpected error")
      2
    }

    exitProcess(exitCode)
  }

  private fun parseCliOptions(args: Array<String>): CliOptions {
    var showDetails = false
    var showHelp = false
    val paths = ArrayList<String>()

    for (arg in args) {
      when (arg) {
        "--details" -> showDetails = true
        "--summary-only" -> showDetails = false
        "--help", "-h" -> showHelp = true
        else -> {
          if (arg.startsWith("--")) {
            throw IllegalArgumentException("Unknown option: $arg")
          }
          paths += arg
        }
      }
    }

    return CliOptions(
      paths = paths,
      showDetails = showDetails,
      showHelp = showHelp,
    )
  }

  private fun compareByInferredMode(paths: List<String>, showDetails: Boolean): Int {
    return when (paths.size) {
      1 -> compareWithDefaultGatheredMetadata(File(paths[0]), showDetails)
      2 -> compareWithExplicitPaths(File(paths[0]), File(paths[1]), showDetails)
      else -> throw IllegalArgumentException(
        "Expected one or two paths, but got ${paths.size}",
      )
    }
  }

  private fun compareWithDefaultGatheredMetadata(jarPath: File, showDetails: Boolean): Int {
    if (!jarPath.isJarPath()) {
      throw IllegalArgumentException(
        "Single-argument mode expects a jar path, but got '${jarPath.path}'",
      )
    }

    val gatheredDir = resolveGatheredMetadataDir(null)

    val gathered = loadFromDirectory(gatheredDir)
    val jar = loadFromJar(jarPath)

    val sourceA = "gathered metadata (${gatheredDir.absolutePath})"
    val sourceB = "jar (${jarPath.absolutePath})"
    return printResult(sourceA, sourceB, compareMetadata(gathered, jar), showDetails)
  }

  private fun compareWithExplicitPaths(leftPath: File, rightPath: File, showDetails: Boolean): Int {
    val leftIsJar = leftPath.isJarPath()
    val rightIsJar = rightPath.isJarPath()

    if (!leftIsJar && !rightIsJar) {
      throw IllegalArgumentException(
        "Could not infer comparison mode: no jar paths found in '${leftPath.path}' and '${rightPath.path}'",
      )
    }

    val left = if (leftIsJar) {
      loadFromJar(leftPath)
    }
    else {
      loadFromDirectory(resolveGatheredMetadataDir(leftPath.path))
    }

    val right = if (rightIsJar) {
      loadFromJar(rightPath)
    }
    else {
      loadFromDirectory(resolveGatheredMetadataDir(rightPath.path))
    }

    val sourceA = if (leftIsJar) {
      "jar (${leftPath.absolutePath})"
    }
    else {
      "gathered metadata (${resolveGatheredMetadataDir(leftPath.path).absolutePath})"
    }
    val sourceB = if (rightIsJar) {
      "jar (${rightPath.absolutePath})"
    }
    else {
      "gathered metadata (${resolveGatheredMetadataDir(rightPath.path).absolutePath})"
    }

    return printResult(sourceA, sourceB, compareMetadata(left, right), showDetails)
  }

  private fun File.isJarPath(): Boolean {
    if (extension.equals("jar", ignoreCase = true)) {
      return true
    }

    if (!isFile) {
      return false
    }

    return runCatching {
      JarFile(this).use { true }
    }.getOrElse { false }
  }

  private fun printResult(sourceA: String, sourceB: String, diff: List<String>, showDetails: Boolean): Int {
    if (diff.isEmpty()) {
      println("No differences found between $sourceA and $sourceB")
      return 0
    }

    println("Found ${diff.size} difference(s) between $sourceA and $sourceB:")
    val summary = buildDifferenceSummary(diff)
    printDifferenceSummary(summary, sourceA, sourceB)
    if (showDetails) {
      println("Detailed differences:")
      diff.forEach { println("- $it") }
    }
    else {
      println("Detailed differences are hidden. Re-run with '--details' to print all items.")
    }
    return 1
  }

  private fun buildDifferenceSummary(diff: List<String>): DifferenceSummary {
    val summary = DifferenceSummary()

    for (line in diff) {
      val match = ONLY_IN_SIDE_REGEX.matchEntire(line) ?: continue

      val path = match.groupValues[1]
      val side = match.groupValues[2]
      val count = match.groupValues[3].toInt()
      val names = parseNames(match.groupValues[4])
      val normalizedPath = path.replace(SUMMARY_INDEX_REGEX, "[]")

      when (side) {
        "left" -> summary.leftOnlyCounts.addCount(normalizedPath, count)
        "right" -> summary.rightOnlyCounts.addCount(normalizedPath, count)
      }

      when (path) {
        "metadata.resourceTypes" -> {
          when (side) {
            "left" -> summary.leftOnlyResourceTypes = names
            "right" -> summary.rightOnlyResourceTypes = names
          }
        }

        "metadata.predefinedParameters" -> {
          when (side) {
            "left" -> summary.leftOnlyPredefinedParameters = names
            "right" -> summary.rightOnlyPredefinedParameters = names
          }
        }
      }
    }

    return summary
  }

  private fun parseNames(rawValue: String): List<String> {
    val normalized = rawValue.trim().replace(SUMMARY_TRUNCATED_SUFFIX_REGEX, "")
    if (normalized.isEmpty()) {
      return emptyList()
    }

    return normalized.split(", ").filter { it.isNotBlank() }
  }

  private fun MutableMap<String, Int>.addCount(path: String, value: Int) {
    this[path] = (this[path] ?: 0) + value
  }

  private fun printDifferenceSummary(summary: DifferenceSummary, sourceA: String, sourceB: String) {
    val leftMetadataOnly = summary.leftOnlyCounts.filterKeys { it.startsWith("metadata.") }
    val rightMetadataOnly = summary.rightOnlyCounts.filterKeys { it.startsWith("metadata.") }

    if (leftMetadataOnly.isEmpty() && rightMetadataOnly.isEmpty()) {
      return
    }

    println("Summary:")
    printSideSummary(
      title = "Present in $sourceA, missing in $sourceB",
      counts = leftMetadataOnly,
    )
    printSideSummary(
      title = "Present in $sourceB, missing in $sourceA",
      counts = rightMetadataOnly,
    )

    if (summary.leftOnlyResourceTypes.isNotEmpty()) {
      println(
        "- Resource types missing in $sourceB (${summary.leftOnlyResourceTypes.size}): " +
        formatSummaryPreview(summary.leftOnlyResourceTypes),
      )
    }
    if (summary.rightOnlyResourceTypes.isNotEmpty()) {
      println(
        "- Resource types missing in $sourceA (${summary.rightOnlyResourceTypes.size}): " +
        formatSummaryPreview(summary.rightOnlyResourceTypes),
      )
    }

    if (summary.leftOnlyPredefinedParameters.isNotEmpty()) {
      println(
        "- Predefined parameters missing in $sourceB (${summary.leftOnlyPredefinedParameters.size}): " +
        summary.leftOnlyPredefinedParameters.joinToString(", "),
      )
    }
    if (summary.rightOnlyPredefinedParameters.isNotEmpty()) {
      println(
        "- Predefined parameters missing in $sourceA (${summary.rightOnlyPredefinedParameters.size}): " +
        summary.rightOnlyPredefinedParameters.joinToString(", "),
      )
    }
  }

  private fun printSideSummary(title: String, counts: Map<String, Int>) {
    if (counts.isEmpty()) {
      return
    }

    println("- $title:")

    val preferredOrder = listOf(
      "metadata.predefinedParameters",
      "metadata.resourceTypes",
      "metadata.resourceTypes[].properties",
      "metadata.resourceTypes[].attributes",
    )

    val printedKeys = HashSet<String>()
    for (key in preferredOrder) {
      val count = counts[key] ?: continue
      println("  - ${renderSummaryPath(key)}: $count")
      printedKeys += key
    }

    val extraKeys = counts.keys.filter { it !in printedKeys }.sorted()
    for (key in extraKeys) {
      println("  - ${renderSummaryPath(key)}: ${counts.getValue(key)}")
    }
  }

  private fun renderSummaryPath(path: String): String {
    return when (path) {
      "metadata.predefinedParameters" -> "predefinedParameters"
      "metadata.resourceTypes" -> "resourceTypes"
      "metadata.resourceTypes[].properties" -> "resourceType properties"
      "metadata.resourceTypes[].attributes" -> "resourceType attributes"
      else -> path
    }
  }

  private fun formatSummaryPreview(names: List<String>): String {
    if (names.size <= SUMMARY_NAMES_PREVIEW_LIMIT) {
      return names.joinToString(", ")
    }

    val preview = names.take(SUMMARY_NAMES_PREVIEW_LIMIT).joinToString(", ")
    val hidden = names.size - SUMMARY_NAMES_PREVIEW_LIMIT
    return "$preview, ... (+$hidden more)"
  }

  private fun compareMetadata(left: MetadataBundle, right: MetadataBundle): List<String> {
    val diff = ArrayList<String>()
    compareMetadata(left.metadata, right.metadata, diff)
    compareDescriptions(left.descriptions, right.descriptions, diff)
    return diff
  }

  private fun compareMetadata(
    left: CloudFormationMetadata,
    right: CloudFormationMetadata,
    diff: MutableList<String>,
  ) {
    if (left.limits != right.limits) {
      diff += "metadata.limits changed: ${left.limits} -> ${right.limits}"
    }

    compareStringList(left.predefinedParameters, right.predefinedParameters, diff)

    compareResourceTypes(left.resourceTypes, right.resourceTypes, diff)
  }

  private fun compareDescriptions(
    left: CloudFormationResourceTypesDescription,
    right: CloudFormationResourceTypesDescription,
    diff: MutableList<String>,
  ) {
    val path = "descriptions.resourceTypes"
    compareMapKeys(path, left.resourceTypes.keys, right.resourceTypes.keys, diff)

    val sharedTypes = left.resourceTypes.keys.intersect(right.resourceTypes.keys).sorted()
    for (resourceTypeName in sharedTypes) {
      val leftDescription = left.resourceTypes.getValue(resourceTypeName)
      val rightDescription = right.resourceTypes.getValue(resourceTypeName)
      compareResourceTypeDescription(
        path = "$path[$resourceTypeName]",
        left = leftDescription,
        right = rightDescription,
        diff = diff,
      )
    }
  }

  private fun compareResourceTypes(
    left: Map<String, CloudFormationResourceType>,
    right: Map<String, CloudFormationResourceType>,
    diff: MutableList<String>,
  ) {
    val path = "metadata.resourceTypes"
    compareMapKeys(path, left.keys, right.keys, diff)

    val sharedTypes = left.keys.intersect(right.keys).sorted()
    for (resourceTypeName in sharedTypes) {
      val leftType = left.getValue(resourceTypeName)
      val rightType = right.getValue(resourceTypeName)
      compareResourceType(
        path = "$path[$resourceTypeName]",
        left = leftType,
        right = rightType,
        diff = diff,
      )
    }
  }

  private fun compareResourceType(
    path: String,
    left: CloudFormationResourceType,
    right: CloudFormationResourceType,
    diff: MutableList<String>,
  ) {
    if (left.transform != right.transform) {
      diff += "$path.transform changed: '${left.transform}' -> '${right.transform}'"
    }
    if (left.url != right.url) {
      diff += "$path.url changed: '${left.url}' -> '${right.url}'"
    }

    compareMapKeys("$path.properties", left.properties.keys, right.properties.keys, diff)
    val sharedProperties = left.properties.keys.intersect(right.properties.keys).sorted()
    for (propertyName in sharedProperties) {
      val leftProperty = left.properties.getValue(propertyName)
      val rightProperty = right.properties.getValue(propertyName)
      compareResourceProperty(
        path = "$path.properties[$propertyName]",
        left = leftProperty,
        right = rightProperty,
        diff = diff,
      )
    }

    compareMapKeys("$path.attributes", left.attributes.keys, right.attributes.keys, diff)
    val sharedAttributes = left.attributes.keys.intersect(right.attributes.keys).sorted()
    val changedAttributes = sharedAttributes.filter {
      left.attributes.getValue(it) != right.attributes.getValue(it)
    }
    if (changedAttributes.isNotEmpty()) {
      diff += "$path.attributes changed values (${changedAttributes.size}): ${formatNames(changedAttributes)}"
    }
  }

  private fun compareResourceProperty(
    path: String,
    left: CloudFormationResourceProperty,
    right: CloudFormationResourceProperty,
    diff: MutableList<String>,
  ) {
    if (left.type != right.type) {
      diff += "$path.type changed: '${left.type}' -> '${right.type}'"
    }
    if (left.required != right.required) {
      diff += "$path.required changed: '${left.required}' -> '${right.required}'"
    }
    if (left.updateRequires != right.updateRequires) {
      diff += "$path.updateRequires changed"
    }
    if (left.url != right.url) {
      diff += "$path.url changed: '${left.url}' -> '${right.url}'"
    }
  }

  private fun compareResourceTypeDescription(
    path: String,
    left: CloudFormationResourceTypeDescription,
    right: CloudFormationResourceTypeDescription,
    diff: MutableList<String>,
  ) {
    if (left.description != right.description) {
      diff += "$path.description changed"
    }

    compareStringMap(
      path = "$path.properties",
      left = left.properties,
      right = right.properties,
      diff = diff,
    )

    compareStringMap(
      path = "$path.attributes",
      left = left.attributes,
      right = right.attributes,
      diff = diff,
    )
  }

  private fun compareStringMap(
    path: String,
    left: Map<String, String>,
    right: Map<String, String>,
    diff: MutableList<String>,
  ) {
    compareMapKeys(path, left.keys, right.keys, diff)
    val changedKeys = left.keys.intersect(right.keys).filter {
      left.getValue(it) != right.getValue(it)
    }.sorted()

    if (changedKeys.isNotEmpty()) {
      diff += "$path changed values (${changedKeys.size}): ${formatNames(changedKeys)}"
    }
  }

  private fun compareStringList(
    left: List<String>,
    right: List<String>,
    diff: MutableList<String>,
  ) {
    val path = "metadata.predefinedParameters"
    if (left == right) {
      return
    }

    val leftSet = left.toSet()
    val rightSet = right.toSet()
    val leftOnly = leftSet.subtract(rightSet)
    val rightOnly = rightSet.subtract(leftSet)

    if (leftOnly.isNotEmpty()) {
      diff += "$path only in left (${leftOnly.size}): ${formatNamesForPath(path, leftOnly)}"
    }
    if (rightOnly.isNotEmpty()) {
      diff += "$path only in right (${rightOnly.size}): ${formatNamesForPath(path, rightOnly)}"
    }

    if (leftOnly.isEmpty() && rightOnly.isEmpty()) {
      diff += "$path order changed"
    }
  }

  private fun compareMapKeys(
    path: String,
    left: Set<String>,
    right: Set<String>,
    diff: MutableList<String>,
  ) {
    val leftOnly = left.subtract(right)
    val rightOnly = right.subtract(left)

    if (leftOnly.isNotEmpty()) {
      diff += "$path only in left (${leftOnly.size}): ${formatNamesForPath(path, leftOnly)}"
    }
    if (rightOnly.isNotEmpty()) {
      diff += "$path only in right (${rightOnly.size}): ${formatNamesForPath(path, rightOnly)}"
    }
  }

  private fun formatNamesForPath(path: String, names: Collection<String>): String {
    return if (path in NON_TRUNCATED_DIFF_PATHS) {
      formatNames(names, truncate = false)
    }
    else {
      formatNames(names)
    }
  }

  private fun formatNames(names: Collection<String>, truncate: Boolean = true): String {
    val sorted = names.sorted()
    if (!truncate || sorted.size <= MAX_NAMES_IN_DIFF_LINE) {
      return sorted.joinToString(", ")
    }

    val preview = sorted.take(MAX_NAMES_IN_DIFF_LINE).joinToString(", ")
    val hidden = sorted.size - MAX_NAMES_IN_DIFF_LINE
    return "$preview, ... (+$hidden more)"
  }

  private fun resolveGatheredMetadataDir(path: String?): File {
    val candidate = path?.let { File(it) } ?: CrawlerPaths.metadataResourceDir

    val rootMetadataFile = File(candidate, METADATA_FILE_NAME)
    if (rootMetadataFile.isFile) {
      return candidate.absoluteFile
    }

    val nested = File(candidate, METADATA_RESOURCE_DIR)
    val nestedMetadataFile = File(nested, METADATA_FILE_NAME)
    if (nestedMetadataFile.isFile) {
      return nested.absoluteFile
    }

    throw IllegalArgumentException(
      "Could not find '$METADATA_FILE_NAME' under '${candidate.absolutePath}'. " +
      "Expected either '<dir>/$METADATA_FILE_NAME' or '<dir>/$METADATA_RESOURCE_DIR/$METADATA_FILE_NAME'.",
    )
  }

  private fun loadFromDirectory(directory: File): MetadataBundle {
    if (!directory.isDirectory) {
      throw IllegalArgumentException("Directory does not exist: ${directory.absolutePath}")
    }

    val metadataFile = File(directory, METADATA_FILE_NAME)
    val descriptionsFile = File(directory, DESCRIPTIONS_FILE_NAME)

    if (!metadataFile.isFile) {
      throw IllegalArgumentException("File does not exist: ${metadataFile.absolutePath}")
    }
    if (!descriptionsFile.isFile) {
      throw IllegalArgumentException("File does not exist: ${descriptionsFile.absolutePath}")
    }

    val metadata = metadataFile.inputStream().use(MetadataSerializer::metadataFromXML)
    val descriptions = descriptionsFile.inputStream().use(MetadataSerializer::descriptionsFromXML)
    return MetadataBundle(metadata = metadata, descriptions = descriptions)
  }

  private fun loadFromJar(jarFile: File): MetadataBundle {
    if (!jarFile.isFile) {
      throw IllegalArgumentException("Jar file does not exist: ${jarFile.absolutePath}")
    }

    JarFile(jarFile).use { jar ->
      val metadata = jar.readEntry(METADATA_ENTRY_PATH, MetadataSerializer::metadataFromXML)
      val descriptions = jar.readEntry(DESCRIPTIONS_ENTRY_PATH, MetadataSerializer::descriptionsFromXML)
      return MetadataBundle(metadata = metadata, descriptions = descriptions)
    }
  }

  private fun <T> JarFile.readEntry(entryPath: String, parser: (InputStream) -> T): T {
    val entry = getJarEntry(entryPath)
                ?: throw IllegalArgumentException("Entry '$entryPath' was not found in jar '$name'")
    return getInputStream(entry).use(parser)
  }

  private fun printUsage() {
    println(
      """
      Usage:
        MetadataComparatorMain [--details|--summary-only] <jar-path>
        MetadataComparatorMain [--details|--summary-only] <path-1> <path-2>

      Mode inference:
        - one path: compare default gathered metadata with jar
        - two jar paths: compare jar vs jar
        - one jar path and one non-jar path: compare gathered metadata dir vs jar

      Output:
        - summary is printed by default
        - use --details to print full itemized diff
        - for two-path mode, left/right correspond to the first/second path arguments

      default gathered metadata location:
        ${CrawlerPaths.metadataResourceDir.absolutePath}
      """.trimIndent(),
    )
  }
}
