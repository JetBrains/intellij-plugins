;;; RUN: %(wat2wasm)s -o %(out_dir)s/tmp.wasm
;;; ARGS: -
;;; STDIN: %(in_file)s
;;; RUN: %(wasm-objdump)s -x
;;; STDIN: %(out_dir)s/tmp.wasm
;;; ARGS: -
(module
  (func (result i32)
    i32.const 42
    return)
)
(;; STDOUT ;;;

<stdin>:	file format wasm 0x1

Section Details:

Type[1]:
 - type[0] () -> i32
Function[1]:
 - func[0] sig=0
Code[1]:
 - func[0] size=5
;;; STDOUT ;;)