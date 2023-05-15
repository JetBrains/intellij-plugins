;;; TOOL: wat2wasm
(type $v_v (func))
(type $v_ii (func (param i32 i32)))
(type $ii_v (func (result i32 i32)))
(type $ff_ff (func (param f32 f32) (result f32 f32)))

(func
  i32.const 0
  if (type $v_v)
  end)

(func
  i32.const 0
  i32.const 1
  i32.const 2
  if (type $v_ii)
    drop
    drop
  else
    drop
    drop
  end)

(func
  i32.const 0
  if (type $ii_v)
    i32.const 1
    i32.const 2
  else
    i32.const 3
    i32.const 4
  end
  drop
  drop)

(func
  f32.const 1
  f32.const 2
  i32.const 0
  if (type $ff_ff)
  end
  drop
  drop)