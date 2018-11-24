/*
 * Copyright 2012 Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jetbrains.lang.dart.ide.runner.server.google;

import java.util.HashMap;
import java.util.Map;

/**
 * The representation of a VM isolate.
 */
public class VmIsolate {
  private int id;
  private boolean paused = false;
  private boolean firstBreak = true;

  private Map<Integer, VmClass> classInfoMap = new HashMap<>();
  private Map<Integer, VmLibrary> libraryInfoMap = new HashMap<>();
  private boolean myTemporarilyInterrupted;

  protected VmIsolate(int isolateId, boolean paused) {
    this.id = isolateId;
    this.paused = paused;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof VmIsolate) {
      VmIsolate isolate = (VmIsolate)other;

      return getId() == isolate.getId();
    }

    return false;
  }

  public VmClass getClassInfo(int classId) {
    return classInfoMap.get(classId);
  }

  public String getClassName(int classId) {
    VmClass vmClass = classInfoMap.get(classId);

    return vmClass == null ? null : vmClass.getName();
  }

  public int getId() {
    return id;
  }

  public VmLibrary getLibraryInfo(int libraryId) {
    return libraryInfoMap.get(libraryId);
  }

  public String getName() {
    return "isolate-" + getId();
  }

  public boolean hasClassInfo(int classId) {
    return classInfoMap.containsKey(classId);
  }

  @Override
  public int hashCode() {
    return getId();
  }

  public boolean hasLibraryInfo(int libraryId) {
    return libraryInfoMap.containsKey(libraryId);
  }

  public boolean isFirstBreak() {
    return firstBreak;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setFirstBreak(boolean value) {
    firstBreak = value;
  }

  public void setPaused(boolean value) {
    paused = value;
  }

  @Override
  public String toString() {
    return "VmIsolate " + getId();
  }

  protected void clearClassInfoMap() {
    classInfoMap.clear();
    libraryInfoMap.clear();
  }

  protected void setClassInfo(int classId, VmClass vmClass) {
    classInfoMap.put(classId, vmClass);
  }

  protected void setLibraryInfo(int libraryId, VmLibrary vmLibrary) {
    libraryInfoMap.put(libraryId, vmLibrary);
  }

  public void setTemporarilyInterrupted(boolean interrupted) {
    myTemporarilyInterrupted = interrupted;
  }

  public boolean isTemporarilyInterrupted() {
    return myTemporarilyInterrupted;
  }
}
