package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CloudFormationFormatChecker {
  private static Pattern AlphanumericStringPattern = Pattern.compile("[a-zA-Z0-9]+");

  private List<ProblemDescriptor> myProblems = new ArrayList<ProblemDescriptor>();
  private InspectionManager myInspectionManager;
  private final boolean myOnTheFly;

  public CloudFormationFormatChecker(InspectionManager manager, boolean isOnTheFly) {
    myInspectionManager = manager;
    myOnTheFly = isOnTheFly;
  }

  public List<ProblemDescriptor> getProblems() {
    return myProblems;
  }

  private void addProblem(@NotNull PsiElement element, @NotNull String description) {
    myProblems.add(myInspectionManager.createProblemDescriptor(
      element,
      description,
      myOnTheFly,
      LocalQuickFix.EMPTY_ARRAY,
      ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
  }

  private void addProblemOnNameElement(@NotNull JsonProperty property, @NotNull String description) {
    addProblem(
      property.getFirstChild() != null ? property.getFirstChild() : property,
      description);
  }

  private void root(JsonObject root) {
    for (JsonProperty property : root.getPropertyList()) {
      final String name = property.getName();
      final JsonValue value = property.getValue();

      if (name.isEmpty() || value == null) {
        continue;
      }

      if (CloudFormationSections.FormatVersion.equals(name)) {
        formatVersion(value);
      } else if (CloudFormationSections.Description.equals(name)) {
        description(value);
      } else if (CloudFormationSections.Parameters.equals(name)) {
        parameters(value);
      } else if (CloudFormationSections.Resources.equals(name)) {
        resources(value);
      } else if (CloudFormationSections.Conditions.equals(name)) {
        // TODO
      } else if (CloudFormationSections.Metadata.equals(name)) {
        // Generic content inside, no need to check
        // See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/metadata-section-structure.html
      } else if (CloudFormationSections.Outputs.equals(name)) {
        outputs(value);
      } else if (CloudFormationSections.Mappings.equals(name)) {
        mappings(value);
      } else {
        addProblemOnNameElement(
          property,
          CloudFormationBundle.getString("format.unknown.section", property.getName()));
      }
    }

    if (root.findProperty(CloudFormationSections.Resources) == null) {
      addProblem(root, CloudFormationBundle.getString("format.resources.section.required"));
    }
  }

  private void outputs(JsonValue outputsExpression) {
    final JsonObject obj = checkAndGetObject(outputsExpression);
    if (obj == null) {
      return;
    }

    for (JsonProperty property : obj.getPropertyList()) {
      final String name = property.getName();
      final JsonValue value = property.getValue();
      if (name.isEmpty() || value == null) {
        continue;
      }

      checkKeyName(property);
      stringValue(value);
    }

    if (obj.getPropertyList().size() == 0) {
      addProblemOnNameElement(
        (JsonProperty)obj.getParent(),
        CloudFormationBundle.getString("format.no.outputs.declared"));
    }

    if (obj.getPropertyList().size() > CloudFormationMetadataProvider.METADATA.limits.maxOutputs) {
      addProblemOnNameElement(
        (JsonProperty)obj.getParent(),
        CloudFormationBundle.getString("format.max.outputs.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxOutputs));
    }
  }

  private void parameters(JsonValue parametersExpression) {
    final JsonObject obj = checkAndGetObject(parametersExpression);
    if (obj == null) {
      return;
    }

    if (obj.getPropertyList().size() == 0) {
      addProblemOnNameElement(
        (JsonProperty)obj.getParent(),
        CloudFormationBundle.getString("format.no.parameters.declared"));
    }

    if (obj.getPropertyList().size() > CloudFormationMetadataProvider.METADATA.limits.maxParameters) {
      addProblemOnNameElement(
        (JsonProperty)obj.getParent(),
        CloudFormationBundle.getString("format.max.parameters.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxParameters));
    }
  }

  private void mappings(JsonValue mappingsExpression) {
    final JsonObject obj = checkAndGetObject(mappingsExpression);
    if (obj == null) {
      return;
    }

    if (obj.getPropertyList().size() == 0) {
      addProblemOnNameElement(
        (JsonProperty)obj.getParent(),
        CloudFormationBundle.getString("format.no.mappings.declared"));
    }

    if (obj.getPropertyList().size() > CloudFormationMetadataProvider.METADATA.limits.maxMappings) {
      addProblemOnNameElement(
        (JsonProperty)obj.getParent(),
        CloudFormationBundle.getString("format.max.mappings.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxMappings));
    }
  }

  private void stringValue(JsonValue expression) {
    final JsonStringLiteral literalExpression = ObjectUtils.tryCast(expression, JsonStringLiteral.class);
    if (literalExpression != null) {
      // TODO
    }
  }

  private void checkKeyName(JsonProperty property) {
    if (property == null || property.getName().isEmpty()) {
      return;
    }

    if (!AlphanumericStringPattern.matcher(property.getName()).matches()) {
      addProblemOnNameElement(
        property,
        CloudFormationBundle.getString("format.invalid.key.name"));
    }
  }

  private void description(JsonValue value) {
    checkAndGetQuotedStringText(value);
  }

  private void resources(JsonValue value) {
    final JsonObject obj = checkAndGetObject(value);
    if (obj == null) {
      return;
    }

    for (JsonProperty property : obj.getPropertyList()) {
      final String resourceName = property.getName();
      final JsonValue resourceObj = property.getValue();
      if (resourceName.isEmpty() || resourceObj == null) {
        continue;
      }

      checkKeyName(property);
      resource(property);
    }
  }

  private void resource(JsonProperty resourceProperty) {
    final JsonObject obj = checkAndGetObject(resourceProperty.getValue());
    if (obj == null) {
      return;
    }

    final JsonProperty typeProperty = obj.findProperty(CloudFormationConstants.TypePropertyName);
    if (typeProperty == null) {
      addProblemOnNameElement(resourceProperty, CloudFormationBundle.getString("format.type.property.required"));
      return;
    }

    for (JsonProperty property : obj.getPropertyList()) {
      final String propertyName = property.getName();

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName));
      }
    }

    resourceType(typeProperty);

    final JsonProperty propertiesProperty = obj.findProperty(CloudFormationConstants.PropertiesPropertyName);
    if (propertiesProperty != null) {
      resourceProperties(propertiesProperty, typeProperty);
    } else {
      final String resourceType = checkAndGetUnquotedStringText(typeProperty.getValue());
      if (resourceType != null) {
        final CloudFormationResourceType resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(resourceType);
        if (resourceTypeMetadata != null) {
          Set<String> requiredProperties = new HashSet<String>(resourceTypeMetadata.getRequiredProperties());
          if (!requiredProperties.isEmpty()) {
            final String requiredPropertiesString = StringUtil.join(requiredProperties, " ");
            addProblemOnNameElement(
              resourceProperty,
              CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString));
          }
        }
      }
    }
  }

  private void resourceProperties(JsonProperty propertiesProperty, JsonProperty typeProperty) {
    final JsonObject properties = ObjectUtils.tryCast(propertiesProperty.getValue(), JsonObject.class);
    if (properties == null) {
      addProblemOnNameElement(propertiesProperty, CloudFormationBundle.getString("format.properties.property.should.properties.list"));
      return;
    }

    String resourceTypeName = checkAndGetUnquotedStringText(typeProperty.getValue());
    if (resourceTypeName == null) {
      return;
    }

    if (resourceTypeName.startsWith(CloudFormationConstants.CustomResourceTypePrefix)) {
      resourceTypeName = CloudFormationConstants.CustomResourceType;
    }

    final CloudFormationResourceType resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName);
    if (resourceType == null) {
      return;
    }

    Set<String> requiredProperties = new HashSet<String>(resourceType.getRequiredProperties());

    for (JsonProperty property : properties.getPropertyList()) {
      final String propertyName = property.getName();
      if (propertyName.isEmpty()) {
        continue;
      }

      if (resourceType.findProperty(propertyName) == null && !isCustomResourceType(resourceTypeName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.type.property", propertyName));
      }

      requiredProperties.remove(propertyName);
    }

    if (!requiredProperties.isEmpty()) {
      final String requiredPropertiesString = StringUtil.join(requiredProperties, " ");
      addProblemOnNameElement(propertiesProperty,
                              CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString));
    }
  }

  private void resourceType(JsonProperty typeProperty) {
    final String value = checkAndGetUnquotedStringText(typeProperty.getValue());
    if (value == null) {
      return;
    }

    if (isCustomResourceType(value)) {
      return;
    }

    if (CloudFormationMetadataProvider.METADATA.findResourceType(value) == null) {
      addProblem(typeProperty, CloudFormationBundle.getString("format.unknown.type", value));
    }
  }

  private boolean isCustomResourceType(String value) {
    return value.equals(CloudFormationConstants.CustomResourceType) || value.startsWith(CloudFormationConstants.CustomResourceTypePrefix);
  }

  private JsonObject checkAndGetObject(JsonValue expression) {
    final JsonObject obj = ObjectUtils.tryCast(expression, JsonObject.class);
    if (obj == null) {
      addProblem(
        expression,
        CloudFormationBundle.getString("format.expected.json.object"));

      return null;
    }

    return obj;
  }


  private void formatVersion(JsonValue value) {
    final String text = checkAndGetQuotedStringText(value);
    if (text == null) {
      return;
    }

    final String version = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(text));
    if (!CloudFormationConstants.SupportedTemplateFormatVersions.contains(version)) {
      final String supportedVersions = StringUtil.join(CloudFormationConstants.SupportedTemplateFormatVersions, ", ");
      myProblems.add(
        myInspectionManager.createProblemDescriptor(
          value,
          CloudFormationBundle.getString("format.unknownVersion", supportedVersions),
          myOnTheFly,
          LocalQuickFix.EMPTY_ARRAY,
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      );
    }
  }

  @Nullable
  private String checkAndGetQuotedStringText(@Nullable JsonValue expression) {
    if (expression == null) {
      // Do not threat value absense as error
      return null;
    }

    final JsonStringLiteral literal = ObjectUtils.tryCast(expression, JsonStringLiteral.class);
    if (literal == null) {
      myProblems.add(
        myInspectionManager.createProblemDescriptor(
          expression,
          CloudFormationBundle.getString("format.expected.quoted.string"),
          myOnTheFly,
          LocalQuickFix.EMPTY_ARRAY,
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      );

      return null;
    }

    return literal.getText();
  }

  @Nullable
  private String checkAndGetUnquotedStringText(@Nullable JsonValue expression) {
    final String quoted = checkAndGetQuotedStringText(expression);
    if (quoted == null) {
      return null;
    }

    return StringUtil.stripQuotesAroundValue(quoted);
  }

  public void file(PsiFile psiFile) {
    assert CloudFormationPsiUtils.isCloudFormationFile(psiFile) : psiFile.getName() + " is not a cfn file";

    final JsonObject root = CloudFormationPsiUtils.getRootExpression(psiFile);
    if (root == null) {
      return;
    }

    root(root);
  }
}
