import type Card from './card.vue';
import type { ExtractPropTypes, StyleValue } from 'vue';
export declare const cardProps: {
    readonly header: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly bodyStyle: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<StyleValue>, "", unknown, unknown, unknown>;
    readonly shadow: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "always", unknown, unknown, unknown>;
};
export declare type CardProps = ExtractPropTypes<typeof cardProps>;
export declare type CardInstance = InstanceType<typeof Card>;
