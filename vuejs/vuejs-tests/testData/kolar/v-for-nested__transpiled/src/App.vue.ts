/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const rows = [
  ['a1', 'a2'],
  ['b1', 'b2'],
]
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_asFunctionalElement1(__VLS_intrinsics.table, __VLS_intrinsics.table)({
});
const __VLS_0 = __VLS_tryAsConstant((__VLS_unwrap(rows, {} as import('vue').Ref<unknown>)));
for (const [row, rowIndex] of __VLS_vFor(__VLS_nonNull(__VLS_0))) {
__VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
key: (rowIndex),
});
const __VLS_1 = __VLS_tryAsConstant((row));
for (const [cell, colIndex] of __VLS_vFor(__VLS_nonNull(__VLS_1))) {
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
// @ts-ignore
[];
return {
};
};
void [__VLS_template];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
