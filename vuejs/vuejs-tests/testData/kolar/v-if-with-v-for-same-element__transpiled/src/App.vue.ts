/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const items = ['a', 'b', 'c']
const showList = ref(true)
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(showList, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]: __VLS_EventArgs<typeof __VLS_intrinsics['button'], 'onClick'>) => {
void $event;
return (showList.value = !showList.value);
// @ts-ignore
[showList,showList,];
}},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.ul, __VLS_intrinsics.ul)({
});
if (showList.value) {
const __VLS_0 = __VLS_tryAsConstant((__VLS_unwrap(items, {} as import('vue').Ref<unknown>)));
for (const [item] of __VLS_vFor(__VLS_nonNull(__VLS_0))) {
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({
key: (item),
});
( item );
// @ts-ignore
[showList,items,];
}
}
// @ts-ignore
[];
return {
};
};
void [__VLS_template];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
