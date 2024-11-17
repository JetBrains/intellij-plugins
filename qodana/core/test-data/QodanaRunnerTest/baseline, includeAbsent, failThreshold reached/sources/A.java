class A {
  static int unusedResultAbsent() { return 0; }

  static int unusedResultUnchanged() { return 0; }

  static int unusedResultNew() { return 0; }

  public static void main(String[] args) {
    //unusedResultAbsent();
    unusedResultUnchanged();
    unusedResultNew();
  }
}