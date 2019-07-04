library foo;

f() <fold text='{...}' expand='true'>{
  assert();
  assert(true);
  assert(<fold text='...' expand='true'>true
</fold>);
  assert(<fold text='...' expand='true'>true &&
true</fold>);
  assert(true && true && true && true, "");
  assert(<fold text='...' expand='true'>true && true && true && true,
         ""</fold>);
  assert(<fold text='...' expand='true'>true &&
                                        true &&
                                        true &&
                                        true, "some message"</fold>);
  assert(xyz
}</fold>