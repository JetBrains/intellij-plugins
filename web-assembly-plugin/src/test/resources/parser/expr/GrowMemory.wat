;;; TOOL: wat2wasm
(module
  (memory 1)
  (func
    i32.const 100
    grow_memory
    drop))