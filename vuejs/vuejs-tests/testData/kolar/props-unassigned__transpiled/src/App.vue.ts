/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

type __VLS_Props = { count: number };
const __VLS_props = defineProps<__VLS_Props>()
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_PublicProps = __VLS_Props;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as { $props: typeof __VLS_props },
...{} as typeof __VLS_props,
};
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
( __VLS_ctx.$props.count );
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
title: (__VLS_ctx.$props.count),
});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
});
( __VLS_ctx.$props.count );
// @ts-ignore
[$props,$props,$props,];
const __VLS_export = (await import('vue')).defineComponent({
__typeProps: {} as __VLS_PublicProps,
});
export default {} as typeof __VLS_export;
