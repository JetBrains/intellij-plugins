package com.intellij.flex.uiDesigner.mxml;

import com.intellij.psi.xml.XmlFile;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

public class ProjectComponentReferenceCounter {
  public final TIntArrayList total = new TIntArrayList();
  public final List<XmlFile> unregistered = new ArrayList<XmlFile>();

  public boolean hasUnregistered() {
    return !unregistered.isEmpty();
  }

  public void registered(int id) {
    total.add(id);
  }

  public void unregistered(int id, XmlFile psiFile) {
    total.add(id);
    unregistered.add(psiFile);
  }
}
