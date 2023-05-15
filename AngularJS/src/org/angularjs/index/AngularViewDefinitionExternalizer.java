package org.angularjs.index;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.DataInputOutputUtil;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AngularViewDefinitionExternalizer implements DataExternalizer<AngularNamedItemDefinition> {
  public static final AngularViewDefinitionExternalizer INSTANCE = new AngularViewDefinitionExternalizer();

  @Override
  public void save(@NotNull DataOutput out, AngularNamedItemDefinition value) throws IOException {
    out.writeUTF(StringUtil.notNullize(value.getName()));
    DataInputOutputUtil.writeLONG(out, value.getStartOffset());
  }

  @Override
  public AngularNamedItemDefinition read(@NotNull DataInput in) throws IOException {
    final String name = in.readUTF();
    final long offset = DataInputOutputUtil.readLONG(in);
    return new AngularNamedItemDefinition(name, offset);
  }
}
