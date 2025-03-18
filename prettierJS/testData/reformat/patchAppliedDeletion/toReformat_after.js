function f2(b) {
  function f3(fn) {
    setTimeout(fn, 0);
  }

  function f4(fn) {
    setTimeout(
      fn,

      0,
    );
  }
}
