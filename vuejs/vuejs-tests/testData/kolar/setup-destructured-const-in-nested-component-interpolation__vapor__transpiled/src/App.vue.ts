/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const __VLS_export = ((
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
const { env, prNumber } = useAppConfig().buildInfo
const showLogo = computed(() => true)
const isSearchExpanded = computed(() => false)
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(showLogo, {} as import('vue').Ref<unknown>);
__VLS_withDotValue(isSearchExpanded, {} as import('vue').Ref<unknown>);
__VLS_withDotValue(prNumber, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({
});
if (showLogo.value && !isSearchExpanded.value && prNumber.value) {
let __VLS_0!: __VLS_WithComponent<'NuxtLink', __VLS_LocalComponents, __VLS_GlobalComponents, void, false, 'NuxtLink'>['NuxtLink'];
/** @ts-ignore @type { | typeof __VLS_components.NuxtLink | typeof __VLS_components.NuxtLink} */
NuxtLink;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
to: (`https:/*github.com/npmx-dev/npmx.dev/pull/${prNumber.value}`), */ 'aria-label': (__VLS_ctx.$t('header.pr', { prNumber: prNumber.value })), }));
const __VLS_2 = __VLS_1({
to: (`https://github.com/npmx-dev/npmx.dev/pull/${prNumber.value}`),
'aria-label': (__VLS_ctx.$t('header.pr', { prNumber: prNumber.value })),
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
const { default: __VLS_5 } = __VLS_nonNull(__VLS_3.slots);
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
...{ class: "text-xs px-1.5 py-0.5 rounded badge-green font-sans font-medium" },
});
/** @type {__VLS_StyleScopedClasses['text-xs']} */;
/** @type {__VLS_StyleScopedClasses['px-1.5']} */;
/** @type {__VLS_StyleScopedClasses['py-0.5']} */;
/** @type {__VLS_StyleScopedClasses['rounded']} */;
/** @type {__VLS_StyleScopedClasses['badge-green']} */;
/** @type {__VLS_StyleScopedClasses['font-sans']} */;
/** @type {__VLS_StyleScopedClasses['font-medium']} */;
( prNumber.value );
// @ts-ignore
[showLogo,isSearchExpanded,prNumber,prNumber,prNumber,prNumber,$t,];
}
// @ts-ignore
[];
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
