dynamic class A {
  function xxx() {
      if (<caret>) {
          this.model = null;
          // trace("this.model: "+model);
          this.name = null;
      }
  }
}