(function () {
  "use strict";

// From another file
  function b() {
    "use strict";
    /* ... */
    myF();
    var a = bbb;
    <error descr="JSHint: Read only. (W020)">bbb</error> = 1;
  }
} ());
