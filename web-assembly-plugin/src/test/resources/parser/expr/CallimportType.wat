;;; TOOL: wat2wasm
(module
  (type (func (param i32) (result i32)))
  (import "foo" "bar" (func (type 0)))
  (func (param i32) (result i32)
    i32.const 0
    call 0))