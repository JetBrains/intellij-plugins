package com.intellij.protobuf.kotlin

import com.intellij.openapi.components.service
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.jvm.names.NameUtils
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbField
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbOneofDefinition
import com.intellij.protobuf.lang.psi.PbServiceDefinition
import com.intellij.protobuf.lang.psi.PbServiceMethod
import com.intellij.protobuf.lang.stub.PbSearchParameters
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.util.ArrayDeque

internal class PbKotlinImplementationSearcher : PbCodeImplementationSearcher {
    override fun findImplementationsForProtoElement(
        pbElement: PbElement,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<PsiElement> {
        return when (pbElement) {
            is PbServiceDefinition -> serviceKotlinElements(pbElement, converters)
            is PbServiceMethod -> methodKotlinElements(pbElement, converters)
            is PbMessageType -> sequence {
                yieldAll(PbKotlinGeneratedCodeSearch.dslFactoryFunctions(pbElement))
                yieldAll(PbKotlinGeneratedCodeSearch.dslExtensionFunctions(pbElement))
            }
            is PbField -> sequence {
                yieldAll(PbKotlinGeneratedCodeSearch.dslProperties(pbElement))
                yieldAll(PbKotlinGeneratedCodeSearch.dslAccessorFunctions(pbElement))
            }
            is PbOneofDefinition -> sequence {
                yieldAll(PbKotlinGeneratedCodeSearch.dslOneofProperties(pbElement))
                yieldAll(PbKotlinGeneratedCodeSearch.dslOneofAccessorFunctions(pbElement))
            }
            else -> emptySequence()
        }
    }

    override fun findDeclarationsForCodeElement(
        psiElement: PsiElement,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<PbElement> {
        val serviceTargets = when (psiElement) {
            is KtClassOrObject -> protoServicesForGrpcClass(psiElement, converters)
            is KtNamedFunction -> protoMethodsForGrpcFunction(psiElement, converters)
            else -> emptySequence()
        }
        return serviceTargets + PbKotlinGeneratedCodeSearch.protoTargetsForGeneratedKotlinElement(psiElement)
    }

    private fun serviceKotlinElements(
        serviceDefinition: PbServiceDefinition,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<KtClassOrObject> = sequence {
        val project = serviceDefinition.project
        val scope = GlobalSearchScope.projectScope(project)

        yieldAll(generatedCoroutineStubClasses(serviceDefinition, converters)
            .mapNotNull { stubClass -> stubClass.navigationElement as? KtClassOrObject })
        yieldAll(generatedCoroutineBaseClasses(serviceDefinition, converters)
            .flatMap { baseClass ->
                ClassInheritorsSearch.search(baseClass, scope, true).findAll().asSequence()
            }
            .mapNotNull { inheritor -> inheritor.navigationElement as? KtClassOrObject })
    }
        .distinct()

    private fun methodKotlinElements(
        methodDefinition: PbServiceMethod,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<KtNamedFunction> {
        val serviceDefinition = PsiTreeUtil.getParentOfType(
            methodDefinition,
            PbServiceDefinition::class.java,
            true
        ) ?: return emptySequence()
        val kotlinMethodName = methodDefinition.name
            ?.let(NameUtils::underscoreToCamelCase)
            ?: return emptySequence()

        return serviceKotlinElements(serviceDefinition, converters)
            .flatMap { implementation -> implementation.declarations.asSequence() }
            .filterIsInstance<KtNamedFunction>()
            .filter { function -> function.name == kotlinMethodName }
    }

    private fun generatedCoroutineBaseClasses(
        serviceDefinition: PbServiceDefinition,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<PsiClass> {
        return generatedGrpcKotlinClasses(serviceDefinition, converters, ::coroutineBaseFqnForJavaBase)
            .filter(::isCoroutineServiceBase)
    }

    private fun generatedCoroutineStubClasses(
        serviceDefinition: PbServiceDefinition,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<PsiClass> {
        return generatedGrpcKotlinClasses(serviceDefinition, converters, ::coroutineStubFqnForJavaBase)
            .filter(::isCoroutineClientStub)
    }

    private fun generatedGrpcKotlinClasses(
        serviceDefinition: PbServiceDefinition,
        converters: Collection<PbGeneratedCodeConverter>,
        kotlinClassFqn: (String) -> String?,
    ): Sequence<PsiClass> {
        val serviceFqn = effectiveServiceFqn(serviceDefinition) ?: return emptySequence()
        val facade = JavaPsiFacade.getInstance(serviceDefinition.project)
        val scope = GlobalSearchScope.projectScope(serviceDefinition.project)

        val javaBaseFqns = converters.asSequence()
            .map { converter -> converter.protoToCodeEntityName(serviceFqn) } +
            sequenceOf(javaBaseFqnForServiceFqn(serviceFqn))
        return javaBaseFqns
            .mapNotNull(kotlinClassFqn)
            .mapNotNull { classFqn -> facade.findClass(classFqn, scope) }
            .distinct()
    }

    private fun effectiveServiceFqn(serviceDefinition: PbServiceDefinition): String? {
        val pbFile = serviceDefinition.containingFile as? PbFile ?: return null
        val javaPackage = pbFile.options
            .firstOrNull { option -> option.optionName.text == PB_JAVA_PACKAGE_OPTION }
            ?.stringValue
            ?.asString
        return if (javaPackage == null) {
            serviceDefinition.qualifiedName?.toString()
        }
        else {
            "$javaPackage.${serviceDefinition.name.orEmpty()}"
        }
    }

    private fun coroutineBaseFqnForJavaBase(javaBaseFqn: String): String? {
        return coroutineClassFqnForJavaBase(javaBaseFqn, COROUTINE_IMPL_BASE_SUFFIX)
    }

    private fun coroutineStubFqnForJavaBase(javaBaseFqn: String): String? {
        return coroutineClassFqnForJavaBase(javaBaseFqn, COROUTINE_STUB_SUFFIX)
    }

    private fun coroutineClassFqnForJavaBase(
        javaBaseFqn: String,
        kotlinClassSuffix: String,
    ): String? {
        val javaBaseName = javaBaseFqn.substringAfterLast('.')
        val serviceName = javaBaseName.removeSuffix(JAVA_IMPL_BASE_SUFFIX)
            .takeIf { name -> name != javaBaseName && name.isNotEmpty() }
            ?: return null
        val javaContainerFqn = javaBaseFqn.substringBeforeLast('.', missingDelimiterValue = "")
        if (javaContainerFqn.isEmpty() || !javaContainerFqn.substringAfterLast('.').endsWith(GRPC_SUFFIX)) {
            return null
        }

        return "${javaContainerFqn}Kt.$serviceName$kotlinClassSuffix"
    }

    private fun javaBaseFqnForServiceFqn(serviceFqn: String): String {
        val serviceName = serviceFqn.substringAfterLast('.')
        val packageName = serviceFqn.substringBeforeLast('.', missingDelimiterValue = "")
        val prefix = packageName.takeIf { it.isNotEmpty() }?.let { "$it." }.orEmpty()
        return "$prefix$serviceName$GRPC_SUFFIX.$serviceName$JAVA_IMPL_BASE_SUFFIX"
    }

    private fun protoServicesForGrpcClass(
        coroutineClass: KtClassOrObject,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<PbServiceDefinition> {
        val lightClass = coroutineClass.toLightClass() ?: return emptySequence()
        val generatedClass = (sequenceOf(lightClass) + superClasses(lightClass))
            .firstOrNull(::isGrpcKotlinServiceClass)
            ?: return emptySequence()
        val javaBaseFqn = javaBaseFqnForCoroutineClass(generatedClass) ?: return emptySequence()
        val accessor = coroutineClass.project.service<ProtoFileAccessor>()

        val descriptorServiceName = grpcServiceName(javaBaseFqn, coroutineClass)
        if (descriptorServiceName != null) {
            val exactServices = accessor
                .findServicesByFqn(descriptorServiceName, PbSearchParameters.EXACT_MATCH)
                .toList()
            if (exactServices.isNotEmpty()) {
                return exactServices.asSequence()
            }
        }

        val converterProtoNames = converters.asSequence()
            .mapNotNull { converter ->
                val protoName = converter.codeEntityNameToProtoName(javaBaseFqn)
                protoName.takeIf { converter.protoToCodeEntityName(it) == javaBaseFqn }
            }
        val serviceName = javaBaseFqn.substringAfterLast('.').removeSuffix(JAVA_IMPL_BASE_SUFFIX)
        return (converterProtoNames + sequenceOf(serviceName))
            .flatMap { protoName -> sequenceOf(protoName, protoName.substringAfterLast('.')) }
            .distinct()
            .flatMap { protoName -> accessor.findServicesByFqn(protoName, PbSearchParameters.CONTAINS) }
            .distinct()
    }

    private fun grpcServiceName(javaBaseFqn: String, context: PsiElement): String? {
        val javaContainerFqn = javaBaseFqn.substringBeforeLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotEmpty() }
            ?: return null
        val serviceNameField = JavaPsiFacade.getInstance(context.project)
            .findClass(javaContainerFqn, GlobalSearchScope.allScope(context.project))
            ?.findFieldByName(GRPC_SERVICE_NAME_FIELD, false)
            ?: return null
        return serviceNameField.computeConstantValue() as? String
            ?: (serviceNameField.initializer as? PsiLiteralExpression)?.value as? String
    }

    private fun protoMethodsForGrpcFunction(
        function: KtNamedFunction,
        converters: Collection<PbGeneratedCodeConverter>
    ): Sequence<PbServiceMethod> {
        val implementation = PsiTreeUtil.getParentOfType(function, KtClassOrObject::class.java, true)
            ?: return emptySequence()
        val functionName = function.name ?: return emptySequence()

        return protoServicesForGrpcClass(implementation, converters)
            .flatMap { service -> service.body?.serviceMethodList?.asSequence().orEmpty() }
            .filter { method -> method.name?.let(NameUtils::underscoreToCamelCase) == functionName }
    }

    private fun javaBaseFqnForCoroutineClass(coroutineClass: PsiClass): String? {
        val coroutineClassName = coroutineClass.name ?: return null
        val serviceName = sequenceOf(COROUTINE_IMPL_BASE_SUFFIX, COROUTINE_STUB_SUFFIX)
            .mapNotNull { suffix ->
                coroutineClassName.removeSuffix(suffix)
                    .takeIf { name -> name != coroutineClassName && name.isNotEmpty() }
            }
            .firstOrNull()
            ?: return null
        val container = coroutineClass.containingClass ?: return null
        val containerFqn = container.qualifiedName ?: return null
        val javaContainerFqn = containerFqn.removeSuffix(KOTLIN_FILE_SUFFIX)
            .takeIf { name -> name != containerFqn && name.substringAfterLast('.').endsWith(GRPC_SUFFIX) }
            ?: return null

        return "$javaContainerFqn.$serviceName$JAVA_IMPL_BASE_SUFFIX"
    }

    private fun isCoroutineServiceBase(psiClass: PsiClass): Boolean {
        return psiClass.name?.endsWith(COROUTINE_IMPL_BASE_SUFFIX) == true &&
            javaBaseFqnForCoroutineClass(psiClass) != null &&
            InheritanceUtil.isInheritor(psiClass, ABSTRACT_COROUTINE_SERVER_IMPL_FQN)
    }

    private fun isCoroutineClientStub(psiClass: PsiClass): Boolean {
        return psiClass.name?.endsWith(COROUTINE_STUB_SUFFIX) == true &&
            javaBaseFqnForCoroutineClass(psiClass) != null &&
            InheritanceUtil.isInheritor(psiClass, ABSTRACT_COROUTINE_STUB_FQN)
    }

    private fun isGrpcKotlinServiceClass(psiClass: PsiClass): Boolean {
        return isCoroutineServiceBase(psiClass) || isCoroutineClientStub(psiClass)
    }

    private fun superClasses(psiClass: PsiClass): Sequence<PsiClass> = sequence {
        val queue = ArrayDeque<PsiClass>()
        val visited = mutableSetOf<PsiClass>()
        queue.addAll(psiClass.supers)

        while (queue.isNotEmpty()) {
            val candidate = queue.removeFirst()
            if (!visited.add(candidate)) continue
            yield(candidate)
            queue.addAll(candidate.supers)
        }
    }
}

private const val PB_JAVA_PACKAGE_OPTION = "java_package"
private const val GRPC_SUFFIX = "Grpc"
private const val KOTLIN_FILE_SUFFIX = "Kt"
private const val JAVA_IMPL_BASE_SUFFIX = "ImplBase"
private const val COROUTINE_IMPL_BASE_SUFFIX = "CoroutineImplBase"
private const val COROUTINE_STUB_SUFFIX = "CoroutineStub"
private const val ABSTRACT_COROUTINE_SERVER_IMPL_FQN = "io.grpc.kotlin.AbstractCoroutineServerImpl"
private const val ABSTRACT_COROUTINE_STUB_FQN = "io.grpc.kotlin.AbstractCoroutineStub"
private const val GRPC_SERVICE_NAME_FIELD = "SERVICE_NAME"
