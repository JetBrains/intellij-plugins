;;; TOOL: wat2wasm
(module
  (func $foo (param f32)
    f32.const 0.0
    call $foo))