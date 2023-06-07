/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.resolve;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbImportName;
import com.intellij.protobuf.lang.psi.PbStringPart;
import com.intellij.protobuf.lang.resolve.FileResolveProvider.ChildEntry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** The reference to an imported .proto file. */
public class PbImportReference extends PsiPolyVariantReferenceBase<PsiElement> {

  private String importPath;

  public PbImportReference(@NotNull PbImportName element) {
    super(element);
    setImportPathAndRangeInElement(element);
  }

  public PbImportReference(String path, PsiElement element, TextRange rangeInElement) {
    super(element);
    importPath = path;
    setRangeInElement(rangeInElement);
  }

  private void setImportPathAndRangeInElement(@NotNull PbImportName element) {
    importPath = element.getStringValue().getAsString();
    TextRange rangeWithQuotes = element.getTextRange();
    TextRange rangeWithoutQuotes = element.getStringValue().getTextRangeNoQuotes();
    int start = rangeWithoutQuotes.getStartOffset() - rangeWithQuotes.getStartOffset();
    int end =
        rangeWithQuotes.getLength()
            + rangeWithoutQuotes.getEndOffset()
            - rangeWithQuotes.getEndOffset();
    setRangeInElement(TextRange.create(start, end));
  }

  @NotNull
  @Override
  public ResolveResult @NotNull[] multiResolve(boolean incompleteCode) {
    ResolveCache cache = ResolveCache.getInstance(myElement.getProject());
    return cache.resolveWithCaching(
        this,
        (ref, incompleteCode1) -> ref.multiResolveNoCache(),
        /* needToPreventRecursion= */ false,
        incompleteCode);
  }

  @NotNull
  @Override
  public Object @NotNull [] getVariants() {
    PsiElement value = getElement();
    String path = importPath;
    int lastSlash = path.lastIndexOf('/');
    path = lastSlash >= 0 ? path.substring(0, lastSlash) : "";
    Collection<ChildEntry> entries = PbFileResolver.getChildNamesForContext(path, value);
    List<LookupElement> results = new ArrayList<>(entries.size());
    for (ChildEntry entry : entries) {
      String completionValue =
          path.isEmpty() ? entry.getName() : String.join("/", path, entry.getName());
      LookupElementBuilder element;
      if (entry.isDirectory()) {
        element =
            LookupElementBuilder.create(entry, completionValue + "/")
                .withIcon(AllIcons.Nodes.Folder)
                .withPresentableText(entry.getName())
                // Accepting a directory suggestion automatically displays the completion popup
                // again, similar to Java imports.
                .withInsertHandler(
                    (context, item) ->
                        AutoPopupController.getInstance(context.getProject())
                            .scheduleAutoPopup(context.getEditor()));
      } else {
        element =
            LookupElementBuilder.create(entry, completionValue)
                .withIcon(PbIcons.FILE)
                .withPresentableText(entry.getName());
      }
      results.add(element);
    }
    return results.toArray();
  }

  @NotNull
  private ResolveResult[] multiResolveNoCache() {
    PsiElement value = getElement();
    List<PbFile> results = PbFileResolver.findFilesForContext(importPath, value);
    return results.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) {
    PsiElement element = getElement();
    if (!(element instanceof PbImportName importName)) {
      return super.handleElementRename(newElementName);
    }
    // newElementName is just the file's base name, so only replace the last path component.
    // The import string can be multiple string parts. For simplicity, just put the whole of
    // newElementName in the last part, and delete remnants of oldElementName that may be earlier.
    //
    // Example:
    // import "" "replace_me.proto"
    // ->
    // import "new.proto"
    // Example:
    // import "parent/sub" "dir/replace_me.proto"
    //  ->
    // import "parent/sub" "dir/new.proto"
    // Example:
    // import "parent/sub" "dir/" "" "replace_me.proto"
    //  ->
    // import "parent/sub" "dir/" "new.proto"
    List<PbStringPart> parts = importName.getStringValue().getStringParts();
    PbStringPart lastPart = parts.get(parts.size() - 1);
    LeafPsiElement stringLiteral = (LeafPsiElement) lastPart.getStringLiteral();
    String stringLiteralText = stringLiteral.getText();
    String endQuote =
        lastPart.isUnterminated()
            ? ""
            : stringLiteralText.substring(stringLiteralText.length() - 1);
    int slashIndex = stringLiteralText.lastIndexOf('/');
    if (slashIndex < 0) {
      char startQuote = stringLiteralText.charAt(0);
      stringLiteral.replaceWithText(startQuote + newElementName + endQuote);
      deleteUntilSlashOrBeginning(parts);
    } else {
      stringLiteral.replaceWithText(
          stringLiteralText.substring(0, slashIndex + 1) + newElementName + endQuote);
    }

    setImportPathAndRangeInElement(importName);
    return getElement();
  }

  private static void deleteUntilSlashOrBeginning(List<PbStringPart> parts) {
    for (int i = parts.size() - 2; i >= 0; i--) {
      PbStringPart stringPart = parts.get(i);
      LeafPsiElement stringLiteral = (LeafPsiElement) stringPart.getStringLiteral();
      String stringLiteralText = stringLiteral.getText();
      int slashIndex = stringLiteralText.lastIndexOf('/');
      if (slashIndex < 0) {
        stringPart.delete();
      } else {
        String endQuote =
            stringPart.isUnterminated()
                ? ""
                : stringLiteralText.substring(stringLiteralText.length() - 1);
        stringLiteral.replaceWithText(stringLiteralText.substring(0, slashIndex + 1) + endQuote);
        return;
      }
    }
  }
}
