/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const flags: Record<string, boolean> = { beta: true }
const key = 'beta'
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
flags: typeof flags;
key: typeof key;
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
if (__VLS_ctx.flags[__VLS_ctx.key]) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
type __VLS_RootEl = 
| __VLS_Elements['div'];
// @ts-ignore
[flags,key,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
