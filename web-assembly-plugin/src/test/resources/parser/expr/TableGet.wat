;;; TOOL: wat2wasm
;;; ARGS: --enable-reference-types
(module
  (func (result externref)
    i32.const 0
    table.get 0)

  (table 1 externref))