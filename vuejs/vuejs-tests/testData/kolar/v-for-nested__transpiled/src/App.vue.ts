/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const rows = [
  ['a1', 'a2'],
  ['b1', 'b2'],
]
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
rows: typeof rows;
}>;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as __VLS_SetupExposed,
};
type __VLS_LocalComponents = __VLS_SetupExposed;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = __VLS_SetupExposed;
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalElement1(__VLS_intrinsics.table, __VLS_intrinsics.table)({
});
for (const [row, rowIndex] of __VLS_vFor((__VLS_ctx.rows)!)) {
__VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
key: (rowIndex),
});
for (const [cell, colIndex] of __VLS_vFor((row)!)) {
__VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
key: (colIndex),
});
( cell );
// @ts-ignore
[rows,];
}
// @ts-ignore
[];
}
type __VLS_RootEl = 
| __VLS_Elements['table'];
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
