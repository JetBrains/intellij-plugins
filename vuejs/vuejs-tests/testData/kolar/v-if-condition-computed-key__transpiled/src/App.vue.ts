/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const flags: Record<string, boolean> = { beta: true }
const key = 'beta'
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(flags, {} as import('vue').Ref<unknown>);
if (flags.value[__VLS_unwrap(key, {} as import('vue').Ref<unknown>)]) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
// @ts-ignore
[flags,key,];
return {
};
};
void [__VLS_template];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
