;;; TOOL: wat2wasm
(module
  (import "a" "global" (global i32))
  (global i32 (get_global 0)))