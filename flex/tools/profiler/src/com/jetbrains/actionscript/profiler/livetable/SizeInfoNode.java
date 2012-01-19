package com.jetbrains.actionscript.profiler.livetable;

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
  private final String qName;
  private final @Nullable FrameInfo frameInfo;
  private long size;
  private int count;

  public SizeInfoNode(String qName, @Nullable FrameInfo frameInfo, long size, int count) {
    this.qName = qName;
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
    final int genericIndex = qName.indexOf(".<");
    return genericIndex == -1 ? qName : qName.substring(0, genericIndex);
  }

  public String getClassName() {
    int dotIndex = getPackageClassSeparatorIndex();
    return dotIndex == -1 ? qName : qName.substring(dotIndex + 1);
  }

  @Nullable
  public String getPackageName() {
    int dotIndex = getPackageClassSeparatorIndex();
    return dotIndex == -1 ? null : qName.substring(0, dotIndex);
  }

  private int getPackageClassSeparatorIndex() {
    int dotIndex = qName.lastIndexOf('.');
    if (dotIndex >= 0 && qName.charAt(dotIndex + 1) == '<') {
      //generic Vector.<int>
      final String temp = qName.substring(0, dotIndex);
      dotIndex = temp.lastIndexOf('.');
    }
    return dotIndex;
  }
}
