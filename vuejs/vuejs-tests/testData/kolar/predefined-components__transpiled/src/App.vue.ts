/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
});
let __VLS_0!: __VLS_WithComponent<'RouterView', __VLS_LocalComponents, __VLS_GlobalComponents, void, 'RouterView'>['RouterView'];
/** @ts-ignore @type { | typeof __VLS_components.RouterView | typeof __VLS_components.RouterView} */
RouterView;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
}));
const __VLS_2 = __VLS_1({
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
{
const { default: __VLS_5 } = __VLS_3.slots!;
const [{ Component }] = __VLS_vSlot(__VLS_5!);
let __VLS_6!: __VLS_WithComponent<'Transition', __VLS_LocalComponents, __VLS_GlobalComponents, void, 'Transition'>['Transition'];
/** @ts-ignore @type { | typeof __VLS_components.Transition | typeof __VLS_components.Transition} */
Transition;
// @ts-ignore
const __VLS_7 = __VLS_asFunctionalComponent1(__VLS_6, new __VLS_6({
name: "fade",
mode: "out-in",
}));
const __VLS_8 = __VLS_7({
name: "fade",
mode: "out-in",
}, ...__VLS_functionalComponentArgsRest(__VLS_7));
__VLS_3.slots![''/* empty slot name completion */];
}
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
type __VLS_RootEl = 
| __VLS_Elements['main'];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
