/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.osmorc.manifest.lang.header;

import com.intellij.codeInsight.daemon.JavaErrorMessages;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.ManifestBundle;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.resolve.reference.providers.ManifestPackageReferenceSet;
import org.osmorc.util.OsgiPsiUtil;

import java.util.List;

/**
 * @author Vladislav.Soroka
 */
public class BasePackageParser extends OsgiHeaderParser {
  public static final HeaderParser INSTANCE = new BasePackageParser();

  @NotNull
  @Override
  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    if (headerValuePart.getParent() instanceof Clause) {
      return getPackageReferences(headerValuePart);
    }

    return PsiReference.EMPTY_ARRAY;
  }

  protected static PsiReference[] getPackageReferences(PsiElement psiElement) {
    String packageName = psiElement.getText();
    if (StringUtil.isEmptyOrSpaces(packageName) ) {
      return PsiReference.EMPTY_ARRAY;
    }

    int offset = 0;
    if (packageName.charAt(0) == '!') {
      packageName = packageName.substring(1);
      offset = 1;
    }

    int size = packageName.length() - 1;
    if (packageName.charAt(size) == '?') {
      packageName = packageName.substring(0, size);
    }

    PackageReferenceSet referenceSet = new ManifestPackageReferenceSet(packageName, psiElement, offset);
    return referenceSet.getReferences().toArray(new PsiPackageReference[referenceSet.getReferences().size()]);
  }

  @Override
  public boolean annotate(@NotNull Header header, @NotNull AnnotationHolder holder) {
    boolean annotated = false;

    for (HeaderValue value : header.getHeaderValues()) {
      if (value instanceof Clause) {
        HeaderValuePart valuePart = ((Clause)value).getValue();
        if (valuePart != null) {
          String packageName = valuePart.getUnwrappedText();
          packageName = StringUtil.trimEnd(packageName, ".*");

          if (StringUtil.isEmptyOrSpaces(packageName)) {
            holder.createErrorAnnotation(valuePart.getHighlightingRange(), ManifestBundle.message("header.reference.invalid"));
            annotated = true;
            continue;
          }

          PsiDirectory[] directories = OsgiPsiUtil.resolvePackage(header, packageName);
          if (directories.length == 0) {
            holder
              .createErrorAnnotation(valuePart.getHighlightingRange(), JavaErrorMessages.message("cannot.resolve.package", packageName));
            annotated = true;
          }
        }
      }
    }

    return annotated;
  }

  @Nullable
  @Override
  public Object getConvertedValue(@NotNull Header header) {
    List<HeaderValue> headerValues = header.getHeaderValues();
    if (!headerValues.isEmpty()) {
      List<String> packages = ContainerUtil.newArrayListWithCapacity(headerValues.size());
      for (HeaderValue headerValue : headerValues) {
        HeaderValuePart valuePart = ((Clause)headerValue).getValue();
        if (valuePart != null) {
          String packageName = valuePart.getText().replaceAll("\\s+", "");
          packages.add(packageName);
        }
      }
      return packages;
    }

    return null;
  }
}
