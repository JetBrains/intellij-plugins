/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const __VLS_export = (<T,>(
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
type __VLS_Props = { item: T };
const __VLS_props = defineProps<__VLS_Props>()
type __VLS_Emit = { select: [item: T] };
const __VLS_emit = defineEmits<__VLS_Emit>()
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_PublicProps = __VLS_Props;
type __VLS_EmitProps = __VLS_EmitsToProps<__VLS_NormalizeEmits<typeof __VLS_emit>>;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as { $emit: typeof __VLS_emit },
...{} as { $props: typeof __VLS_props & __VLS_EmitProps },
...{} as typeof __VLS_props & __VLS_EmitProps,
};
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]) => {
return __VLS_ctx.$emit('select', __VLS_ctx.$props.item);
// @ts-ignore
[$emit,$props,];
}},
});
( __VLS_ctx.$props.item );
type __VLS_RootEl = 
| __VLS_Elements['button'];
// @ts-ignore
[$props,];
return {} as {
	props: import('vue').PublicProps & __VLS_PrettifyLocal<__VLS_PublicProps & __VLS_EmitProps> & (typeof globalThis extends { __VLS_PROPS_FALLBACK: infer P } ? P : {});
	expose: (exposed: {}) => void;
	attrs: any;
	slots: {};
	emit: typeof __VLS_emit;
};
})(),
) => ({} as import('vue').VNode & { __ctx?: NonNullable<Awaited<typeof __VLS_setup>> }));
export default {} as typeof __VLS_export;
type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {};
