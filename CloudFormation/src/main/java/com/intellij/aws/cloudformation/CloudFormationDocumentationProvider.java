/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @author max
 */
package com.intellij.aws.cloudformation;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;

public class CloudFormationDocumentationProvider extends AbstractDocumentationProvider {
    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return element.getText();
    }
    @Override
    public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
        PsiElement parent = element;
        if (parent.getParent() instanceof JsonStringLiteral) {
            parent = parent.getParent();
        }
        if (CloudFormationPsiUtils.isResourceTypeValuePosition(parent)) {
            return createResourceDescription(parent);
        } else if (CloudFormationPsiUtils.isResourcePropertyNamePosition(parent)) {
            return createPropertyDescription(parent);
        }
        return "";
    }

    public String createResourceDescription(PsiElement element) {
        final JsonStringLiteral propertyValue = ObjectUtils.tryCast(element, JsonStringLiteral.class);
        if (propertyValue == null) {
            return "";
        }

        final JsonProperty property = ObjectUtils.tryCast(element.getParent(), JsonProperty.class);
        if (property == null || !property.getName().equals("Type")) {
            return "";
        }

        for (CloudFormationResourceType resourceType : CloudFormationMetadataProvider.METADATA.resourceTypes) {
            if(resourceType.name.equals(property.getValue().getText().replace("\"", "")))
                return resourceType.description.replace("href=\"(?!http)", "href=\"http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/");;
        }
        return "";
    }
    public String createPropertyDescription(PsiElement element)
    {
        final JsonStringLiteral propertyName = ObjectUtils.tryCast(element, JsonStringLiteral.class);
        if (propertyName == null) {
            return "";
        }

        final JsonProperty property = ObjectUtils.tryCast(propertyName.getParent(), JsonProperty.class);
        if (property == null || property.getNameElement() != propertyName) {
            return "";
        }

        final JsonProperty resourceElement = CloudFormationPsiUtils.getResourceElementFromPropertyName(propertyName);
        if (resourceElement == null) {
            return "";
        }

        final JsonObject resourceValue = ObjectUtils.tryCast(resourceElement.getValue(), JsonObject.class);
        if (resourceValue == null) {
            return "";
        }

        final JsonProperty typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName);
        if (typeProperty == null) {
            return "";
        }

        final JsonStringLiteral typeValue = ObjectUtils.tryCast(typeProperty.getValue(), JsonStringLiteral.class);
        if (typeValue == null) {
            return "";
        }

        final String type = CloudFormationResolve.Companion.getTargetName(typeValue);

        final CloudFormationResourceType resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(type);
        if (resourceTypeMetadata == null) {
            return "";
        }


        for (CloudFormationResourceProperty propertyMetadata : resourceTypeMetadata.properties) {
            if (propertyMetadata.name.equals(property.getName())) {
                String document = "<p>"+ propertyMetadata.description+"</p><br>"+
                    "<p><i>Required:</i> "+ propertyMetadata.required+"</p>"+
                    propertyMetadata.type+
                    propertyMetadata.updateRequires;

                return document.replace("href=\"(?!http)", "href=\"http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/");
            }

        }
        return "";
    }

    @Override
    public java.util.List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return null;
    }
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return element;
    }
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return context;
    }

    @Nullable
    public PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file, @Nullable PsiElement contextElement) {
        return contextElement;
    }

    @Nullable
    public Image getLocalImageForElement(@NotNull PsiElement element, @NotNull String imageSpec) {
        return null;
    }
}