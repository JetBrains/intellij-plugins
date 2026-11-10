/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const __VLS_export = ((
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
const show = ref(true)
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(show, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]: __VLS_EventArgs<typeof __VLS_intrinsics['button'], 'onClick'>) => {
void $event;
return (show.value = !show.value);
// @ts-ignore
[show,show,];
}},
});
if (show.value) {
__VLS_asFunctionalElement1(__VLS_intrinsics.template)({
key: ('a'),
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
else {
__VLS_asFunctionalElement1(__VLS_intrinsics.template)({
key: ('b'),
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
}
// @ts-ignore
[show,];
return {
};
};
void [__VLS_template];
return {} as {
	props: import('vue').PublicProps & (typeof globalThis extends { __VLS_PROPS_FALLBACK: infer P } ? P : {});
	expose: (exposed: {}) => void;
	attrs: any;
	slots: {};
	emit: {};
};
})(),
) => ({} as import('vue').VNode & { __ctx?: NonNullable<Awaited<typeof __VLS_setup>> }));
export default {} as typeof __VLS_export;
type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {};
