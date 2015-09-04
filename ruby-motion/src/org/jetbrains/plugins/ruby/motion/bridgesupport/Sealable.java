package org.jetbrains.plugins.ruby.motion.bridgesupport;

/**
 * @author Dennis.Ushakov
 */
public abstract class Sealable {
  private boolean mySealed;

  protected void checkSeal() {
    if (mySealed) {
      throw new IllegalStateException("Cannot modify sealed class");
    }
  }

  public void seal() {
    mySealed = true;
  }
}
