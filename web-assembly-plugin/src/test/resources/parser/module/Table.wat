;;; TOOL: wat2wasm
(module
  (func (param i32))
  (func (param i32 i64))
  (func (result i64) i64.const 0)
  (table anyfunc (elem 0 0 1 2)))