;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (local $f f32) get_local f))
(;; STDERR ;;;
out/test/parse/expr/bad-getlocal-name.txt:3:40: error: unexpected token "f", expected a numeric index or a name (e.g. 12 or $foo).
(module (func (local $f f32) get_local f))
                                       ^
;;; STDERR ;;)