;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
  (local i32)
  set_local 0))
(;; STDERR ;;;
out/test/parse/expr/bad-setlocal-no-value.txt:5:3: error: type mismatch in local.set, expected [i32] but got []
  set_local 0))
  ^^^^^^^^^
;;; STDERR ;;)