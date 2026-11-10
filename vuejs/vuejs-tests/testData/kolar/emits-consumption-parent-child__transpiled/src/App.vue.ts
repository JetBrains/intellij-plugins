/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'

let count = 1
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = import('vue').ShallowUnwrapRef<{
MyCounter: typeof MyCounter;
}>;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(count, {} as import('vue').Ref<unknown>);
( count.value );
const __VLS_0 = MyCounter;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
...{ 'onChange': {} as any }, ...{ 'onReset': {} as any }, }));
const __VLS_2 = __VLS_1({
...{ 'onChange': {} as any },
...{ 'onReset': {} as any },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
let __VLS_5!: __VLS_ResolveEmits<typeof __VLS_0, typeof __VLS_3.emit>;
const __VLS_6: Record<string, unknown> & Partial<__VLS_ResolveEvent<typeof __VLS_4, typeof __VLS_5, 'onChange', 'change', 'change'>> = {
/** @type {typeof __VLS_5.change} */
onChange: (...[$event]: __VLS_EventArgs<Record<string, unknown> & Partial<__VLS_ResolveEvent<typeof __VLS_4, typeof __VLS_5, 'onChange', 'change', 'change'>>, 'onChange'>) => {
void $event;
__VLS_withDotValue(count, {} as import('vue').Ref<unknown>);
return (count.value = $event);
// @ts-ignore
[count,count,];
},
};
void __VLS_6;
const __VLS_7: Record<string, unknown> & Partial<__VLS_ResolveEvent<typeof __VLS_4, typeof __VLS_5, 'onReset', 'reset', 'reset'>> = {
/** @type {typeof __VLS_5.reset} */
onReset: (...[$event]: __VLS_EventArgs<Record<string, unknown> & Partial<__VLS_ResolveEvent<typeof __VLS_4, typeof __VLS_5, 'onReset', 'reset', 'reset'>>, 'onReset'>) => {
void $event;
__VLS_withDotValue(count, {} as import('vue').Ref<unknown>);
return (count.value = 0);
// @ts-ignore
[count,];
},
};
void __VLS_7;
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
var __VLS_4!: __VLS_ExtractComponentProps<typeof __VLS_0, typeof __VLS_2>;
// @ts-ignore
[];
return {
};
};
void [__VLS_template];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
