library foo;
<fold text='/**...*/' expand='false'>/**
 * doc
 */</fold>
<fold text='///...' expand='false'>/// doc1
/// doc2</fold>
<fold text='/*...*/' expand='true'>/*
 x
 */</fold>
<fold text='//...' expand='true'>// 1
// 2</fold>
topLevel(){}
<fold text='/*...*/' expand='true'>/*
 y
 */</fold>
<fold text='/**...*/' expand='false'>/**
 * class doc
 */</fold>
<fold text='///...' expand='false'>/// doc1
/// doc2</fold>
<fold text='//...' expand='true'>// 1
// 2</fold>
class A<fold text='{...}' expand='true'>{
  <fold text='/**...*/' expand='false'>/**
   method doc
   */</fold>
  <fold text='/*...*/' expand='true'>/*
   * w
   */</fold>
  <fold text='//...' expand='true'>// 1
  // 2</fold>
  <fold text='///...' expand='false'>/// doc1
  /// doc2</fold>
  bar(){}
}</fold>