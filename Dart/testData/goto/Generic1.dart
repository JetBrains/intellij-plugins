class Generic1<T> {
  T generate(){
    return null;
  }

  foo(){
    var test = new Generic1<String>();
    test.generate().codeUnit<caret>At(0);
  }
}