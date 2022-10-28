// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.text.EditDistance;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.inspections.quickfixes.CreateAttributeQuickFix;
import org.angular2.inspections.quickfixes.RenameAttributeQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;

import static org.angular2.web.containers.I18NAttributesScope.isI18nCandidate;

public class AngularInvalidI18nAttributeInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    Angular2AttributeNameParser.AttributeInfo info = descriptor.getInfo();
    XmlTag parent = attribute.getParent();
    if (info.type == Angular2AttributeType.I18N && parent != null) {
      TextRange range = attribute.getNameElement().getTextRangeInParent();
      String i18nedAttrName = info.name;
      Set<String> candidates = new TreeSet<>();
      for (XmlAttribute attr : parent.getAttributes()) {
        Angular2AttributeNameParser.AttributeInfo attrInfo = Angular2AttributeNameParser.parse(attr.getName(), parent);
        if (isI18nCandidate(attrInfo)) {
          candidates.add(Angular2AttributeType.I18N.buildName(attr.getName()));
        }
      }
      for (XmlAttribute attr : parent.getAttributes()) {
        candidates.remove(attr.getName());
      }
      if (i18nedAttrName.isEmpty()) {
        LocalQuickFix[] quickFixes = StreamEx.of(candidates)
          .limit(3)
          .map(name -> new RenameAttributeQuickFix(name)).toArray(LocalQuickFix[]::new);
        //noinspection DialogTitleCapitalization
        holder.registerProblem(attribute, range,
                               Angular2Bundle.message("angular.inspection.i18n.message.empty"),
                               quickFixes);
      }
      else if (descriptor.hasErrorSymbols()) {
        LocalQuickFix[] quickFixes = StreamEx.of(candidates)
          .sorted((str1, str2) -> -EditDistance.levenshtein(str1, str2, false))
          .limit(2)
          .map(name -> (LocalQuickFix)new RenameAttributeQuickFix(name))
          .append(new CreateAttributeQuickFix(i18nedAttrName))
          .toArray(LocalQuickFix[]::new);

        holder.registerProblem(attribute, new TextRange(range.getStartOffset() + 5, range.getEndOffset()),
                               Angular2Bundle.message("angular.inspection.i18n.message.not-matching", i18nedAttrName),
                               quickFixes);
      }
    }
  }
}
