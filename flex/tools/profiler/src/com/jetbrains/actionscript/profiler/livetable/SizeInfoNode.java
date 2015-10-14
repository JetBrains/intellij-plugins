package com.jetbrains.actionscript.profiler.livetable;

import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.jetbrains.actionscript.profiler.base.FilePathProducer;
import com.jetbrains.actionscript.profiler.base.FrameInfoProducer;
import com.jetbrains.actionscript.profiler.base.QNameProducer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author: Fedor.Korotkov
 */
public class SizeInfoNode extends DefaultMutableTreeNode implements FrameInfoProducer, FilePathProducer, QNameProducer {
  private final @Nullable FrameInfo frameInfo;
  private final @Nullable String packageName;
  private final JSResolveUtil.GenericSignature signature;
  private long size;
  private int count;

  public SizeInfoNode(String qName, @Nullable FrameInfo frameInfo, long size, int count) {
    JSResolveUtil.GenericSignature signatureCandidate = JSResolveUtil.extractGenericSignature(qName);
    if (signatureCandidate == null) {
      signatureCandidate = new JSResolveUtil.GenericSignature(qName, null);
    }
    signature = signatureCandidate;

    final int packageSeparatorIndex = signature.elementType.lastIndexOf('.');
    packageName = packageSeparatorIndex == -1 ? null : signature.elementType.substring(0, packageSeparatorIndex);
    this.frameInfo = frameInfo;
    this.size = size;
    this.count = count;

    setUserObject(qName);
  }

  @Nullable
  public FrameInfo getFrameInfo() {
    return frameInfo;
  }

  public long getSize() {
    return size;
  }

  public int getCount() {
    return count;
  }

  public void incSize(long size) {
    this.size += size;
    ++count;
  }


  public void decSize(long size) {
    this.size -= size;
    --count;
  }

  public boolean isMethod() {
    return getFrameInfo() != null;
  }

  @Override
  public String getFilePath() {
    if (frameInfo != null) {
      return frameInfo.getFilePath();
    }
    return null;
  }

  @Override
  public String getQName() {
    if (frameInfo != null) {
      return frameInfo.getQName();
    }
    if (packageName != null) {
      return packageName + "." + signature.elementType;
    }
    return signature.elementType;
  }

  public String getClassName() {
    if (signature.genericType == null) {
      return signature.elementType;
    }
    return signature.elementType + ".<" + signature.genericType + ">";
  }

  @Nullable
  public String getPackageName() {
    return packageName;
  }
}
