package org.angularjs.index;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.angularjs.codeInsight.router.AngularJSRouterConstants;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Irina.Chernushina on 2/11/2016.
 */
public class AngularUiRouterViewsIndex extends FileBasedIndexExtension<String, AngularViewDefinition> {
  public static final ID<String, AngularViewDefinition> UI_ROUTER_VIEWS_CACHE_INDEX = ID.create("angularjs.ui.router.views.index");
  private final MyIndexer myIndexer = new MyIndexer();
  private final ListAngularViewDefinitionExternalizer myExternalizer = new ListAngularViewDefinitionExternalizer();

  @NotNull
  @Override
  public ID<String, AngularViewDefinition> getName() {
    return UI_ROUTER_VIEWS_CACHE_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, AngularViewDefinition, FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @NotNull
  @Override
  public DataExternalizer<AngularViewDefinition> getValueExternalizer() {
    return myExternalizer;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return AngularTemplateIndexInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }

  private static class MyIndexer implements DataIndexer<String, AngularViewDefinition, FileContent> {
    @NotNull
    @Override
    public Map<String, AngularViewDefinition> map(@NotNull FileContent inputData) {
      final Map<String, AngularViewDefinition> map = new HashMap<String, AngularViewDefinition>();
      final PsiFile file = inputData.getPsiFile();
      if (file instanceof XmlFile) {
        file.accept(
          new XmlRecursiveElementWalkingVisitor() {
            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
              if (AngularJSRouterConstants.uiView.equals(attribute.getName())) {
                final XmlAttributeValue element = attribute.getValueElement();
                if (element == null) {
                  map.put("", new AngularViewDefinition("", attribute.getTextRange().getStartOffset()));
                } else {
                  final String name = StringUtil.unquoteString(element.getText());
                  map.put(name, new AngularViewDefinition(name, element.getTextRange().getStartOffset()));
                }
              }
            }
          }
        );
      }
      return map;
    }
  }

  private static class ListAngularViewDefinitionExternalizer implements DataExternalizer<AngularViewDefinition> {
    @Override
    public void save(@NotNull DataOutput out, AngularViewDefinition value) throws IOException {
      out.writeUTF(StringUtil.notNullize(value.getName()));
      DataInputOutputUtil.writeLONG(out, value.getStartOffset());
    }

    @Override
    public AngularViewDefinition read(@NotNull DataInput in) throws IOException {
      final String name = in.readUTF();
      final long offset = DataInputOutputUtil.readLONG(in);
      return new AngularViewDefinition(name, offset);
    }
  }
}
