;;; TOOL: wat2wasm
(module
  (global (mut f32) (f32.const 1))
  (func
    f32.const 2
    set_global 0))