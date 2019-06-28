library foo;

f() <fold text='{...}' expand='true'>{
  assert(<fold text='...' expand='true'>true</fold>);
  assert(<fold text='...' expand='true'>true && true</fold>);
  assert(<fold text='...' expand='true'>true && true && true && true</fold>);
  assert(<fold text='...' expand='true'>true && true && true && true, ""</fold>);
  assert(<fold text='...' expand='true'>true && true && true && true, "message"</fold>);
  assert(<fold text='...' expand='true'>true &&
                                        true &&
                                        true &&
                                        true,
                                        "some message"</fold>);
}</fold>