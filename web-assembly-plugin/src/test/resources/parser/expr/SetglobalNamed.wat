;;; TOOL: wat2wasm
(module
  (global $g (mut f32) (f32.const 1))
  (func
    f32.const 2
    set_global $g))