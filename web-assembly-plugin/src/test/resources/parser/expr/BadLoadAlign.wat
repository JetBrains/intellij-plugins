;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (i32.load align=foo (i32.const 0))))
(;; STDERR ;;;
out/test/parse/expr/bad-load-align.txt:3:25: error: unexpected token align=foo, expected ).
(module (func (i32.load align=foo (i32.const 0))))
                        ^^^^^^^^^
out/test/parse/expr/bad-load-align.txt:3:50: error: unexpected token ), expected EOF.
(module (func (i32.load align=foo (i32.const 0))))
                                                 ^
;;; STDERR ;;)