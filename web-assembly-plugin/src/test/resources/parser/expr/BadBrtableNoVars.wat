;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func br_table))
(;; STDERR ;;;
out/test/parse/expr/bad-brtable-no-vars.txt:3:23: error: unexpected token ")", expected a var (e.g. 12 or $foo).
(module (func br_table))
                      ^
;;; STDERR ;;)