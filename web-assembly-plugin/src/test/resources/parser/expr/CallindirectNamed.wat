;;; TOOL: wat2wasm
(module
  (table anyfunc (elem 0))
  (type $t (func (param i32)))
  (func $g
    i32.const 0
    i32.const 0
    call_indirect (type $t)))