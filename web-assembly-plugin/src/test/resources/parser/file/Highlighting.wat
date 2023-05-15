(module $color-painter
  (export "getHeight" (func $getHeight))
  (export "getWidth" (func $getWidth))
  (export "memory" (memory $mem))
  (export "setDimensions" (func $setDimensions))

  (type $getI32 (func (result i32)))

  (memory $mem 4)

  (global $bytesPerPage i32 i32.const 0x10000)
  (global $bytesPerPixel i32 i32.const 4)
  (global $false i32 i32.const 0)
  (global $true i32 i32.const 1)

  (global $height (mut i32) (i32.const 0))
  (global $width (mut i32) (i32.const 0))

  (func $getHeight (type $getI32) (global.set $height))
  (func $getWidth (type $getI32) (global.set $width))

  (func $setDimentions (param $width i32) (param $height i32) (result i32)
    (local $bytes i32)
    (local $pixels i32)
    (local $pages i32)
    (local $success i32)

    (local.set $success (global.get $true))

    (local.set $pixels
      (i32.mul
        (local.get $width)
        (local.get $height)))
  )
)