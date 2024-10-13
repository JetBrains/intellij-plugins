import com.intellij.json.psi.JsonProperty
import com.intellij.psi.*
import org.jetbrains.yaml.psi.YAMLKeyValue

/**
 * Check that react version defined in package.json is not lower than 18.0.0
 */
val packageJsonReactVersionInspection = localInspection { psiFile, inspection ->
    /**
     * If you want to check values of some exact JSON fields, then it's easier to describe JSON in form of class and deserialize it to an object using [parseJson]
     * like it is done in this example
     */
    data class Dependencies(
        val react: String? = null
    )
    data class PackageJson(
        val dependencies: Dependencies? = null
    )

    if (psiFile.name != "package.json") return@localInspection

    val packageJson = parseJson(psiFile.text, PackageJson::class.java, failOnUnknownProperties = false)
    when(packageJson) {
        is JsonParseResult.Failed -> {
            // you can throw exception: `throw packageJson.exception`
            inspection.registerProblem(psiFile, "Can't read ${psiFile.getPathRelativeToProject()}")
            return@localInspection
        }
        is JsonParseResult.Success -> {}
    }
    val reactVersionString = packageJson.value.dependencies?.react ?: return@localInspection
    val (major, minor, patch) = reactVersionString.filter { it != '^' || it != '~' }.split(".")
    if (major.toInt() < 18) {
        inspection.registerProblem(psiFile, "react version must not be lower than 18.0.0, current version: $reactVersionString")
    }
}

/**
 * Check that all json keys have camelCase naming
 */
val camelCaseForJsonKeysInspection = localInspection { psiFile, inspection ->
    /**
     * If definition of JSON as a class isn't enough, use JSON PSI (for example to check syntax of JSON file)
     */

    val camelCaseRegex = Regex("^[a-z]+([A-Z][a-z\\d]*)*\$")

    val notCamelCaseProperties = psiFile.descendantsOfType<JsonProperty>()
        .filter { property -> !property.name.matches(camelCaseRegex) }

    notCamelCaseProperties.forEach { property ->
        inspection.registerProblem(property.nameElement, "key must be in camelCase")
    }
}

/**
 * Check that Dependency Analysis inspection is enabled in Qodana analysis
 * https://www.jetbrains.com/help/qodana/license-audit.html
 */
val dependencyAnalysisEnabledInQodanaYamlInspection = localInspection { psiFile, inspection ->
    /**
     * If you want to check values of some exact YAML fields, then it's easier to describe YAML in form of class and deserialize it to an object using [parseYaml]
     * like it is done in this example
     */
    class IncludeItem(
        val name: String? = null
    )
    class QodanaYaml(
        val include: List<IncludeItem> = emptyList()
    )

    val isQodanaYamlFile = psiFile.name == "qodana.yaml" || psiFile.name == "qodana.yml"
    if (!isQodanaYamlFile) return@localInspection

    val qodanaYaml = parseYaml(psiFile.text, QodanaYaml::class.java, failOnUnknownProperties = false)
    when(qodanaYaml) {
        is YamlParseResult.Failed -> {
            // you can throw exception: `throw qodanaYaml.exception`
            inspection.registerProblem(psiFile, "Failed to read ${psiFile.name}")
            return@localInspection
        }
        is YamlParseResult.Success -> {}
    }

    val isDependencyAnalysisEnabled = qodanaYaml.value.include.any { include -> include.name == "CheckDependencyLicenses" }
    if (!isDependencyAnalysisEnabled) {
        inspection.registerProblem(psiFile, "Enable Qodana Dependency Analysis inspection to ensure that licenses of third-party libraries compatible with your application's license")
    }
}

/**
 * Check that docker-compose.yml doesn't use latest tags in `image`, which may lead to breaking changes in the new versions and unexpected errors
 */
val prohibitImageLatestTagInDockerComposeInspection = localInspection { psiFile, inspection ->
    /**
     * If definition of YAML as a class isn't enough, use YAML PSI
     */

    val isDockerCompose = psiFile.name.endsWith("compose.yml") || psiFile.name.endsWith("compose.yaml")
    if (!isDockerCompose) return@localInspection

    val allImageEntries = psiFile.descendantsOfType<YAMLKeyValue>()
        .filter { entry -> entry.keyText == "image" }

    allImageEntries.forEach { entry ->
        val tag = entry.valueText.substringAfter(":", missingDelimiterValue = "")
        val isLatest = tag == "" || tag == "latest"
        if (isLatest) {
            inspection.registerProblem(entry, "Provide specific tag for the image, using latest may lead to breaking changes and unexpected errors")
        }
    }
}