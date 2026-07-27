/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

type __VLS_Props = { count: number };
const __VLS_props = defineProps<__VLS_Props>();
const { count } = __VLS_props

type __VLS_Emit = {
  change: [id: number]
};
const emit = defineEmits<__VLS_Emit>()

function onChange() {
  emit('change', count)
}
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_PublicProps = __VLS_Props;
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
onChange: typeof onChange;
emit: typeof emit;
}>;
type __VLS_EmitProps = __VLS_EmitsToProps<__VLS_NormalizeEmits<typeof emit>>;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as { $emit: typeof emit },
...{} as { $props: typeof __VLS_props & __VLS_EmitProps },
...{} as typeof __VLS_props & __VLS_EmitProps,
...{} as __VLS_SetupExposed,
};
type __VLS_LocalComponents = __VLS_SetupExposed;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = __VLS_SetupExposed;
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
( count );
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (__VLS_ctx.onChange)},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]) => {
return __VLS_ctx.emit('change', count);
// @ts-ignore
[onChange,emit,];
}},
});
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
__typeEmits: {} as __VLS_Emit,
__typeProps: {} as __VLS_PublicProps,
});
export default {} as typeof __VLS_export;
