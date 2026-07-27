/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const status = 'loading'
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
status: typeof status;
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
if (__VLS_ctx.status === 'loading') {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
else if (__VLS_ctx.status === 'error') {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
else if (__VLS_ctx.status === 'empty') {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
else {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
type __VLS_RootEl = 
| __VLS_Elements['div'];
// @ts-ignore
[status,status,status,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
