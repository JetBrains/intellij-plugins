/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'

const names = ['header', 'footer'] as const
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
names: typeof names;
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
const __VLS_0 = MyCounter || MyCounter;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
}));
const __VLS_2 = __VLS_1({
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
var __VLS_5!: Parameters<NonNullable<typeof __VLS_3['expose']>>[0];
const { default: __VLS_6 } = __VLS_3.slots!;
for (const [name] of __VLS_vFor((__VLS_ctx.names)!)) {
{
const { [__VLS_tryAsConstant(name)]: __VLS_7 } = __VLS_3.slots!;
const [slotProps] = __VLS_vSlot(__VLS_7!);
( name );
( slotProps.value );
// @ts-ignore
[names,];
}
// @ts-ignore
[];
}
// @ts-ignore
[];
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
type __VLS_RootEl = 
| NonNullable<typeof __VLS_5>['$el'];
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
