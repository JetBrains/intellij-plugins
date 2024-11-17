import com.intellij.psi.*

/**
 * Class does not depend on its inheritors
 */
val classDoesNotDependOnInheritorsInspection = localInspection { psiFile, inspection ->
    fun isInheritorOfPsiClass(psiType: PsiType, psiClass: PsiClass): Boolean {
        return psiClass.asPsiClassType() in psiType.getAllSuperTypes()
    }

    val notFinalClasses = psiFile.descendantsOfType<PsiClass>()
        .filter { javaClass ->
            val isFinal = javaClass.modifierList?.text?.contains("final") ?: true
            !isFinal
        }

    notFinalClasses.forEach { javaClass ->
        // check return type of called methods
        val methodCalls = javaClass.descendantsOfType<PsiCallExpression>()
            .filter { call -> call !is PsiNewExpression } // constructor will be checked by code below (reference to type)

        methodCalls.forEach { call ->
            val returnType = call.type ?: return@forEach
            if (isInheritorOfPsiClass(returnType, javaClass)) {
                inspection.registerProblem(call, "Class ${javaClass.name} must not depend on its inheritor ${returnType.canonicalText}")
            }
        }

        // references to type: parameter/variable type, return type, instanceof, etc.
        val references = javaClass.descendantsOfType<PsiJavaCodeReferenceElement>()
        references.forEach { reference ->
            val referenceType = reference.asPsiClassType()
            if (isInheritorOfPsiClass(referenceType, javaClass)) {
                inspection.registerProblem(reference, "Class ${javaClass.name} must not depend on its inheritor ${referenceType.className}")
            }
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

    val javaFile = psiFile as? PsiJavaFile ?: return@localInspection

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

    val packageName = javaFile.packageName
    val restrictedImportPackages = restrictedImportsJson.value.noImports
        .find { packageName.startsWith(it.packageName) }
        ?.restrictedImportPackages ?: return@localInspection

    javaFile.importList?.importStatements?.forEach { import: PsiImportStatement ->
        val importFqName = import.qualifiedName ?: return@forEach
        val isRestrictedImport = restrictedImportPackages.any { restrictedPackage -> importFqName.startsWith(restrictedPackage) }
        if (isRestrictedImport) {
            inspection.registerProblem(import, "$packageName must not depend on $importFqName")
        }
    }
}

/**
 * Not private method's arguments are directly passed to println: propagation of a taint value to a sink
 * (you may replace println with SQL request construction, etc.)
 */
val methodArgumentIsNotPassedToPrintlnInspection = localInspection { psiFile, inspection ->
    fun isPrivateMethod(method: PsiMethod): Boolean {
        val modifiersText = method.modifierList.text ?: return false
        return "private" in modifiersText
    }

    val notPrivateMethods = psiFile.descendantsOfType<PsiMethod>()
        .filter { method -> !isPrivateMethod(method) }

    notPrivateMethods.forEach { method ->
        val parameters = method.parameterList.parameters
        fun isReferenceToParameter(reference: PsiReference): Boolean {
            // reference.resolve is like "go to declaration"
            return reference.resolve() in parameters
        }

        val calls = method.descendantsOfType<PsiMethodCallExpression>()
        calls.forEach { call ->
            val calledMethod = call.methodExpression.reference?.resolve() as? PsiMethod ?: return@forEach
            val isPrintln = calledMethod.name == "println" && calledMethod.containingClass?.qualifiedName == "java.io.PrintStream"
            if (!isPrintln) return@forEach

            val arguments = call.argumentList.expressions
            arguments.forEach { argument ->
                val references = when(argument) {
                    is PsiReferenceExpression -> listOfNotNull(argument.reference)
                    is PsiPolyadicExpression -> argument.references.toList()
                    else -> return@forEach
                }
                references
                    .filter { reference -> isReferenceToParameter(reference) }
                    .forEach { reference ->
                        inspection.registerProblem(call, "Method parameter ${reference.canonicalText} is passed to println")
                    }
            }
        }
    }
}