;;; TOOL: wat2wasm
;;; ERROR: 1
;;; ARGS: --enable-threads
(module
  (memory 1 1 shared)
  (func
    i32.const 0 i32.const 0 atomic.notify align=8 drop
    i32.const 0 i32.const 0 i64.const 0 i32.atomic.wait align=8 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.wait align=16 drop

    i32.const 0 i32.atomic.load align=8 drop
    i32.const 0 i64.atomic.load align=16 drop
    i32.const 0 i32.atomic.load8_u align=2 drop
    i32.const 0 i32.atomic.load16_u align=4 drop
    i32.const 0 i64.atomic.load8_u align=2 drop
    i32.const 0 i64.atomic.load16_u align=4 drop
    i32.const 0 i64.atomic.load32_u align=8 drop

    i32.const 0 i32.const 0 i32.atomic.store align=8
    i32.const 0 i64.const 0 i64.atomic.store align=16
    i32.const 0 i32.const 0 i32.atomic.store8 align=1
    i32.const 0 i32.const 0 i32.atomic.store16 align=4
    i32.const 0 i64.const 0 i64.atomic.store8 align=1
    i32.const 0 i64.const 0 i64.atomic.store16 align=4
    i32.const 0 i64.const 0 i64.atomic.store32 align=8

    i32.const 0 i32.const 0 i32.atomic.rmw.add align=8 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.add align=16 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.add_u align=2 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.add_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.add_u align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.add_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.add_u align=8 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.sub align=8 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.sub align=16 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.sub_u align=2 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.sub_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.sub_u align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.sub_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.sub_u align=8 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.and align=8 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.and align=16 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.and_u align=2 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.and_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.and_u align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.and_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.and_u align=8 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.or align=8 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.or align=16 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.or_u align=2 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.or_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.or_u align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.or_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.or_u align=8 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xor align=8 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xor align=16 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.xor_u align=2 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.xor_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.xor_u align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.xor_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.xor_u align=8 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xchg align=8 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xchg align=16 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8.xchg_u align=2 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16.xchg_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8.xchg_u align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16.xchg_u align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32.xchg_u align=8 drop

    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw.cmpxchg align=8 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw.cmpxchg align=16 drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw8.cmpxchg_u align=2 drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw16.cmpxchg_u align=4 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw8.cmpxchg_u align=2 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw16.cmpxchg_u align=4 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw32.cmpxchg_u align=8 drop

))

(;; STDERR ;;;
out/test/parse/expr/bad-atomic-unnatural-align.txt:7:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 atomic.notify align=8 drop
                            ^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:8:41: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i64.const 0 i32.atomic.wait align=8 drop
                                        ^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:9:41: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.wait align=16 drop
                                        ^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:11:17: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.atomic.load align=8 drop
                ^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:12:17: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.atomic.load align=16 drop
                ^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:13:17: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.atomic.load8_u align=2 drop
                ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:14:17: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.atomic.load16_u align=4 drop
                ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:15:17: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.atomic.load8_u align=2 drop
                ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:16:17: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.atomic.load16_u align=4 drop
                ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:17:17: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.atomic.load32_u align=8 drop
                ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:19:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.store align=8
                            ^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:20:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.store align=16
                            ^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:22:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.store16 align=4
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:24:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.store16 align=4
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:25:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.store32 align=8
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:27:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.rmw.add align=8 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:28:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.rmw.add align=16 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:29:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.atomic.rmw8.add_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:30:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.rmw16.add_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:31:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.atomic.rmw8.add_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:32:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.rmw16.add_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:33:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.rmw32.add_u align=8 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:35:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.rmw.sub align=8 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:36:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.rmw.sub align=16 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:37:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.atomic.rmw8.sub_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:38:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.rmw16.sub_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:39:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.atomic.rmw8.sub_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:40:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.rmw16.sub_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:41:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.rmw32.sub_u align=8 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:43:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.rmw.and align=8 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:44:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.rmw.and align=16 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:45:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.atomic.rmw8.and_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:46:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.rmw16.and_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:47:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.atomic.rmw8.and_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:48:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.rmw16.and_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:49:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.rmw32.and_u align=8 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:51:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.rmw.or align=8 drop
                            ^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:52:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.rmw.or align=16 drop
                            ^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:53:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.atomic.rmw8.or_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:54:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.rmw16.or_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:55:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.atomic.rmw8.or_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:56:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.rmw16.or_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:57:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.rmw32.or_u align=8 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:59:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.rmw.xor align=8 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:60:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.rmw.xor align=16 drop
                            ^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:61:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.atomic.rmw8.xor_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:62:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.rmw16.xor_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:63:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.atomic.rmw8.xor_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:64:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.rmw16.xor_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:65:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.rmw32.xor_u align=8 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:67:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.atomic.rmw.xchg align=8 drop
                            ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:68:29: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.atomic.rmw.xchg align=16 drop
                            ^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:69:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.atomic.rmw8.xchg_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:70:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.atomic.rmw16.xchg_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:71:29: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.atomic.rmw8.xchg_u align=2 drop
                            ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:72:29: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.atomic.rmw16.xchg_u align=4 drop
                            ^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:73:29: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.atomic.rmw32.xchg_u align=8 drop
                            ^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:75:41: error: alignment must be equal to natural alignment (4)
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw.cmpxchg align=8 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:76:41: error: alignment must be equal to natural alignment (8)
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw.cmpxchg align=16 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:77:41: error: alignment must be equal to natural alignment (1)
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw8.cmpxchg_u align=2 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:78:41: error: alignment must be equal to natural alignment (2)
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw16.cmpxchg_u align=4 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:79:41: error: alignment must be equal to natural alignment (1)
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw8.cmpxchg_u align=2 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:80:41: error: alignment must be equal to natural alignment (2)
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw16.cmpxchg_u align=4 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
out/test/parse/expr/bad-atomic-unnatural-align.txt:81:41: error: alignment must be equal to natural alignment (4)
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw32.cmpxchg_u align=8 drop
                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
;;; STDERR ;;)
