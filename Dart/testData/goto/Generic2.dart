class Generic2<T> {
  T generate(){
    return null;
  }

  foo(Generic2<Generic2<String>> test){
    test.generate().generate().codeUnit<caret>At(0);
  }
}