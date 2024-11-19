import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.psi.*

/**
 * When expression with sealed class as an argument must process all subclasses, do not use else to not miss some cases
 */
val noElseInWhenExpressionOnSealedClassInspection = localInspection { psiFile, inspection ->
    val ktWhenExpressions = psiFile.descendantsOfType<KtWhenExpression>()
    ktWhenExpressions.forEach { ktWhenExpression ->
        val elseExpression = ktWhenExpression.elseExpression ?: return@forEach
        val whenSubject = ktWhenExpression.subjectExpression ?: return@forEach
        analyze(whenSubject) {
            val classType = (whenSubject.expressionType?.expandedSymbol as? KaNamedClassSymbol) ?: return@forEach
            val isSealed = classType.modality == KaSymbolModality.SEALED
            if (isSealed) {
                inspection.registerProblem(elseExpression, "when should be exhaustive on sealed class argument, process each exact type")
            }
        }
    }
}

/**
 * Loops in suspend functions should check cancellation to achieve better cancellability
 */
val ensureActiveInLoopsOfSuspendFunctionsInspection = localInspection { psiFile, inspection ->
    fun isSuspendFunction(function: KtNamedFunction): Boolean {
        return analyze(function) {
            (function.symbol as? KaNamedFunctionSymbol)?.isSuspend ?: false
        }
    }

    val suspendFunctions = psiFile.descendantsOfType<KtNamedFunction>()
        .filter { function -> isSuspendFunction(function) }

    suspendFunctions.forEach { suspendFunction ->
        val loops = suspendFunction.descendantsOfType<KtLoopExpression>()
        loops.forEach { loop ->
            val isEnsureActivePresent = loop.descendantsOfType<KtCallExpression>()
                .any { call -> (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() == "ensureActive" }

            if (!isEnsureActivePresent) {
                inspection.registerProblem(loop, "No ensureActive in loop inside suspend function")
            }
        }
    }
}

/**
 * In ViewModel classes public suspend functions are prohibited, call suspend function on VM's CoroutineScope
 */
val noSuspendApiFunctionsInViewModelInspection = localInspection { psiFile, inspection ->
    fun isViewModel(kotlinClass: KtClass): Boolean {
        val className = kotlinClass.name ?: return false
        return "ViewModel" in className || className.endsWith("VM")
    }

    fun isNotPrivateSuspendFunction(function: KtNamedFunction): Boolean {
        val modifiersText = function.modifierList?.text ?: return false
        val isSuspend = "suspend" in modifiersText
        if (!isSuspend) return false

        val isPrivate = "private" in modifiersText
        return !isPrivate
    }

    val viewModelClasses = psiFile.descendantsOfType<KtClass>()
        .filter { kotlinClass -> isViewModel(kotlinClass) }

    viewModelClasses.forEach { viewModelClass ->
        val notPrivateSuspendFunctions = viewModelClass.body?.children?.filterIsInstance<KtNamedFunction>()
            ?.filter { function -> isNotPrivateSuspendFunction(function) }

        notPrivateSuspendFunctions?.forEach { notPrivateSuspendFunction ->
            inspection.registerProblem(notPrivateSuspendFunction.nameIdentifier, "Not private suspend functions are not allowed in ViewModel")
        }
    }
}

/**
 * Inspection reads JSON configuration from "restricted-imports.json", file example:
 * {
 *   "noImports": [
 *     {
 *       "packageName": "com.xxx",
 *       "restrictedImportPackages": ["com.yyy", "com.zzz"]
 *     },
 *     {
 *       "packageName": "com.zzz",
 *       "restrictedImportPackages": ["com.nnn"]
 *     }
 *   ]
 * }
 *
 * Checks that files in packages "packageNames" do not have imports from "notAllowedImportedPackages"
 */
val checkRestrictedImportsFromConfigFileInspection = localInspection { psiFile, inspection ->
    data class NoDependencyEntry(
        val packageName: String,
        val restrictedImportPackages: List<String>
    )
    data class NoDependenciesJson(
        val noImports: List<NoDependencyEntry>
    )

    val ktFile = psiFile as? KtFile ?: return@localInspection

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

    val packageName = ktFile.packageFqName.asString()
    val restrictedImportPackages = restrictedImportsJson.value.noImports
        .find { packageName.startsWith(it.packageName) }
        ?.restrictedImportPackages ?: return@localInspection

    ktFile.importDirectives.forEach { import ->
        val importFqName = import.importedFqName?.asString() ?: return@forEach
        if (restrictedImportPackages.any { restrictedPackage -> importFqName.startsWith(restrictedPackage) }) {
            inspection.registerProblem(import, "$packageName must not depend on $importFqName")
        }
    }
}


/**
 * Not private function's arguments are directly passed to println: propagation of a taint value to a sink
 * (you may replace println with SQL request construction, etc.)
 */
val functionArgumentIsNotPassedToPrintlnInspection = localInspection { psiFile, inspection ->
    fun isPrivateFunction(function: KtNamedFunction): Boolean {
      val modifiersText = function.modifierList?.text ?: return false
      return "private" in modifiersText
    }

    val notPrivateFunctions = psiFile.descendantsOfType<KtNamedFunction>()
      .filter { function -> !isPrivateFunction(function) }

    notPrivateFunctions.forEach { function ->
        val parameters = function.valueParameters

        fun isReferenceToParameter(reference: KtNameReferenceExpression): Boolean {
            // .reference.resolve is like "go to declaration"
            return reference.reference?.resolve() in parameters
        }

        val callsInFunctionBody = function.bodyBlockExpression?.children?.filterIsInstance<KtCallExpression>() ?: return@forEach
        callsInFunctionBody.forEach { call ->
            val isPrintln = (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() == "println"
            if (!isPrintln) return@forEach

            val arguments = call.valueArguments
            arguments.forEach { argument ->
                // simple println(x)
                val plainReference = (argument.getArgumentExpression() as? KtNameReferenceExpression)
                // string templates: println("${x}")
                val referencesInStringTemplate = argument.stringTemplateExpression?.descendantsOfType<KtNameReferenceExpression>()?.toList() ?: emptyList()

                (referencesInStringTemplate + listOfNotNull(plainReference))
                    .filter { reference -> isReferenceToParameter(reference) }
                    .forEach { reference ->
                        inspection.registerProblem(reference, "Method parameter ${reference.getReferencedName()} is passed to println")
                    }
            }
        }
    }
}