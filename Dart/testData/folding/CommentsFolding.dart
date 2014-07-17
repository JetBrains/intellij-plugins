library foo;
// single
/// single
<fold text='/*...*/' expand='true'>/*
multiline
 */</fold>
<fold text='//...' expand='true'>// 1
// 2</fold>
<fold text='///...' expand='true'>/// doc 1
/// doc 2
/// doc 3</fold>
<fold text='/**...*/' expand='true'>/**
 * multiline doc
 */</fold>
class A{
  bar(){
    if (true){
      <fold text='/*...*/' expand='true'>/*
      multiline
       */</fold>
      <fold text='//...' expand='true'>// 1
      // 2
      // 3</fold>
      <fold text='///...' expand='true'>/// doc 1
      /// doc 2</fold>
      // single
      /// single
      <fold text='/**...*/' expand='true'>/**
       * multiline doc
       */</fold>
      <fold text='/**...*/' expand='true'>/**
       * multiline doc 2
       */</fold>
    }
  }
}