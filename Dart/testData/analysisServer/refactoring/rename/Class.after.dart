class NewName {
  NewName() {}
  NewName.named() {}
}

class B extends <caret>NewName { // in B
}

class C implements NewName {
}

/**
 * Has a parameter of type [NewName].
 */
main(NewName a) {
  print(NewName);
  new NewName();
  new NewName.named();
}