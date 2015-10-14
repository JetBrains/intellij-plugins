package com.intellij.javascript.flex.index;

import com.intellij.lang.javascript.index.JSCustomIndexer;
import com.intellij.lang.javascript.index.JSIndexContentBuilder;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptCustomIndexer extends JSCustomIndexer {
  public ActionScriptCustomIndexer(@NotNull PsiFile file, @NotNull JSIndexContentBuilder indexBuilder) {
    super(file, indexBuilder);
  }

  @Override
  protected JSQualifiedName processXmlTag(XmlTag element) {
    final XmlAttribute idAttribute = element.getAttribute("id");
    final String id = idAttribute == null ? null : idAttribute.getValue();
    if (idAttribute != null && id != null) {
      final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder(id, null)
        .setType(JSImplicitElement.Type.Tag);
      addImplicitElement(idAttribute, builder);
    }

    return myNamespaces.peek();
  }
}
