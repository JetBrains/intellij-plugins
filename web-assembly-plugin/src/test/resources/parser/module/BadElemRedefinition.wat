;;; TOOL: wat2wasm
;;; ARGS: --enable-bulk-memory
;;; ERROR: 1
(module
  (elem $elem funcref 0)
  (elem $elem funcref 0)
  (func))

(;; STDERR ;;;
out/test/parse/module/bad-elem-redefinition.txt:5:23: error: unexpected token 0, expected ).
  (elem $elem funcref 0)
                      ^
out/test/parse/module/bad-elem-redefinition.txt:6:23: error: unexpected token 0, expected ).
  (elem $elem funcref 0)
                      ^
;;; STDERR ;;)