;;; TOOL: wat2wasm
;;; ARGS: --enable-reference-types
(module
  (func (param externref)
    i32.const 0
    get_local 0
    table.set 0)

  (table 1 externref))