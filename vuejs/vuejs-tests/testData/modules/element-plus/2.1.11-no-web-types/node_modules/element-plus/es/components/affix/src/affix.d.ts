import type { ExtractPropTypes } from 'vue';
import type { ZIndexProperty } from 'csstype';
import type Affix from './affix.vue';
export declare const affixProps: {
    readonly zIndex: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<ZIndexProperty>, 100, unknown, unknown, unknown>;
    readonly target: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly offset: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
    readonly position: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "top", unknown, "top" | "bottom", unknown>;
};
export declare type AffixProps = ExtractPropTypes<typeof affixProps>;
export declare const affixEmits: {
    scroll: ({ scrollTop, fixed }: {
        scrollTop: number;
        fixed: boolean;
    }) => boolean;
    change: (fixed: boolean) => boolean;
};
export declare type AffixEmits = typeof affixEmits;
export declare type AffixInstance = InstanceType<typeof Affix>;
