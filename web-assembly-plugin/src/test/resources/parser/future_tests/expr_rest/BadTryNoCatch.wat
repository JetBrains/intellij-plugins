;;; TOOL: wat2wasm
;;; ARGS: --enable-exceptions
;;; ERROR: 1
(module
  (func try nop end)
  (func (try (do nop))))
(;; STDERR ;;;
out/test/parse/expr/bad-try-no-catch.txt:5:17: error: unexpected token end, expected catch.
  (func try nop end)
                ^^^
out/test/parse/expr/bad-try-no-catch.txt:6:22: error: unexpected token ), expected (.
  (func (try (do nop))))
                     ^
;;; STDERR ;;)
