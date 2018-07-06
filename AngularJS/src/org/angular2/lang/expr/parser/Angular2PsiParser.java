// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Dennis.Ushakov
 */
public class Angular2PsiParser implements PsiParser {

  public static final String ACTION = "action";
  public static final String BINDING = "binding";
  public static final String TEMPLATE_BINDINGS = "template_bindings";
  public static final String INTERPOLATION = "interpolation";
  public static final String SIMPLE_BINDING = "simple_binding";

  private static final Logger LOG = Logger.getInstance(Angular2PsiParser.class);

  private static final Map<String, BiConsumer<Angular2Parser, IElementType>> parseMappings = ContainerUtil.newHashMap(
    Pair.create(ACTION, Angular2Parser::parseAction),
    Pair.create(BINDING, Angular2Parser::parseBinding),
    Pair.create(INTERPOLATION, Angular2Parser::parseInterpolation),
    Pair.create(SIMPLE_BINDING, Angular2Parser::parseSimpleBinding)
  );

  @NotNull
  @Override
  public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
    PsiFile containingFile = builder.getUserData(FileContextUtil.CONTAINING_FILE_KEY);
    if (containingFile != null) {
      String ext = FileUtilRt.getExtension(containingFile.getName());
      BiConsumer<Angular2Parser, IElementType> parseMethod = parseMappings.get(ext);
      if (parseMethod != null) {
        parseMethod.accept(new Angular2Parser(builder), root);
      } else if (TEMPLATE_BINDINGS.equals(ext)) {
        String templateKey = FileUtilRt.getExtension(FileUtilRt.getNameWithoutExtension(containingFile.getName()));
        new Angular2Parser(builder).parseTemplateBindings(root, templateKey);
      }
      else {
        LOG.error("Invalid file name '" + containingFile.getName() + "' - unsupported extension: " + ext);
      }
    } else {
      LOG.error("No containing file while parsing Angular2 expression.");
    }
    return builder.getTreeBuilt();
  }
}
