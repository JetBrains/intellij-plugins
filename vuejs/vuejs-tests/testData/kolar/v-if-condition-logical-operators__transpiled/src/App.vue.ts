/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const isLoggedIn = ref(true)
const isAdmin = ref(false)
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
isLoggedIn: typeof isLoggedIn;
isAdmin: typeof isAdmin;
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
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]) => {
return __VLS_ctx.isLoggedIn = !__VLS_ctx.isLoggedIn;
// @ts-ignore
[isLoggedIn,isLoggedIn,];
}},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]) => {
return __VLS_ctx.isAdmin = !__VLS_ctx.isAdmin;
// @ts-ignore
[isAdmin,isAdmin,];
}},
});
if (__VLS_ctx.isLoggedIn && __VLS_ctx.isAdmin) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
else if (__VLS_ctx.isLoggedIn || __VLS_ctx.isAdmin) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
// @ts-ignore
[isLoggedIn,isLoggedIn,isAdmin,isAdmin,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
