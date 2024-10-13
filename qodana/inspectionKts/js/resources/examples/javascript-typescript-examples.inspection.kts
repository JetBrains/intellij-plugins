import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import kotlin.io.path.Path

fun isConsoleLog(call: JSCallExpression): Boolean {
    // is a call to log
    val methodReference = call.methodExpression as? JSReferenceExpression ?: return false
    if (methodReference.referenceName != "log") {
        return false
    }
    // now verify that it is console's log

    // resolve is like "go to declaration of a called function"
    val calledFunction = methodReference.reference?.resolve() as? JSFunction ?: return false
    return calledFunction.qualifiedName == "Console.log"
}

/**
 * Verify that all calls to Console.log have valid prefix (start with "log")
 */
val validConsoleLogPrefixInspection = localInspection { psiFile, inspection ->
    fun getPassedStringValue(expression: JSExpression): String? {
        // if argument is a variable (like console.log(variable)), get a value assigned to the variable
        val stringExpression = if (expression is JSReferenceExpression) {
            val referencedVariable = (expression.reference?.resolve() as? JSInitializerOwner)
            referencedVariable?.initializer
        } else {
            expression
        }

        return when(stringExpression) {
            // plain string: "log_text"
            is JSLiteralExpression -> stringExpression.stringValue
            // concatenation: "log" + "_text", simply get the whole text of concat without first and last quotation mark
            is JSBinaryExpression -> stringExpression.text.removeSuffix("\"").removePrefix("\"")
            else -> null
        }
    }

    val consoleLogCalls = psiFile.descendantsOfType<JSCallExpression>()
        .filter { call -> isConsoleLog(call) }

    consoleLogCalls.forEach { call ->
        val firstArgument = call.arguments.firstOrNull() ?: return@forEach
        val firstArgumentString = getPassedStringValue(firstArgument)
        if (firstArgumentString == null) {
            inspection.registerProblem(call, "Can't obtain string value of passed string to log, pass a variable initialized with a string value or a string directly")
            return@forEach
        }

        val isValidArgument = firstArgumentString.startsWith("log")
        if (!isValidArgument) {
            inspection.registerProblem(call, "Console.log must start with log")
        }
    }
}

/**
 * Not private function's arguments are directly passed to console.log: propagation of a taint value to a sink
 * (you may replace println with some request construction, SQL, etc.)
 */
val functionArgumentNotPassedToConsoleLogInspection = localInspection { psiFile, inspection ->
    val notPrivateFunctions = psiFile.descendantsOfType<JSFunction>()
        .filter { function -> function.accessType != JSAttributeList.AccessType.PRIVATE }

    notPrivateFunctions.forEach { function ->
        fun isReferenceToParameter(jsReferenceExpression: JSReferenceExpression): Boolean {
            // reference.resolve is like "go to declaration"
            return jsReferenceExpression.reference?.resolve() in function.parameters
        }

        val consoleLogCalls = function.descendantsOfType<JSCallExpression>()
            .filter { call -> isConsoleLog(call) }

        consoleLogCalls.forEach { call ->
            call.arguments.forEach { argument ->
                val references = when(argument) {
                    // directly passed: .log(argument)
                    is JSReferenceExpression -> listOf(argument)
                    // passed to string template: .log(`text ${argument}`)
                    is JSStringTemplateExpression -> argument.arguments.filterIsInstance<JSReferenceExpression>()
                    // concatenated string: .log("text" + argument)
                    is JSBinaryExpression -> argument.descendantsOfType<JSReferenceExpression>().toList()
                    else -> return@forEach
                }
                references
                    .filter { reference -> isReferenceToParameter(reference) }
                    .forEach { reference ->
                        inspection.registerProblem(reference, "Not private function argument is passed to console.log")
                    }
            }
        }
    }
}

/**
 * Inspection reads JSON configuration from "restricted-imports.json", file example:
 * {
 *   "noImports": [
 *     {
 *       "directory": "path1/in/project/",
 *       "restrictedImportModulePaths": ["@moduleXXX", "@moduleYYY"]
 *     },
 *     {
 *       "directory": "path2/in/project/",
 *       "restrictedImportModulePaths": ["@moduleZZZ"]
 *     }
 *   ]
 * }
 *
 * Checks that files in directories "directory" do not have imports from "restrictedImportModulePaths"
 */
val checkRestrictedImportsFromConfigFileInspection = localInspection { psiFile, inspection ->
    data class NoDependencyEntry(
        val directory: String,
        val restrictedImportModulePaths: List<String>
    )
    data class NoDependenciesJson(
        val noImports: List<NoDependencyEntry>
    )

    val jsFile = psiFile as? JSFile ?: return@localInspection

    val restrictedImportsFile = inspection.findPsiFileByRelativeToProjectPath("restricted-imports.json") ?: return@localInspection
    val restrictedImportsJson = parseJson(restrictedImportsFile.text, NoDependenciesJson::class.java, failOnUnknownProperties = false)
    when(restrictedImportsJson) {
        is JsonParseResult.Failed -> {
            //throw restrictedImportsJson.exception
            inspection.registerProblem(psiFile, "can't read restricted-imports.json")
            return@localInspection
        }
        is JsonParseResult.Success -> {}
    }


    val currentDir = psiFile.getPathRelativeToProject()?.let { Path(it) }?.parent.toString()
    val restrictedImportPackages = restrictedImportsJson.value.noImports
        .find { currentDir.startsWith(it.directory) }
        ?.restrictedImportModulePaths ?: return@localInspection

    val imports = jsFile.children.filterIsInstance<ES6ImportDeclaration>()
        .filter { import -> import.importExportPrefixKind != ES6ImportExportDeclaration.ImportExportPrefixKind.EXPORT }

    imports.forEach { import: ES6ImportDeclaration ->
        val importedModulePath = import.fromClause?.referenceText?.removePrefix("\'")?.removeSuffix("\'") ?: return@forEach
        val isRestrictedImport = restrictedImportPackages.any { restrictedPackage -> importedModulePath.startsWith(restrictedPackage) }
        if (isRestrictedImport) {
            inspection.registerProblem(import, "$currentDir must not depend on $importedModulePath")
        }
    }
}

/**
 * Check that react version defined in package.json is not lower than 18.0.0
 */
val packageJsonReactVersionInspection = localInspection { psiFile, inspection ->
    class Dependencies(
        val react: String? = null
    )
    class PackageJson(
        val dependencies: Dependencies? = null
    )

    if (psiFile.name != "package.json") return@localInspection

    val packageJson = parseJson(psiFile.text, PackageJson::class.java, failOnUnknownProperties = false)
    when(packageJson) {
        is JsonParseResult.Failed -> {
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