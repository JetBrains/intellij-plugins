/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

type __VLS_Emit = {
  change: [id: number]
  reset: []
};
const emit = defineEmits<__VLS_Emit>()

function onChange() {
  emit('change', 1)
}
type __VLS_EmitProps = __VLS_EmitsToProps<__VLS_NormalizeEmits<typeof emit>>;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as { $emit: typeof emit },
...{} as { $props: __VLS_EmitProps },
...{} as __VLS_EmitProps,
};
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (onChange)},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (__VLS_unwrap(emit, {} as import('vue').Ref<unknown>)('change', 1));
// @ts-ignore
[emit,];
}},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (__VLS_unwrap(emit, {} as import('vue').Ref<unknown>)('reset'));
// @ts-ignore
[emit,];
}},
});
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
__typeEmits: {} as __VLS_Emit,
});
export default {} as typeof __VLS_export;
import { defineEmits, } from 'vue';
