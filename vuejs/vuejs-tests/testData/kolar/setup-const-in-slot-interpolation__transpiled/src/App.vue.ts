/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const route = useRoute()
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(route, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
let __VLS_0!: __VLS_WithComponent<'NuxtRouteAnnouncer', __VLS_LocalComponents, __VLS_GlobalComponents, void, false, 'NuxtRouteAnnouncer'>['NuxtRouteAnnouncer'];
/** @ts-ignore @type { | typeof __VLS_components.NuxtRouteAnnouncer | typeof __VLS_components.NuxtRouteAnnouncer} */
NuxtRouteAnnouncer;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
}));
const __VLS_2 = __VLS_1({
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
{
const { default: __VLS_5 } = __VLS_nonNull(__VLS_3.slots);
const [{ message }] = __VLS_vSlot(__VLS_nonNull(__VLS_5));
( route.value.name === 'search' ? `${__VLS_ctx.$t('search.title_packages')} - npmx` : message );
// @ts-ignore
[route,$t,];
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
