// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.protobuf.lang.stub.PbMessageDefinitionStub;

public interface PbMessageDefinition extends PbDefinition, PbMessageType, StubBasedPsiElement<PbMessageDefinitionStub> {

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  PbMessageBody getBody();

}
