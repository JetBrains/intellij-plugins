;;; TOOL: wat2wasm
(module
  (table anyfunc (elem 0))
  (type (func (param i32)))
  (func
    i32.const 0
    i32.const 0
    call_indirect (type 0)))