;;; TOOL: wat2wasm
(module
  (global $g i32 (i32.const 1))
  (func (result i32)
    get_global $g))