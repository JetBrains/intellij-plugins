;;; TOOL: wat2wasm
;;; ERROR: 1

(module
  (memory 1)
  (func
    i32.const 0 i32.const 0 atomic.notify drop
    i32.const 0 i32.const 0 i64.const 0 i32.atomic.wait drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.wait drop

    i32.const 0 i32.atomic.load drop
    i32.const 0 i64.atomic.load drop
    i32.const 0 i32.atomic.load8_u drop
    i32.const 0 i32.atomic.load16_u drop
    i32.const 0 i64.atomic.load8_u drop
    i32.const 0 i64.atomic.load16_u drop
    i32.const 0 i64.atomic.load32_u drop

    i32.const 0 i32.const 0 i32.atomic.store
    i32.const 0 i64.const 0 i64.atomic.store
    i32.const 0 i32.const 0 i32.atomic.store8
    i32.const 0 i32.const 0 i32.atomic.store16
    i32.const 0 i64.const 0 i64.atomic.store8
    i32.const 0 i64.const 0 i64.atomic.store16
    i32.const 0 i64.const 0 i64.atomic.store32

    i32.const 0 i32.const 0 i32.atomic.rmw.add drop
    i32.const 0 i64.const 0 i64.atomic.rmw.add drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.add_u drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.add_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.add_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.add_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.add_u drop

    i32.const 0 i32.const 0 i32.atomic.rmw.sub drop
    i32.const 0 i64.const 0 i64.atomic.rmw.sub drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.sub_u drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.sub_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.sub_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.sub_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.sub_u drop

    i32.const 0 i32.const 0 i32.atomic.rmw.and drop
    i32.const 0 i64.const 0 i64.atomic.rmw.and drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.and_u drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.and_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.and_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.and_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.and_u drop

    i32.const 0 i32.const 0 i32.atomic.rmw.or drop
    i32.const 0 i64.const 0 i64.atomic.rmw.or drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.or_u drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.or_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.or_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.or_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.or_u drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xor drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xor drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.xor_u drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.xor_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.xor_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.xor_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.xor_u drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xchg drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xchg drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.xchg_u drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.xchg_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.xchg_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.xchg_u drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.xchg_u drop

    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw.cmpxchg drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw.cmpxchg drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw8.cmpxchg_u drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw16.cmpxchg_u drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw8.cmpxchg_u drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw16.cmpxchg_u drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw32.cmpxchg_u drop

))
(;; STDERR ;;;
out/test/parse/expr/atomic-disabled.txt:7:29: error: opcode not allowed: atomic.notify
    i32.const 0 i32.const 0 atomic.notify drop
                            ^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:8:41: error: opcode not allowed: i32.atomic.wait
    i32.const 0 i32.const 0 i64.const 0 i32.atomic.wait drop
                                        ^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:9:41: error: opcode not allowed: i64.atomic.wait
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.wait drop
                                        ^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:11:17: error: opcode not allowed: i32.atomic.load
    i32.const 0 i32.atomic.load drop
                ^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:12:17: error: opcode not allowed: i64.atomic.load
    i32.const 0 i64.atomic.load drop
                ^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:13:17: error: opcode not allowed: i32.atomic.load8_u
    i32.const 0 i32.atomic.load8_u drop
                ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:14:17: error: opcode not allowed: i32.atomic.load16_u
    i32.const 0 i32.atomic.load16_u drop
                ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:15:17: error: opcode not allowed: i64.atomic.load8_u
    i32.const 0 i64.atomic.load8_u drop
                ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:16:17: error: opcode not allowed: i64.atomic.load16_u
    i32.const 0 i64.atomic.load16_u drop
                ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:17:17: error: opcode not allowed: i64.atomic.load32_u
    i32.const 0 i64.atomic.load32_u drop
                ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:19:29: error: opcode not allowed: i32.atomic.store
    i32.const 0 i32.const 0 i32.atomic.store
                            ^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:20:29: error: opcode not allowed: i64.atomic.store
    i32.const 0 i64.const 0 i64.atomic.store
                            ^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:21:29: error: opcode not allowed: i32.atomic.store8
    i32.const 0 i32.const 0 i32.atomic.store8
                            ^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:22:29: error: opcode not allowed: i32.atomic.store16
    i32.const 0 i32.const 0 i32.atomic.store16
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:23:29: error: opcode not allowed: i64.atomic.store8
    i32.const 0 i64.const 0 i64.atomic.store8
                            ^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:24:29: error: opcode not allowed: i64.atomic.store16
    i32.const 0 i64.const 0 i64.atomic.store16
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:25:29: error: opcode not allowed: i64.atomic.store32
    i32.const 0 i64.const 0 i64.atomic.store32
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:27:29: error: opcode not allowed: i32.atomic.rmw.add
    i32.const 0 i32.const 0 i32.atomic.rmw.add drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:28:29: error: opcode not allowed: i64.atomic.rmw.add
    i32.const 0 i64.const 0 i64.atomic.rmw.add drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:29:29: error: opcode not allowed: i32.atomic.rmw8.add_u
    i32.const 0 i32.const 0 i32.atomic.rmw8.add_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:30:29: error: opcode not allowed: i32.atomic.rmw16.add_u
    i32.const 0 i32.const 0 i32.atomic.rmw16.add_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:31:29: error: opcode not allowed: i64.atomic.rmw8.add_u
    i32.const 0 i64.const 0 i64.atomic.rmw8.add_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:32:29: error: opcode not allowed: i64.atomic.rmw16.add_u
    i32.const 0 i64.const 0 i64.atomic.rmw16.add_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:33:29: error: opcode not allowed: i64.atomic.rmw32.add_u
    i32.const 0 i64.const 0 i64.atomic.rmw32.add_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:35:29: error: opcode not allowed: i32.atomic.rmw.sub
    i32.const 0 i32.const 0 i32.atomic.rmw.sub drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:36:29: error: opcode not allowed: i64.atomic.rmw.sub
    i32.const 0 i64.const 0 i64.atomic.rmw.sub drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:37:29: error: opcode not allowed: i32.atomic.rmw8.sub_u
    i32.const 0 i32.const 0 i32.atomic.rmw8.sub_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:38:29: error: opcode not allowed: i32.atomic.rmw16.sub_u
    i32.const 0 i32.const 0 i32.atomic.rmw16.sub_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:39:29: error: opcode not allowed: i64.atomic.rmw8.sub_u
    i32.const 0 i64.const 0 i64.atomic.rmw8.sub_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:40:29: error: opcode not allowed: i64.atomic.rmw16.sub_u
    i32.const 0 i64.const 0 i64.atomic.rmw16.sub_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:41:29: error: opcode not allowed: i64.atomic.rmw32.sub_u
    i32.const 0 i64.const 0 i64.atomic.rmw32.sub_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:43:29: error: opcode not allowed: i32.atomic.rmw.and
    i32.const 0 i32.const 0 i32.atomic.rmw.and drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:44:29: error: opcode not allowed: i64.atomic.rmw.and
    i32.const 0 i64.const 0 i64.atomic.rmw.and drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:45:29: error: opcode not allowed: i32.atomic.rmw8.and_u
    i32.const 0 i32.const 0 i32.atomic.rmw8.and_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:46:29: error: opcode not allowed: i32.atomic.rmw16.and_u
    i32.const 0 i32.const 0 i32.atomic.rmw16.and_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:47:29: error: opcode not allowed: i64.atomic.rmw8.and_u
    i32.const 0 i64.const 0 i64.atomic.rmw8.and_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:48:29: error: opcode not allowed: i64.atomic.rmw16.and_u
    i32.const 0 i64.const 0 i64.atomic.rmw16.and_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:49:29: error: opcode not allowed: i64.atomic.rmw32.and_u
    i32.const 0 i64.const 0 i64.atomic.rmw32.and_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:51:29: error: opcode not allowed: i32.atomic.rmw.or
    i32.const 0 i32.const 0 i32.atomic.rmw.or drop
                            ^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:52:29: error: opcode not allowed: i64.atomic.rmw.or
    i32.const 0 i64.const 0 i64.atomic.rmw.or drop
                            ^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:53:29: error: opcode not allowed: i32.atomic.rmw8.or_u
    i32.const 0 i32.const 0 i32.atomic.rmw8.or_u drop
                            ^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:54:29: error: opcode not allowed: i32.atomic.rmw16.or_u
    i32.const 0 i32.const 0 i32.atomic.rmw16.or_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:55:29: error: opcode not allowed: i64.atomic.rmw8.or_u
    i32.const 0 i64.const 0 i64.atomic.rmw8.or_u drop
                            ^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:56:29: error: opcode not allowed: i64.atomic.rmw16.or_u
    i32.const 0 i64.const 0 i64.atomic.rmw16.or_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:57:29: error: opcode not allowed: i64.atomic.rmw32.or_u
    i32.const 0 i64.const 0 i64.atomic.rmw32.or_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:59:29: error: opcode not allowed: i32.atomic.rmw.xor
    i32.const 0 i32.const 0 i32.atomic.rmw.xor drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:60:29: error: opcode not allowed: i64.atomic.rmw.xor
    i32.const 0 i64.const 0 i64.atomic.rmw.xor drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:61:29: error: opcode not allowed: i32.atomic.rmw8.xor_u
    i32.const 0 i32.const 0 i32.atomic.rmw8.xor_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:62:29: error: opcode not allowed: i32.atomic.rmw16.xor_u
    i32.const 0 i32.const 0 i32.atomic.rmw16.xor_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:63:29: error: opcode not allowed: i64.atomic.rmw8.xor_u
    i32.const 0 i64.const 0 i64.atomic.rmw8.xor_u drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:64:29: error: opcode not allowed: i64.atomic.rmw16.xor_u
    i32.const 0 i64.const 0 i64.atomic.rmw16.xor_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:65:29: error: opcode not allowed: i64.atomic.rmw32.xor_u
    i32.const 0 i64.const 0 i64.atomic.rmw32.xor_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:67:29: error: opcode not allowed: i32.atomic.rmw.xchg
    i32.const 0 i32.const 0 i32.atomic.rmw.xchg drop
                            ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:68:29: error: opcode not allowed: i64.atomic.rmw.xchg
    i32.const 0 i64.const 0 i64.atomic.rmw.xchg drop
                            ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:69:29: error: opcode not allowed: i32.atomic.rmw8.xchg_u
    i32.const 0 i32.const 0 i32.atomic.rmw8.xchg_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:70:29: error: opcode not allowed: i32.atomic.rmw16.xchg_u
    i32.const 0 i32.const 0 i32.atomic.rmw16.xchg_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:71:29: error: opcode not allowed: i64.atomic.rmw8.xchg_u
    i32.const 0 i64.const 0 i64.atomic.rmw8.xchg_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:72:29: error: opcode not allowed: i64.atomic.rmw16.xchg_u
    i32.const 0 i64.const 0 i64.atomic.rmw16.xchg_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:73:29: error: opcode not allowed: i64.atomic.rmw32.xchg_u
    i32.const 0 i64.const 0 i64.atomic.rmw32.xchg_u drop
                            ^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:75:41: error: opcode not allowed: i32.atomic.rmw.cmpxchg
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw.cmpxchg drop
                                        ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:76:41: error: opcode not allowed: i64.atomic.rmw.cmpxchg
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw.cmpxchg drop
                                        ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:77:41: error: opcode not allowed: i32.atomic.rmw8.cmpxchg_u
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw8.cmpxchg_u drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:78:41: error: opcode not allowed: i32.atomic.rmw16.cmpxchg_u
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw16.cmpxchg_u drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:79:41: error: opcode not allowed: i64.atomic.rmw8.cmpxchg_u
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw8.cmpxchg_u drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:80:41: error: opcode not allowed: i64.atomic.rmw16.cmpxchg_u
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw16.cmpxchg_u drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/atomic-disabled.txt:81:41: error: opcode not allowed: i64.atomic.rmw32.cmpxchg_u
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw32.cmpxchg_u drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
;;; STDERR ;;)
