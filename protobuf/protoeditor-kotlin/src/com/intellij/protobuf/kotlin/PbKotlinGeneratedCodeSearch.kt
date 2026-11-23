package com.intellij.protobuf.kotlin

import com.intellij.protobuf.jvm.PbJavaFindUsagesHandlerFactory
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbOneofDefinition
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.psi.util.PbPsiUtil
import com.intellij.protobuf.shared.gencode.ProtoFromSourceComments
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.QualifiedName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUserType

internal object PbKotlinGeneratedCodeSearch {
    private val dslListOperatorNames = setOf("add", "plusAssign", "addAll", "set", "clear")
    private val dslMapOperatorNames = setOf("put", "set", "remove", "putAll", "clear")
    private val dslExtensionFunctionNames = setOf(
        "get", "contains", "clear", "setExtension", "set", "add", "plusAssign", "addAll"
    )

    fun javaGeneratedElements(symbol: PbSymbol): List<PsiElement> {
        val converter = PbJavaFindUsagesHandlerFactory.ProtoToJavaConverter(symbol.pbFile)
        symbol.accept(converter)
        return converter.results?.toList().orEmpty()
    }

    fun dslFactoryFunctions(message: PbMessageType): List<KtNamedFunction> {
        val messageName = message.name ?: return emptyList()
        val functionName = PbKotlinGeneratedNames.dslFactoryNameForMessage(messageName)

        return generatedKotlinFilesForMessage(message)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java) }
            .filter { function -> function.name == functionName && isDslFactoryFunction(function) }
    }

    fun dslCopyFunctions(message: PbMessageType): List<KtNamedFunction> {
        return generatedKotlinFilesForMessage(message)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java) }
            .filter { function ->
                isDslCopyFunction(function) &&
                    protoTargetsForDslCopy(function).any { target -> target == message }
            }
    }

    fun dslExtensionFunctions(message: PbMessageType): List<KtNamedFunction> {
        return generatedKotlinFilesForMessage(message)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java) }
            .filter { function ->
                protoTargetsForDslExtensionFunction(function).any { target -> target == message }
            }
    }

    fun dslProperties(field: PbField): List<KtProperty> {
        val propertyName = PbKotlinGeneratedNames.dslPropertyNameForField(field) ?: return emptyList()
        val owner = field.symbolOwner
        if (!PbPsiUtil.isMessageElement(owner)) {
            return emptyList()
        }

        val message = owner as? PbMessageType ?: return emptyList()

        return generatedKotlinFilesForMessage(message)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java) }
            .filter { property ->
                property.name == propertyName || generatedOrNullFieldName(property) == propertyName
            }
    }

    fun dslAccessorFunctions(field: PbField): List<KtNamedFunction> {
        val owner = field.symbolOwner as? PbMessageType ?: return emptyList()

        return generatedKotlinFilesForMessage(owner)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java) }
            .filter { function ->
                protoTargetsForGeneratedKotlinFunction(function).any { target -> target == field }
            }
    }

    fun dslOneofProperties(oneof: PbOneofDefinition): List<KtProperty> {
        val owner = oneof.symbolOwner as? PbMessageType ?: return emptyList()

        return generatedKotlinFilesForMessage(owner)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java) }
            .filter { property ->
                protoTargetsForDslProperty(property).any { target -> target == oneof }
            }
    }

    fun dslOneofAccessorFunctions(oneof: PbOneofDefinition): List<KtNamedFunction> {
        val owner = oneof.symbolOwner as? PbMessageType ?: return emptyList()

        return generatedKotlinFilesForMessage(owner)
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java) }
            .filter { function ->
                protoTargetsForGeneratedKotlinFunction(function).any { target -> target == oneof }
            }
    }

    fun protoTargetsForGeneratedKotlinElement(element: PsiElement): Sequence<PbElement> {
        return when (element) {
            is KtNamedFunction -> protoTargetsForGeneratedKotlinFunction(element)
            is KtProperty -> protoTargetsForDslProperty(element)
            else -> emptySequence()
        }.distinct()
    }

    private fun generatedKotlinFilesForMessage(message: PbMessageType): List<KtFile> {
        val messageName = topLevelMessageName(message) ?: return emptyList()
        val generatedFileName = "${messageName}Kt.kt"
        val project = message.project
        val scope = GlobalSearchScope.allScope(project)
        val psiManager = PsiManager.getInstance(project)

        return FilenameIndex.getVirtualFilesByName(generatedFileName, scope)
            .mapNotNull { virtualFile -> psiManager.findFile(virtualFile) as? KtFile }
            .filter { file -> file.packageFqName.asString() == javaPackageName(message) }
    }

    private fun javaPackageName(symbol: PbSymbol): String? {
        val javaElement = javaGeneratedElements(symbol).firstOrNull() ?: return null
        return (javaElement.containingFile as? PsiJavaFile)?.packageName
    }

    private fun protoTargetsForGeneratedKotlinFunction(function: KtNamedFunction): Sequence<PbElement> {
        val oneofTargets = protoTargetsForDslOneofAccessor(function).toList()
        if (oneofTargets.isNotEmpty()) {
            return oneofTargets.asSequence()
        }

        return when {
            isDslExtensionFunction(function) -> protoTargetsForDslExtensionFunction(function)
            isDslCopyFunction(function) -> protoTargetsForDslCopy(function)
            dslPropertyNameForAccessorFunction(function) != null -> protoTargetsForDslAccessor(function)
            dslCollectionPropertyName(function) != null -> protoTargetsForDslCollectionOperator(function)
            isDslFactoryFunction(function) -> protoTargetsForDslFactory(function)
            else -> emptySequence()
        }
    }

    private fun isDslExtensionFunction(function: KtNamedFunction): Boolean {
        if (function.name !in dslExtensionFunctionNames || !function.isInsideGeneratedDslClass()) {
            return false
        }

        val parameterTypes = function.valueParameters.mapNotNull { parameter -> parameter.typeReference?.text }
        val receiverType = function.receiverTypeReference?.text
        return parameterTypes.any { type -> type.contains("ExtensionLite<") } ||
            receiverType?.contains("ExtensionList<") == true
    }

    private fun protoTargetsForDslExtensionFunction(function: KtNamedFunction): Sequence<PbElement> {
        if (!isDslExtensionFunction(function)) {
            return emptySequence()
        }

        val ktFile = function.containingFile as? KtFile ?: return emptySequence()
        val messageNames = function.kotlinDslObjectMessageNames()
        val messageName = messageNames.lastOrNull() ?: return emptySequence()
        val factoryName = PbKotlinGeneratedNames.dslFactoryNameForMessage(messageName)
        val mappedTargets = PsiTreeUtil.findChildrenOfType(ktFile, KtNamedFunction::class.java)
            .asSequence()
            .filter { candidate -> candidate.name == factoryName && isDslFactoryFunction(candidate) }
            .flatMap(::protoTargetsForDslFactory)
            .filterIsInstance<PbMessageType>()
            .toList()
        if (mappedTargets.isNotEmpty()) {
            return mappedTargets.asSequence()
        }

        val pbFile = protoFileFromSourceComments(function) ?: return emptySequence()
        return protoSymbols(pbFile, messageNames.joinToString(separator = "."))
            .filterIsInstance<PbMessageType>()
    }

    private fun isDslFactoryFunction(function: KtNamedFunction): Boolean {
        if (function.receiverTypeReference != null || function.isInsideDslClass()) {
            return false
        }

        return function.hasDslBlockParameterForContainingFile()
    }

    private fun isDslCopyFunction(function: KtNamedFunction): Boolean {
        return function.name == "copy" &&
            function.receiverTypeReference != null &&
            !function.isInsideDslClass() &&
            function.hasDslBlockParameterForContainingFile()
    }

    private fun KtNamedFunction.hasDslBlockParameterForContainingFile(): Boolean {
        val ktFile = containingFile as? KtFile ?: return false
        val generatedContainerName = ktFile.name.removeSuffix(".kt")
        val generatedContainerQualifier = "$generatedContainerName."

        return valueParameters.any { parameter ->
            val typeText = parameter.typeReference?.text ?: return@any false
            val hasGeneratedContainer = typeText.startsWith(generatedContainerQualifier) ||
                typeText.contains(".$generatedContainerQualifier")
            hasGeneratedContainer && typeText.contains("Dsl.() ->")
        }
    }

    private fun KtNamedFunction.isInsideDslClass(): Boolean {
        return generateSequence(parent) { element -> element.parent }
            .filterIsInstance<KtClassOrObject>()
            .any { classOrObject -> classOrObject.name == "Dsl" }
    }

    private fun topLevelMessageName(message: PbMessageType): String? {
        var current = message
        while (current.symbolOwner is PbMessageType) {
            current = current.symbolOwner as PbMessageType
        }

        return current.name
    }

    private fun protoTargetsForDslFactory(function: KtNamedFunction): Sequence<PbElement> {
        val returnTypeTargets = protoTargetsForFactoryReturnType(function).toList()

        val ktFile = function.containingFile as? KtFile ?: return emptySequence()
        val packageName = ktFile.packageFqName.asString().takeIf { it.isNotEmpty() } ?: return emptySequence()
        val messageClassName = function.protoMessageNamesForDslFactory().joinToString(separator = ".")
        val messageClass = JavaPsiFacade.getInstance(function.project)
            .findClass("$packageName.$messageClassName", GlobalSearchScope.allScope(function.project))
            ?: return returnTypeTargets
                .takeIf { it.isNotEmpty() }
                ?.asSequence()
                ?: protoMessagesForDslFactoryFromSourceComments(function)

        val javaMappedTargets = (returnTypeTargets.asSequence() + protoTargets(messageClass)).toList()
        if (javaMappedTargets.isNotEmpty()) {
            return javaMappedTargets.asSequence()
        }

        return protoMessagesForDslFactoryFromSourceComments(function)
    }

    private fun protoTargetsForDslCopy(function: KtNamedFunction): Sequence<PbElement> {
        val returnTypeTargets = protoTargetsForFactoryReturnType(function).toList()
        if (returnTypeTargets.isNotEmpty()) {
            return returnTypeTargets.asSequence()
        }

        val pbFile = protoFileFromSourceComments(function) ?: return emptySequence()
        val messageName = function.receiverTypeReference
            ?.text
            ?.substringBefore("<")
            ?.split('.')
            ?.filter { name -> name.isNotEmpty() }
            ?.joinToString(separator = ".")
            ?: return emptySequence()

        return protoSymbols(pbFile, messageName).filterIsInstance<PbMessageType>()
    }

    private fun protoTargetsForFactoryReturnType(function: KtNamedFunction): Sequence<PbElement> {
        val returnType = function.typeReference?.typeElement ?: return emptySequence()
        val resolvedTargets = ((returnType as? KtUserType)
            ?: PsiTreeUtil.findChildOfType(returnType, KtUserType::class.java))
            ?.referenceExpression
            ?.references
            ?.asSequence()
            ?.mapNotNull { reference -> reference.resolve() }
            ?.flatMap(::protoTargets)
            .orEmpty()
            .toList()
        if (resolvedTargets.isNotEmpty()) {
            return resolvedTargets.asSequence()
        }

        val ktFile = function.containingFile as? KtFile ?: return emptySequence()
        val packageName = ktFile.packageFqName.asString().takeIf { it.isNotEmpty() } ?: return emptySequence()
        val returnTypePaths = listOfNotNull(
            PsiTreeUtil.findChildrenOfType(returnType, KtUserType::class.java)
                .mapNotNull { userType -> userType.referencedName }
                .takeIf { it.isNotEmpty() },
            function.typeReference?.text
                ?.substringBefore("<")
                ?.split('.')
                ?.filter { name -> name.isNotEmpty() }
                ?.takeIf { it.isNotEmpty() }
        )

        return returnTypePaths
            .asSequence()
            .mapNotNull { path -> findJavaClassForKotlinTypePath(function, packageName, path) }
            .flatMap(::protoTargets)
    }

    private fun findJavaClassForKotlinTypePath(element: PsiElement, packageName: String, path: List<String>): PsiClass? {
        val packagePath = packageName.split('.')
        val classPath = path
            .drop(packagePath.size)
            .takeIf { path.take(packagePath.size) == packagePath && it.isNotEmpty() }
            ?: path
        val topLevelClass = JavaPsiFacade.getInstance(element.project)
            .findClass("$packageName.${classPath.first()}", GlobalSearchScope.allScope(element.project))
            ?: return null

        return classPath.drop(1).fold(topLevelClass as PsiClass?) { currentClass, nestedName ->
            currentClass?.findInnerClassByName(nestedName, false)
        }
    }

    private fun protoTargetsForDslProperty(property: KtProperty): Sequence<PbElement> {
        val fieldName = property.name ?: return emptySequence()
        val oneofName = fieldName.removeSuffix("Case").takeIf { it != fieldName }
        if (oneofName != null && property.isInsideGeneratedDslClass()) {
            val oneofTargets = protoOneofsForDslElement(property, oneofName).toList()
            if (oneofTargets.isNotEmpty()) {
                return oneofTargets.asSequence()
            }

            val sourceCommentTargets = protoOneofsForDslElementFromSourceComments(property, oneofName).toList()
            if (sourceCommentTargets.isNotEmpty()) {
                return sourceCommentTargets.asSequence()
            }
        }

        val directTargets = protoTargetsForDslFieldProperty(property, fieldName).toList()
        if (directTargets.isNotEmpty()) {
            return directTargets.asSequence()
        }

        val orNullFieldName = generatedOrNullFieldName(property) ?: return emptySequence()
        return protoTargetsForDslFieldProperty(property, orNullFieldName)
    }

    private fun protoTargetsForDslFieldProperty(
        property: KtProperty,
        fieldName: String
    ): Sequence<PbElement> {
        val orBuilderTargets = protoTargetsForOrBuilderProperty(property, fieldName).toList()
        if (orBuilderTargets.isNotEmpty()) {
            return orBuilderTargets.asSequence()
        }

        val messageClassNames = property.kotlinDslObjectMessageNames()
        val messageClass = findGeneratedJavaClassForDslElement(property, messageClassNames)

        val javaMappedTargets = messageClass
            ?.findInnerClassByName("Builder", false)
            ?.let { builderClass ->
                PbKotlinGeneratedNames.builderAccessorNamesForDslField(fieldName)
                    .asSequence()
                    .mapNotNull { methodName -> builderClass.findMethodsByName(methodName, false).firstOrNull() }
                    .flatMap(::protoTargets)
                    .filterIsInstance<PbField>()
                    .toList()
            }
            .orEmpty()
        if (javaMappedTargets.isNotEmpty()) {
            return javaMappedTargets.asSequence()
        }

        val kotlinMappedTargets = protoFieldsForDslElement(property, fieldName).toList()
        if (kotlinMappedTargets.isNotEmpty()) {
            return kotlinMappedTargets.asSequence()
        }

        return protoFieldsForDslElementFromSourceComments(property, fieldName)
    }

    private fun protoTargetsForOrBuilderProperty(
        property: KtProperty,
        fieldName: String
    ): Sequence<PbElement> {
        val receiverType = property.receiverTypeReference?.typeElement ?: return emptySequence()
        val receiverTypes = (listOfNotNull(receiverType as? KtUserType) +
            PsiTreeUtil.findChildrenOfType(receiverType, KtUserType::class.java))
        val receiverTypeNames = receiverTypes.mapNotNull { userType -> userType.referencedName }
        if (receiverTypeNames.firstOrNull()?.endsWith("OrBuilder") != true) {
            return emptySequence()
        }

        val ktFile = property.containingFile as? KtFile ?: return emptySequence()
        val generatedContainerName = ktFile.name.removeSuffix("Kt.kt")
        if (receiverTypeNames.none { name ->
                name == generatedContainerName || name == "${generatedContainerName}OrBuilder"
            }) {
            return emptySequence()
        }

        val receiverClass = receiverTypes
            .asReversed()
            .asSequence()
            .mapNotNull { userType -> userType.referenceExpression }
            .flatMap { referenceExpression -> referenceExpression.references.asSequence() }
            .mapNotNull { reference -> reference.resolve() as? PsiClass }
            .firstOrNull()
            ?: return emptySequence()
        val getterName = "get${fieldName.replaceFirstChar { it.uppercaseChar() }}"

        return receiverClass.findMethodsByName(getterName, false)
            .asSequence()
            .flatMap(::protoTargets)
            .filterIsInstance<PbField>()
    }

    private fun generatedOrNullFieldName(property: KtProperty): String? {
        val propertyName = property.name ?: return null
        val fieldName = propertyName.removeSuffix("OrNull")
            .takeIf { it != propertyName && it.isNotEmpty() }
            ?: return null
        if (property.typeReference?.text?.trim()?.endsWith('?') != true) {
            return null
        }

        val receiverType = property.receiverTypeReference?.typeElement ?: return null
        val receiverTypeNames = (listOfNotNull(receiverType as? KtUserType) +
            PsiTreeUtil.findChildrenOfType(receiverType, KtUserType::class.java))
            .mapNotNull { userType -> userType.referencedName }
        val isDslReceiver = property.isInsideGeneratedDslClass() && "Dsl" in receiverTypeNames
        val isOrBuilderReceiver = receiverTypeNames.firstOrNull()?.endsWith("OrBuilder") == true

        return fieldName.takeIf { isDslReceiver || isOrBuilderReceiver }
    }

    private fun protoTargetsForDslAccessor(function: KtNamedFunction): Sequence<PbElement> {
        val propertyName = dslPropertyNameForAccessorFunction(function) ?: return emptySequence()
        val messageClassNames = function.kotlinDslObjectMessageNames()
        val messageClass = findGeneratedJavaClassForDslElement(function, messageClassNames)
        val javaMappedTargets = messageClass
            ?.findInnerClassByName("Builder", false)
            ?.findMethodsByName(function.name.orEmpty(), false)
            ?.asSequence()
            ?.flatMap(::protoTargets)
            ?.filterIsInstance<PbField>()
            ?.toList()
            .orEmpty()
        if (javaMappedTargets.isNotEmpty()) {
            return javaMappedTargets.asSequence()
        }

        val kotlinMappedTargets = protoFieldsForDslElement(function, propertyName).toList()
        if (kotlinMappedTargets.isNotEmpty()) {
            return kotlinMappedTargets.asSequence()
        }

        return protoFieldsForDslElementFromSourceComments(function, propertyName)
    }

    private fun protoTargetsForDslOneofAccessor(function: KtNamedFunction): Sequence<PbElement> {
        if (!function.isInsideGeneratedDslClass()) {
            return emptySequence()
        }

        val functionName = function.name ?: return emptySequence()
        val oneofName = functionName
            .removePrefix("clear")
            .takeIf { it != functionName && it.isNotEmpty() }
            ?.replaceFirstChar { it.lowercaseChar() }
            ?: return emptySequence()
        val kotlinMappedTargets = protoOneofsForDslElement(function, oneofName).toList()
        if (kotlinMappedTargets.isNotEmpty()) {
            return kotlinMappedTargets.asSequence()
        }

        return protoOneofsForDslElementFromSourceComments(function, oneofName)
    }

    private fun protoTargetsForDslCollectionOperator(function: KtNamedFunction): Sequence<PbElement> {
        val propertyName = dslCollectionPropertyName(function) ?: return emptySequence()
        val kotlinMappedTargets = protoFieldsForDslElement(function, propertyName).toList()
        if (kotlinMappedTargets.isNotEmpty()) {
            return kotlinMappedTargets.asSequence()
        }

        return protoFieldsForDslElementFromSourceComments(function, propertyName)
    }

    private fun dslCollectionPropertyName(function: KtNamedFunction): String? {
        if (!function.isInsideGeneratedDslClass()) {
            return null
        }

        val receiverType = function.receiverTypeReference?.typeElement ?: return null
        val receiverTypeNames = (listOfNotNull(receiverType as? KtUserType) +
            PsiTreeUtil.findChildrenOfType(receiverType, KtUserType::class.java))
            .mapNotNull { userType -> userType.referencedName }
        val operatorNames = when {
            "DslList" in receiverTypeNames -> dslListOperatorNames
            "DslMap" in receiverTypeNames -> dslMapOperatorNames
            else -> return null
        }
        if (function.name !in operatorNames) {
            return null
        }

        val proxyName = receiverTypeNames.lastOrNull { name -> name.endsWith("Proxy") }
            ?: return null
        val dslClass = generateSequence(function.parent) { element -> element.parent }
            .filterIsInstance<KtClassOrObject>()
            .firstOrNull { classOrObject -> classOrObject.name == "Dsl" }
            ?: return null

        return dslClass.declarations
            .filterIsInstance<KtProperty>()
            .singleOrNull { property -> property.typeReference?.text?.contains(proxyName) == true }
            ?.name
    }

    private fun dslPropertyNameForAccessorFunction(function: KtNamedFunction): String? {
        if (!function.isInsideGeneratedDslClass()) {
            return null
        }

        val functionName = function.name ?: return null
        return PbKotlinGeneratedNames.dslPropertyNameForAccessorFunction(functionName)
    }

    private fun PsiElement.isInsideGeneratedDslClass(): Boolean {
        val ktFile = containingFile as? KtFile ?: return false
        val enclosingClasses = generateSequence(parent) { element -> element.parent }
            .filterIsInstance<KtClassOrObject>()
            .toList()
        if (enclosingClasses.none { classOrObject -> classOrObject.name == "Dsl" }) {
            return false
        }

        val generatedContainerName = enclosingClasses
            .asReversed()
            .firstOrNull { classOrObject -> classOrObject.name?.endsWith("Kt") == true }
            ?.name
            ?: return false
        return ktFile.name == "$generatedContainerName.kt"
    }

    private fun findGeneratedJavaClassForDslElement(
        element: PsiElement,
        messageClassNames: List<String>
    ): PsiClass? {
        val messageClassName = messageClassNames
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ".")
            ?: return null
        val ktFile = element.containingFile as? KtFile ?: return null
        val packageName = ktFile.packageFqName.asString().takeIf { it.isNotEmpty() } ?: return null
        val directClass = JavaPsiFacade.getInstance(element.project)
            .findClass("$packageName.$messageClassName", GlobalSearchScope.allScope(element.project))
        if (directClass != null) {
            return directClass
        }

        return findGeneratedJavaClassForDslObject(element, messageClassNames.lastOrNull())
    }

    private fun findGeneratedJavaClassForDslObject(element: PsiElement, messageName: String?): PsiClass? {
        if (messageName == null) {
            return null
        }

        val ktFile = element.containingFile as? KtFile ?: return null
        val factoryName = PbKotlinGeneratedNames.dslFactoryNameForMessage(messageName)

        return PsiTreeUtil.findChildrenOfType(ktFile, KtNamedFunction::class.java)
            .asSequence()
            .filter { function -> function.name == factoryName }
            .flatMap { function -> protoTargetsForDslFactory(function) }
            .filterIsInstance<PbMessageType>()
            .flatMap { message -> javaGeneratedElements(message).asSequence() }
            .filterIsInstance<PsiClass>()
            .firstOrNull()
    }

    private fun protoFieldsForDslElement(element: PsiElement, propertyName: String): Sequence<PbField> {
        val ktFile = element.containingFile as? KtFile ?: return emptySequence()
        val messageName = element.kotlinDslObjectMessageNames().lastOrNull() ?: return emptySequence()
        val factoryName = PbKotlinGeneratedNames.dslFactoryNameForMessage(messageName)

        return PsiTreeUtil.findChildrenOfType(ktFile, KtNamedFunction::class.java)
            .asSequence()
            .filter { function -> function.name == factoryName }
            .flatMap { function -> protoTargetsForDslFactory(function) }
            .filterIsInstance<PbMessageType>()
            .flatMap { message -> PsiTreeUtil.findChildrenOfType(message, PbField::class.java).asSequence() }
            .filter { field -> PbKotlinGeneratedNames.dslPropertyNameForField(field) == propertyName }
    }

    private fun protoOneofsForDslElement(
        element: PsiElement,
        oneofName: String
    ): Sequence<PbOneofDefinition> {
        val ktFile = element.containingFile as? KtFile ?: return emptySequence()
        val messageName = element.kotlinDslObjectMessageNames().lastOrNull() ?: return emptySequence()
        val factoryName = PbKotlinGeneratedNames.dslFactoryNameForMessage(messageName)

        return PsiTreeUtil.findChildrenOfType(ktFile, KtNamedFunction::class.java)
            .asSequence()
            .filter { function -> function.name == factoryName }
            .flatMap { function -> protoTargetsForDslFactory(function) }
            .filterIsInstance<PbMessageType>()
            .flatMap { message -> message.getSymbols(PbOneofDefinition::class.java).asSequence() }
            .filter { oneof ->
                oneof.name?.let(PbKotlinGeneratedNames::dslPropertyNameForField) == oneofName
            }
    }

    private fun protoMessagesForDslFactoryFromSourceComments(function: KtNamedFunction): Sequence<PbMessageType> {
        val pbFile = protoFileFromSourceComments(function) ?: return emptySequence()
        val returnTypeNames = function.typeReference
            ?.text
            ?.substringBefore('<')
            ?.split('.')
            ?.map { name -> name.trim('`') }
            ?.filter { name -> name.isNotEmpty() }
            .orEmpty()
        val returnTypeTargets = returnTypeNames.indices
            .asSequence()
            .flatMap { startIndex ->
                protoSymbols(pbFile, returnTypeNames.drop(startIndex).joinToString(separator = "."))
            }
            .filterIsInstance<PbMessageType>()
            .toList()
        if (returnTypeTargets.isNotEmpty()) {
            return returnTypeTargets.asSequence()
        }

        val messageName = function.protoMessageNamesForDslFactory().joinToString(separator = ".")

        return protoSymbols(pbFile, messageName).filterIsInstance<PbMessageType>()
    }

    private fun protoFieldsForDslElementFromSourceComments(
        element: PsiElement,
        propertyName: String
    ): Sequence<PbField> {
        val pbFile = protoFileFromSourceComments(element) ?: return emptySequence()
        val messageName = element.kotlinDslObjectMessageNames()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ".")
            ?: return emptySequence()

        return protoSymbols(pbFile, messageName)
            .filterIsInstance<PbMessageType>()
            .flatMap { message -> message.getSymbols(PbField::class.java).asSequence() }
            .filter { field -> PbKotlinGeneratedNames.dslPropertyNameForField(field) == propertyName }
    }

    private fun protoOneofsForDslElementFromSourceComments(
        element: PsiElement,
        oneofName: String
    ): Sequence<PbOneofDefinition> {
        val pbFile = protoFileFromSourceComments(element) ?: return emptySequence()
        val messageName = element.kotlinDslObjectMessageNames()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ".")
            ?: return emptySequence()

        return protoSymbols(pbFile, messageName)
            .filterIsInstance<PbMessageType>()
            .flatMap { message -> message.getSymbols(PbOneofDefinition::class.java).asSequence() }
            .filter { oneof ->
                oneof.name?.let(PbKotlinGeneratedNames::dslPropertyNameForField) == oneofName
            }
    }

    private fun protoFileFromSourceComments(element: PsiElement): PbFile? {
        val ktFile = element.containingFile as? KtFile ?: return null
        return ProtoFromSourceComments.findProtoOfGeneratedCode("//", ktFile)
    }

    private fun protoSymbols(pbFile: PbFile, localDottedName: String): Sequence<PbSymbol> {
        val qualifiedName = pbFile.packageQualifiedName
            .append(QualifiedName.fromDottedString(localDottedName))

        return pbFile.localQualifiedSymbolMap[qualifiedName]
            .orEmpty()
            .asSequence()
            .filterIsInstance<PbSymbol>()
    }

    private fun PsiElement.kotlinDslObjectMessageNames(): List<String> {
        return generateSequence(parent) { it.parent }
            .filterIsInstance<KtClassOrObject>()
            .mapNotNull { classOrObject -> classOrObject.name }
            .filter { name -> name.endsWith("Kt") }
            .map { name -> name.removeSuffix("Kt") }
            .toList()
            .asReversed()
    }

    private fun KtNamedFunction.protoMessageNamesForDslFactory(): List<String> {
        val containingMessageNames = kotlinDslObjectMessageNames()
        if (containingMessageNames.isNotEmpty()) {
            val functionName = name ?: return emptyList()
            return containingMessageNames + PbKotlinGeneratedNames.messageClassNameForDslFactory(functionName)
        }

        val ktFile = containingFile as? KtFile ?: return emptyList()
        return listOf(ktFile.name.removeSuffix("Kt.kt"))
    }

    private fun protoTargets(element: PsiElement): Sequence<PbElement> {
        return PbJavaGotoDeclarationHandler
            .findProtoDeclarationForResolvedJavaElement(element)
            ?.asSequence()
            .orEmpty()
            .filterIsInstance<PbElement>()
    }
}
