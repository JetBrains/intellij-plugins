/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const count = defineModel<number>('count')
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_ModelProps = {
'count'?: number;
};
type __VLS_ModelEmit = {
'update:count': [value: number | undefined];
};
let __VLS_modelEmit!: __VLS_ShortEmits<__VLS_ModelEmit>;
type __VLS_PublicProps = __VLS_ModelProps;
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
count: typeof count;
}>;
type __VLS_EmitProps = __VLS_EmitsToProps<__VLS_NormalizeEmits<typeof __VLS_modelEmit>>;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as { $emit: typeof __VLS_modelEmit },
...{} as { $props: __VLS_ModelProps & __VLS_EmitProps },
...{} as __VLS_ModelProps & __VLS_EmitProps,
...{} as __VLS_SetupExposed,
};
type __VLS_LocalComponents = __VLS_SetupExposed;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = __VLS_SetupExposed;
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
( __VLS_ctx.count );
type __VLS_RootEl = 
| __VLS_Elements['div'];
// @ts-ignore
[count,];
const __VLS_export = (await import('vue')).defineComponent({
__typeEmits: {} as __VLS_ModelEmit,
__typeProps: {} as __VLS_PublicProps,
});
export default {} as typeof __VLS_export;
