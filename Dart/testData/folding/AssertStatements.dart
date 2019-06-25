// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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