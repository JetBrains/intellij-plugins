// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.vfs;

/**
 * @author Kir
 *
 * Code interval representation. Can represent a point, see #isPoint()
 */
public class CodePointer {
  private final int myLine1, myCol1, myLine2, myCol2;

  public CodePointer(int line, int col) {
    this(line, col, line, col);
  }

  public CodePointer(int line1, int col1, int line2, int col2) {
    assert line1 <= line2 : "" + line1 + " " + line2;
    if (line1 == line2) {
      assert col1 <= col2 : "" + col1 + " " + col2;
    }

    myLine1 = line1;
    myCol1 = col1;
    myLine2 = line2;
    myCol2 = col2;
  }

  public boolean isSameLine() {
    return myLine1 == myLine2;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CodePointer)) return false;

    final CodePointer codePointer = (CodePointer) o;

    if (myCol1 != codePointer.myCol1) return false;
    if (myCol2 != codePointer.myCol2) return false;
    if (myLine1 != codePointer.myLine1) return false;
    if (myLine2 != codePointer.myLine2) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = myLine1;
    result = 29 * result + myCol1;
    result = 29 * result + myLine2;
    result = 29 * result + myCol2;
    return result;
  }

  public String toString() {
    return "[" + myLine1 + "," + myCol1 + "," + myLine2 + "," + myCol2 + "]";
  }

  public int getLine1() {
    return myLine1;
  }

  public int getLine2() {
    return myLine2;
  }

  public int getColumn1() {
    return myCol1;
  }

  public int getColumn2() {
    return myCol2;
  }
}
