;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (if (i32.const 0))))
(;; STDERR ;;;
out/test/parse/expr/bad-if-no-then.txt:3:32: error: unexpected token ")", expected then block (e.g. (then ...)).
(module (func (if (i32.const 0))))
                               ^
;;; STDERR ;;)