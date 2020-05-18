// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

public class Angular2PsiParser implements PsiParser {

  @NonNls public static final String ACTION = "action";
  @NonNls public static final String BINDING = "binding";
  @NonNls public static final String TEMPLATE_BINDINGS = "template_bindings";
  @NonNls public static final String INTERPOLATION = "interpolation";
  @NonNls public static final String SIMPLE_BINDING = "simple_binding";

  @NonNls private static final Logger LOG = Logger.getInstance(Angular2PsiParser.class);

  private static final Map<String, BiConsumer<PsiBuilder, IElementType>> parseMappings = ContainerUtil.newHashMap(
    Pair.create(ACTION, Angular2Parser::parseAction),
    Pair.create(BINDING, Angular2Parser::parseBinding),
    Pair.create(INTERPOLATION, Angular2Parser::parseInterpolation),
    Pair.create(SIMPLE_BINDING, Angular2Parser::parseSimpleBinding)
  );

  @Override
  public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
    PsiFile containingFile = builder.getUserData(FileContextUtil.CONTAINING_FILE_KEY);
    if (containingFile != null) {
      String ext = FileUtilRt.getExtension(containingFile.getName());
      BiConsumer<PsiBuilder, IElementType> parseMethod = parseMappings.get(ext);
      if (parseMethod != null) {
        parseMethod.accept(builder, root);
      }
      else if (TEMPLATE_BINDINGS.equals(ext)) {
        String templateKey = FileUtilRt.getExtension(FileUtilRt.getNameWithoutExtension(containingFile.getName()));
        Angular2Parser.parseTemplateBindings(builder, root, templateKey);
      }
      else if (ext.equals("js")) {
        //special case for creation of AST from text
        Angular2Parser.parseJS(builder, root);
      }
      else {
        Angular2Parser.parseInterpolation(builder, root);
      }
    }
    else {
      LOG.error("No containing file while parsing Angular2 expression.");
    }
    return builder.getTreeBuilt();
  }
}
