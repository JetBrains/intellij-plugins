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
class A<fold text='{...}' expand='true'>{
  bar()<fold text='{...}' expand='true'>{
    if (true)<fold text='{...}' expand='true'>{
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
    }</fold>
  }</fold>
}</fold>

class B <fold text='{...}' expand='true'>{
  //
}</fold>

class C
<fold text='{...}' expand='true'>{

}</fold>