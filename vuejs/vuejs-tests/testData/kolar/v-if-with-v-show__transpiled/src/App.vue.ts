/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const showIf = ref(true)
const showShow = ref(true)
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
// @ts-ignore
__VLS_withDotValue(showIf, {} as import('vue').Ref<unknown>);
// @ts-ignore
__VLS_withDotValue(showShow, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (showIf.value = !showIf.value);
// @ts-ignore
[showIf,showIf,];
}},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (showShow.value = !showShow.value);
// @ts-ignore
[showShow,showShow,];
}},
});
if (showIf.value) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
__VLS_asFunctionalDirective(__VLS_directives.vShow, {} as import('vue').ObjectDirective)(__VLS_nonNull(null), { ...__VLS_directiveBindingRestFields, value: (showShow.value), },
__VLS_nonNull(null), __VLS_nonNull(null));
}
// @ts-ignore
[showIf,showShow,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
