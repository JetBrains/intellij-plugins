;;; TOOL: wat2wasm
;;; ARGS: --enable-reference-types
(module
  (func (result i32)
    ref.null extern
    i32.const 0
    table.grow 0)

  (table 1 externref))