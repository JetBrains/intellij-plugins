/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const show = ref(true)
const items = [1, 2, 3]
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
// @ts-ignore
__VLS_withDotValue(show, {} as import('vue').Ref<unknown>);
// @ts-ignore
__VLS_withDotValue(items, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: // @ts-ignore
(...[$event]) => {
void $event;
return (show.value = !show.value);
// @ts-ignore
[show,show,];
}},
});
if (show.value) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
__VLS_asFunctionalDirective(__VLS_directives.vMemo, {} as import('vue').ObjectDirective)(__VLS_nonNull(null), { ...__VLS_directiveBindingRestFields, value: ([items.value.length]), },
__VLS_nonNull(null), __VLS_nonNull(null));
( items.value.length );
}
// @ts-ignore
[show,items,items,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
