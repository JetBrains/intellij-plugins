/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const word = 'abc'
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
word: typeof word;
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
__VLS_asFunctionalElement1(__VLS_intrinsics.ul, __VLS_intrinsics.ul)({
});
for (const [char, index] of __VLS_vFor((__VLS_ctx.word)!)) {
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({
key: (index),
});
( char );
// @ts-ignore
[word,];
}
type __VLS_RootEl = 
| __VLS_Elements['ul'];
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
