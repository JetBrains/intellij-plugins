;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
  (local $n i32)
  i32.const 0
  set_local n))
(;; STDERR ;;;
out/test/parse/expr/bad-setlocal-name.txt:6:13: error: unexpected token "n", expected a numeric index or a name (e.g. 12 or $foo).
  set_local n))
            ^
;;; STDERR ;;)