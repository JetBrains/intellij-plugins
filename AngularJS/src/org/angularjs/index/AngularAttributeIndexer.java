package org.angularjs.index;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/17/2016.
 */
public class AngularAttributeIndexer implements DataIndexer<String, AngularNamedItemDefinition, FileContent> {
  private final String myDirectiveName;

  public AngularAttributeIndexer(final @NotNull String directiveName) {
    myDirectiveName = directiveName;
  }

  @Override
  public @NotNull Map<String, AngularNamedItemDefinition> map(@NotNull FileContent inputData) {
    final Map<String, AngularNamedItemDefinition> map = new HashMap<>();
    final PsiFile file = inputData.getPsiFile();
    if (file instanceof XmlFile) {
      file.accept(
        new XmlRecursiveElementWalkingVisitor() {
          @Override
          public void visitXmlAttribute(XmlAttribute attribute) {
            if (myDirectiveName.equals(DirectiveUtil.normalizeAttributeName(attribute.getName()))) {
              final XmlAttributeValue element = attribute.getValueElement();
              if (element == null) {
                map.put("", new AngularNamedItemDefinition("", attribute.getTextRange().getStartOffset()));
              }
              else {
                final String name = StringUtil.unquoteString(element.getText());
                map.put(name, new AngularNamedItemDefinition(name, element.getTextRange().getStartOffset()));
              }
            }
          }
        }
      );
    }
    return map;
  }
}
