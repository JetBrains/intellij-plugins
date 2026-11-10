/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const { defineEmits, } = {} as typeof import('vue');
const emit = defineEmits({
  submit: (payload: { email: string }) => payload.email != null,
  click: null,
})

function onSubmit() {
  emit('submit', { email: 'a@b.com' })
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
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (__VLS_unwrap(emit, {} as import('vue').Ref<unknown>)('click'));
// @ts-ignore
[emit,];
}},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (onSubmit)},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (__VLS_unwrap(emit, {} as import('vue').Ref<unknown>)('submit', { email: 'a@b.com' }));
// @ts-ignore
[emit,];
}},
});
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
emits: {} as __VLS_NormalizeEmits<typeof emit>,
});
export default {} as typeof __VLS_export;
