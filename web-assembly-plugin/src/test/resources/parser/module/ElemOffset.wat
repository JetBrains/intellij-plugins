;;; TOOL: wat2wasm
(module
  (import "foo" "bar" (global i32))
  (global i32 i32.const 1)
  (func)
  (table 2 anyfunc)
  (elem (get_global 0) 0))