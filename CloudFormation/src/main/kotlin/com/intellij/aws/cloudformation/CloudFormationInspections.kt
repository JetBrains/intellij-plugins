@file:Suppress("IfThenToSafeAccess", "RedundantUnitExpression")

package com.intellij.aws.cloudformation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.common.primitives.Floats
import com.intellij.aws.cloudformation.CloudFormationBundle.message
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType.Companion.isCustomResourceType
import com.intellij.aws.cloudformation.metadata.awsServerlessFunction
import com.intellij.aws.cloudformation.metadata.awsServerlessNamePrefix
import com.intellij.aws.cloudformation.model.*
import com.intellij.aws.cloudformation.references.CloudFormationEntityReference
import com.intellij.aws.cloudformation.references.CloudFormationMappingFirstLevelKeyReference
import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference
import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Nls
import org.jetbrains.yaml.psi.impl.YAMLScalarImpl
import java.util.regex.Pattern

class CloudFormationInspections private constructor(val parsed: CloudFormationParsedFile): CfnVisitor() {
  val problems: MutableList<CloudFormationProblem> = mutableListOf()
  val references: Multimap<PsiElement, CloudFormationReferenceBase> = ArrayListMultimap.create()

  private val numbersPattern = Pattern.compile("^[0-9]+$")!!

  private fun addReference(reference: CloudFormationReferenceBase) {
    references.put(reference.element, reference)
  }

  private fun addEntityReference(element: CfnScalarValueNode, sections: Collection<CloudFormationSection>, excludeFromCompletion: Collection<String>? = null, referenceValue: String? = null) {
    val psiElement = parsed.getPsiElement(element)
    val entityReference = CloudFormationEntityReference(psiElement, sections, excludeFromCompletion, referenceValue = referenceValue)

    val scalarImpl = psiElement as? YAMLScalarImpl
    if (scalarImpl != null && scalarImpl.contentRanges.isNotEmpty()) {
      val startOffset: Int = scalarImpl.contentRanges.first().startOffset
      val endOffset: Int = scalarImpl.contentRanges.last().endOffset

      entityReference.rangeInElement = TextRange(startOffset, endOffset)
    }

    addReference(entityReference)
  }

/*
  private fun addProblem(element: PsiElement, description: String) {
    problems.add(Problem(element, description))
  }
*/

  private fun addProblem(element: CfnNode, @Nls description: String) {
    // TODO check psi element mapping not exists
    val psiElement = if (element is CfnNamedNode && element.name != null) {
      parsed.getPsiElement(element.name)
    }
    else {
      parsed.getPsiElement(element)
    }

    problems.add(CloudFormationProblem(psiElement, description))
  }

  /*private fun addProblemOnNameElement(property: JsonProperty, description: String) {
    addProblem(
        if (property.firstChild != null) property.firstChild else property,
        description)
  }
*/

  private var currentResource: CfnResourceNode? = null

  override fun function(function: CfnFunctionNode) {
    val arg0 = function.args.getOrNull(0)
    val arg1 = function.args.getOrNull(1)

    // TODO make it sealed when some time in the future
    @Suppress("UNUSED_VARIABLE")
    val _used_to_enforce_exhaustive_check: Unit = when (function.functionId) {
      CloudFormationIntrinsicFunction.Ref -> {
        if (function.args.size != 1 || arg0 !is CfnScalarValueNode) {
          addProblem(function, message("reference.expects.one.string.argument"))
        }
        else {
          val arg0WithoutVersionOrAlias = arg0.value.removeSuffix(".Version").removeSuffix(".Alias")
          val resourceNodeWithoutVersionOrAlias = CloudFormationResolve.resolveResource(
            parsed, arg0WithoutVersionOrAlias)

          val resourceNodeParent = function.parentOfType<CfnResourceNode>(parsed)
          val excluded = resourceNodeParent?.let { it.name?.value }?.let { listOf(it) } ?: emptyList()

          when {
            CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(arg0.value) -> Unit

            // Disgusting hack from serverless spec
            // https://github.com/awslabs/serverless-application-model/blob/develop/versions/2016-10-31.md#referencing-lambda-version--alias-resources
            resourceNodeWithoutVersionOrAlias != null && resourceNodeWithoutVersionOrAlias.isAwsServerlessFunctionWithAutoPublishAlias() &&
            arg0WithoutVersionOrAlias != arg0.value -> {
              addEntityReference(arg0, CloudFormationSection.ResourcesSingletonList, excludeFromCompletion = excluded,
                                 referenceValue = arg0WithoutVersionOrAlias)
            }

            else -> {
              addEntityReference(arg0, CloudFormationSection.ParametersAndResources, excludeFromCompletion = excluded)
            }
          }
        }

        Unit
      }

      CloudFormationIntrinsicFunction.Condition ->
        if (function.args.size != 1 || arg0 !is CfnScalarValueNode) {
          addProblem(function, message("condition.reference.expects.one.string.argument"))
        }
        else {
          addEntityReference(arg0, CloudFormationSection.ConditionsSingletonList)
        }

      CloudFormationIntrinsicFunction.FnBase64 -> {
        if (function.args.size != 1) {
          addProblem(function, message("base64.reference.expects.1.argument"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnFindInMap -> {
        if (function.args.size != 3) {
          addProblem(function, message("findinmap.requires.3.arguments"))
        }
        else {
          val mappingName = function.args[0]
          val firstLevelKey = function.args[1]
          val secondLevelKey = function.args[2]

          if (mappingName is CfnScalarValueNode) {
            addEntityReference(mappingName, CloudFormationSection.MappingsSingletonList)

            val mapping = CloudFormationResolve.resolveMapping(parsed, mappingName.value)
            if (mapping != null && firstLevelKey is CfnScalarValueNode) {
              val firstLevelKeyPsiElement = parsed.getPsiElement(firstLevelKey)
              addReference(CloudFormationMappingFirstLevelKeyReference(firstLevelKeyPsiElement, mappingName.value))

              // TODO resolve possible values if first level key is an expression

              if (secondLevelKey is CfnScalarValueNode) {
                val secondLevelKeyPsiElement = parsed.getPsiElement(secondLevelKey)
                addReference(CloudFormationMappingSecondLevelKeyReference(secondLevelKeyPsiElement, mappingName.value, firstLevelKey.value))
              }
            }
          }
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnGetAtt -> {
        val resourceName: String?
        val attributeName: String?

        if (function.args.size == 1 && arg0 is CfnScalarValueNode && function.name.value == CloudFormationIntrinsicFunction.FnGetAtt.shortForm) {
          val dotIndex = arg0.value.indexOf('.')
          if (dotIndex < 0) {
            addProblem(function,
                       message("getattr.in.short.form.requires.argument.in.the.format.logicalnameofresource.attributename"))
            resourceName = null
            attributeName = null
          }
          else {
            resourceName = arg0.value.substring(0, dotIndex)
            attributeName = arg0.value.substring(dotIndex + 1)
          }
        }
        else if (function.args.size == 2 && arg0 is CfnScalarValueNode) {
          resourceName = arg0.value
          attributeName = if (arg1 is CfnScalarValueNode) arg1.value else null
        }
        else {
          addProblem(function,
                     message("getatt.requires.two.string.arguments.in.full.form.or.one.string.argument.in.short.form"))
          resourceName = null
          attributeName = null
        }

        if (resourceName != null) {
          // TODO calculate exact text range and add it to ReferencesTest
          val resourceNodeParent = function.parentOfType<CfnResourceNode>(parsed)
          val excluded = resourceNodeParent?.let { it.name?.value }?.let { listOf(it) } ?: emptyList()

          val resource = CloudFormationResolve.resolveResource(parsed, resourceName)

          // From https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#awsserverlessfunction
          // Role: ARN of an IAM role to use as this function's execution role. If omitted, a default role is created for this function.
          // Here we handle this implicitly created role
          val resourceNameWithoutRoleSuffix = resourceName.removeSuffix("Role")
          val resourceWithoutRoleSuffix = CloudFormationResolve.resolveResource(parsed, resourceNameWithoutRoleSuffix)
          if (resource == null && resourceWithoutRoleSuffix?.typeName == awsServerlessFunction.name) {
            addEntityReference(arg0 as CfnScalarValueNode, CloudFormationSection.ResourcesSingletonList,
                               excludeFromCompletion = excluded, referenceValue = resourceNameWithoutRoleSuffix)
            if (attributeName != "Arn") {
              addProblem(
                if (function.args.size == 1) arg0 else (arg1 ?: function),
                message("implicit.iam.function.role.supports.only.arn.attribute"))
            }
          }
          else {
            addEntityReference(arg0 as CfnScalarValueNode, CloudFormationSection.ResourcesSingletonList, excludeFromCompletion = excluded,
                               referenceValue = resourceName)

            if (attributeName != null) {
              val typeName = resource?.typeName
              if (typeName != null &&
                  !CloudFormationResourceType.isCustomResourceType(typeName) &&
                  !(CloudFormationResourceType.isCloudFormationStack(typeName) && attributeName.startsWith("Outputs.")) &&
                  !(CloudFormationResourceType.isServerlessApplication(typeName) && attributeName.startsWith("Outputs.")) &&
                  CloudFormationMetadataProvider.METADATA.findResourceType(typeName, parsed.root) != null) {
                if (!resource.getAttributes(parsed.root).containsKey(attributeName)) {
                  addProblem(
                    if (function.args.size == 1) arg0 else (arg1 ?: function),
                    message("unknown.attribute.in.resource.type.0.1", typeName, attributeName))
                }
              }
            }
          }
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnGetAZs -> {
        // TODO verify string against known regions
        // TODO possibility for dataflow checks
        if (function.args.size != 1) {
          addProblem(function, message("getazs.expects.one.argument"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnCidr -> {
        if (function.args.size != 2 && function.args.size != 3) {
          addProblem(function, message("cidr.expects.two.or.three.arguments"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnImportValue -> {
        if (function.args.size != 1) {
          addProblem(function, message("importvalue.expects.one.argument"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnJoin -> {
        if (function.args.size != 2) {
          addProblem(function, message("join.expects.a.string.argument.and.an.array.argument"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnSplit -> {
        if (function.args.size != 2) {
          addProblem(function, message("split.expects.two.string.arguments"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnSelect -> {
        if (function.args.size != 2) {
          addProblem(function, message("select.expects.an.index.argument.and.an.array.argument"))
        }
        else if (arg0 is CfnScalarValueNode) {
          try {
            Integer.parseUnsignedInt(arg0.value)
          }
          catch (t: NumberFormatException) {
            addProblem(function, message("select.index.should.be.a.valid.non.negative.number"))
          }
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnSub -> {
        // TODO Add references to substituted values in a string
        // TODO Add references to the mapping
        if (function.args.size != 1 && !(function.args.size == 2 && arg1 is CfnObjectValueNode)) {
          addProblem(function, message("sub.expects.one.argument.plus.an.optional.value.map"))
        }

        Unit
      }

      // TODO Check context, valid only in boolean context
      CloudFormationIntrinsicFunction.FnAnd, CloudFormationIntrinsicFunction.FnOr -> {
        if (function.args.size < 2) {
          addProblem(function, message("0.expects.at.least.2.arguments", function.functionId.shortForm))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnEquals -> {
        if (function.args.size != 2) {
          addProblem(function, message("equals.expects.exactly.2.arguments"))
        }

        Unit
      }

      CloudFormationIntrinsicFunction.FnIf ->
        if (function.args.size == 3) {
          if (arg0 is CfnScalarValueNode) {
            addEntityReference(arg0, CloudFormationSection.ConditionsSingletonList)
          }
          else {
            addProblem(function, message("if.s.first.argument.should.be.a.condition.name"))
          }
        }
        else {
          addProblem(function, message("if.expects.exactly.3.arguments"))
        }

      CloudFormationIntrinsicFunction.FnNot -> {
        if (function.args.size != 1) {
          addProblem(function, message("not.expects.exactly.1.argument"))
        }

        Unit
      }
    }

    super.function(function)
  }

  override fun resourceDependsOn(resourceDependsOn: CfnResourceDependsOnNode) {
    resourceDependsOn.dependsOn.forEach { depend ->
      val excludeFromCompletion = mutableListOf<String>()

      currentResource!!.name?.value?.let { excludeFromCompletion.add(it) }
      resourceDependsOn.dependsOn.forEach { if (depend.value != it.value) excludeFromCompletion.add(it.value) }

      addEntityReference(depend, CloudFormationSection.ResourcesSingletonList, excludeFromCompletion)
    }

    super.resourceDependsOn(resourceDependsOn)
  }

  override fun resourceCondition(resourceCondition: CfnResourceConditionNode) {
    resourceCondition.condition?.let {
      addEntityReference(it, CloudFormationSection.ConditionsSingletonList)
    }

    super.resourceCondition(resourceCondition)
  }

  override fun resourceType(resourceType: CfnResourceTypeNode) {
    val resourceTypeValue = resourceType.value
    val typeName = resourceTypeValue?.value

    if (resourceTypeValue == null || typeName == null || typeName.isEmpty()) {
      addProblem(resourceType, message("type.value.is.required"))
      return
    }

    if (!isCustomResourceType(typeName)) {
      val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(typeName, parsed.root)
      if (resourceTypeMetadata == null) {
        addProblem(resourceTypeValue, message("format.unknown.type", typeName))
      }
    }

    super.resourceType(resourceType)
  }

  override fun outputs(outputs: CfnOutputsNode) {
    if (outputs.properties.isEmpty()) {
      addProblem(outputs, message("outputs.section.must.declare.at.least.one.stack.output"))
    }

    if (outputs.properties.size > CloudFormationMetadataProvider.METADATA.limits.maxOutputs) {
      addProblem(outputs, message("format.max.outputs.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxOutputs))
    }

    super.outputs(outputs)
  }

  override fun globals(globals: CfnGlobalsNode) {
    if (!parsed.root.transformValues.contains(CloudFormationConstants.awsServerless20161031TransformName)) {
      addProblem(globals,
                 message("globals.section.supported.with.0.transform.only", CloudFormationConstants.awsServerless20161031TransformName))
      return
    }

    if (globals.globals.isEmpty()) {
      addProblem(globals, message("globals.section.must.provide.defaults.for.at.least.one.resource"))
    }

    super.globals(globals)
  }

  override fun serverlessEntityDefaultsNode(serverlessEntityDefaultsNode: CfnServerlessEntityDefaultsNode) {
    @NlsSafe val name = serverlessEntityDefaultsNode.name?.value

    if (name != null) {
      val resourceType = CloudFormationConstants.GlobalsResourcesMap.get(name)
      if (resourceType == null) {
        addProblem(
          serverlessEntityDefaultsNode,
          message("unsupported.globals.section.0.the.following.sections.are.supported.1", name,
                  CloudFormationConstants.GlobalsResourcesMap.keys.sorted().joinToString()))
      } else {
        for (nameValueNode in serverlessEntityDefaultsNode.properties) {
          @NlsSafe val propertyName = nameValueNode.name?.value ?: continue

          val property = resourceType.properties.firstOrNull { it.name == propertyName }
          if (property == null || property.excludedFromGlobals) {
            addProblem(
              nameValueNode.name,
              message("property.0.is.unsupported.in.1.sections.of.globals", propertyName, name))
          }
        }
      }
    }

    super.serverlessEntityDefaultsNode(serverlessEntityDefaultsNode)
  }

  override fun parameters(parameters: CfnParametersNode) {
    if (parameters.parameters.isEmpty()) {
      addProblem(parameters, message("parameters.section.must.declare.at.least.one.parameter"))
    }

    if (parameters.parameters.size > CloudFormationMetadataProvider.METADATA.limits.maxParameters) {
      addProblem(parameters, message("format.max.parameters.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxParameters))
    }

    super.parameters(parameters)
  }

  private fun checkValueIsScalar(nameValue: CfnNameValueNode) {
    if (nameValue.value !is CfnScalarValueNode && nameValue.value != null) {
      addProblem(nameValue, message("expected.a.string"))
    }
  }

  override fun parameter(parameter: CfnParameterNode) {
    // https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html

    val type = parameter.properties.firstOrNull { it.name?.value == CloudFormationParameterProperty.Type.id }
    if (type != null) checkValueIsScalar(type)

    val typeValue = type?.value
    @NlsSafe val typeName = (typeValue as? CfnScalarValueNode)?.value?.trim()
    if (type == null || typeName == null) {
      addProblem(parameter, message("required.property.type.is.missing.or.empty"))
      return
    }

    if (!CloudFormationConstants.allParameterTypes.contains(typeName)) {
      addProblem(type.value, message("unknown.parameter.type.0", typeName))
      return
    }

    parameter.properties.forEach { property ->
      @NlsSafe val propertyName = property.name?.value?.trim() ?: return@forEach
      val typedPropertyName = CloudFormationParameterProperty.values().singleOrNull { it.id == propertyName }

      when (typedPropertyName) {
        null -> {
          addProblem(parameter, message("unknown.parameter.property.0", propertyName))
        }

        CloudFormationParameterProperty.AllowedPattern -> {
          checkValueIsScalar(property)
        }

        CloudFormationParameterProperty.AllowedValues -> {
          if (property.value is CfnArrayValueNode) {
            property.value.items.forEach {
              if (it !is CfnScalarValueNode) {
                addProblem(it, message("expected.a.string"))
              }
            }
          }
          else {
            addProblem(property, message("expected.an.array"))
          }

          Unit
        }

        CloudFormationParameterProperty.ConstraintDescription -> {
          checkValueIsScalar(property)
        }

        CloudFormationParameterProperty.Default -> {
          checkValueIsScalar(property)
        }

        CloudFormationParameterProperty.Description -> {
          checkValueIsScalar(property)

          val scalar = property.value as? CfnScalarValueNode
          if (scalar != null && scalar.value.length > CloudFormationConstants.ParameterDescriptionLimit) {
            addProblem(property,
                       message("0.is.too.long.1.chars.maximum.allowed.length.is.2", CloudFormationParameterProperty.Description.id,
                               scalar.value.length, CloudFormationConstants.ParameterDescriptionLimit))
          }

          Unit
        }

        CloudFormationParameterProperty.MaxLength, CloudFormationParameterProperty.MinLength -> {
          checkValueIsScalar(property)

          val scalar = property.value as? CfnScalarValueNode
          if (scalar != null) {
            if (!numbersPattern.matcher(scalar.value).matches()) {
              addProblem(property, message("expected.a.number"))
            }

            if (typeName != CloudFormationParameterType.String.id &&
                !CloudFormationConstants.AwsSpecificParameterTypes.contains(typeName) &&
                !CloudFormationConstants.SsmParameterTypes.contains(typeName)) {
              addProblem(property, message("0.property.is.valid.for.1.type.only", propertyName, CloudFormationParameterType.String.id))
            }
          }

          Unit
        }

        CloudFormationParameterProperty.MinValue, CloudFormationParameterProperty.MaxValue -> {
          checkValueIsScalar(property)

          val scalar = property.value as? CfnScalarValueNode
          if (scalar != null) {
            if (Floats.tryParse(scalar.value) == null) {
              addProblem(property, message("expected.an.integer.or.float"))
            }

            if (typeName != CloudFormationParameterType.Number.id) {
              addProblem(property, message("0.property.is.valid.for.1.type.only1", propertyName, CloudFormationParameterType.Number.id))
            }
          }

          Unit
        }

        CloudFormationParameterProperty.NoEcho -> {
          checkValueIsScalar(property)

          val scalar = property.value as? CfnScalarValueNode
          if (scalar != null && !scalar.value.equals("True", ignoreCase = true)) {
            addProblem(property, message("only.true.value.is.allowed"))
          }

          Unit
        }

        CloudFormationParameterProperty.Type -> Unit
      }.let {  } // enforce exhaustive check
    }
  }

  override fun mappings(mappings: CfnMappingsNode) {
    if (mappings.mappings.isEmpty()) {
      addProblem(mappings, message("mappings.section.must.declare.at.least.one.parameter"))
    }

    if (mappings.mappings.size > CloudFormationMetadataProvider.METADATA.limits.maxMappings) {
      addProblem(mappings, message("format.max.mappings.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxMappings))
    }

    super.mappings(mappings)
  }

  private fun calculateMissingProperties(presentProperties: List<String>, metadata: CloudFormationResourceType): List<String> {
    val propertiesDefinedInGlobals = if (metadata.name.startsWith(awsServerlessNamePrefix)) {
      val globalsSectionName = metadata.name.removePrefix(awsServerlessNamePrefix)

      val globals = parsed.root.globalsNode?.
          globals?.singleOrNull { it.name?.value == globalsSectionName }

      globals?.properties?.mapNotNull { it.name?.value } ?: emptyList()
    } else emptyList()

    val missingProperties = metadata.requiredProperties.filter {
      required -> !presentProperties.contains(required) && !propertiesDefinedInGlobals.contains(required)
    }

    return missingProperties.sorted()
  }

  override fun resource(resource: CfnResourceNode) {
    currentResource = resource

    val resourceType = resource.type
    if (resourceType == null) {
      addProblem(resource, message("type.property.is.required.for.resource"))
      return
    }

    val metadata = resourceType.metadata(parsed.root)
    if (metadata != null) {
      val propertiesNode = resource.properties
      propertiesNode?.properties?.forEach {
        val propertyName = it.name?.value
        if (propertyName != null &&
            propertyName != CloudFormationConstants.CommentResourcePropertyName &&
            !isCustomResourceType(resourceType.value!!.value) &&
            metadata.findProperty(propertyName) == null) {
          addProblem(it, message("format.unknown.resource.type.property", propertyName))
        }
      }

      val presentProperties = propertiesNode?.properties?.mapNotNull { it.name?.value } ?: emptyList()
      val missingProperties = calculateMissingProperties(presentProperties, metadata)
      if (missingProperties.isNotEmpty()) {
        addProblem(
          element = propertiesNode ?: resource,
          description = message("format.required.resource.properties.are.not.set", missingProperties.joinToString(separator = " "))
        )
      }
    }

    super.resource(resource)

    currentResource = null
  }

  override fun resources(resources: CfnResourcesNode) {
    if (resources.resources.isEmpty()) {
      addProblem(resources, message("resources.section.should.declare.at.least.one.resource"))
      return
    }

    super.resources(resources)
  }

  override fun metadata(metadata: CfnMetadataNode) {
    val cfnInterface = metadata.value?.properties?.singleOrNull {
      it.value is CfnObjectValueNode && it.name?.value == CloudFormationConstants.CloudFormationInterfaceType
    }?.let { it.value as CfnObjectValueNode }

    if (cfnInterface != null) {
      val parameterGroups = cfnInterface.properties
          .singleOrNull { it.name?.value == CloudFormationConstants.CloudFormationInterfaceParameterGroups }
          ?.let { it.value as? CfnArrayValueNode }

      val predefinedParameters = CloudFormationMetadataProvider.METADATA.predefinedParameters

      @Suppress("LoopToCallChain")
      for (parameterGroup in parameterGroups?.items?.mapNotNull { it as? CfnObjectValueNode } ?: emptyList()) {
        val parameters = parameterGroup.properties
            .singleOrNull { it.name?.value == CloudFormationConstants.CloudFormationInterfaceParameters }
            ?.let { it.value as? CfnArrayValueNode }

        for (parameter in parameters?.items ?: emptyList()) {
          if (parameter is CfnScalarValueNode) {
            addEntityReference(parameter, CloudFormationSection.ParametersSingletonList, predefinedParameters)
          } else {
            addProblem(parameter, message("expected.a.string"))
          }
        }
      }

      val parameterLabels = cfnInterface.properties
          .singleOrNull { it.name?.value == CloudFormationConstants.CloudFormationInterfaceParameterLabels }
          ?.let { it.value as? CfnObjectValueNode }
      for (parameterName in parameterLabels?.properties?.mapNotNull { it.name } ?: emptyList()) {
        addEntityReference(parameterName, CloudFormationSection.ParametersSingletonList, predefinedParameters)
      }
    }

    super.metadata(metadata)
  }

  override fun root(root: CfnRootNode) {
    if (root.resourcesNode == null && !parsed.getPsiElement(root).textRange.isEmpty) {
      addProblem(root, message("resources.section.is.missing"))
    }

    super.root(root)
  }

  class InspectionResult(
      val problems: List<CloudFormationProblem>,
      val references: Multimap<PsiElement, CloudFormationReferenceBase>,
      val fileModificationStamp: Long
  )

  companion object {
    private val ANALYZED_KEY = Key.create<InspectionResult>("CFN_ANALYZED_FILE")

    fun inspectFile(parsed: CloudFormationParsedFile): InspectionResult {
      val cached = parsed.psiFile.getUserData(ANALYZED_KEY)
      if (cached != null && cached.fileModificationStamp == parsed.psiFile.modificationStamp) {
        return cached
      }

      val inspections = CloudFormationInspections(parsed)
      inspections.root(parsed.root)
      val inspectionResult = InspectionResult(inspections.problems, inspections.references, parsed.psiFile.modificationStamp)

      parsed.psiFile.putUserData(ANALYZED_KEY, inspectionResult)

      return inspectionResult
    }
  }
}
