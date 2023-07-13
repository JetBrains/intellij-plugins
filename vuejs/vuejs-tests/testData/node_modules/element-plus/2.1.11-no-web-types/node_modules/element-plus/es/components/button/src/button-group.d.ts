import type { ExtractPropTypes } from 'vue';
import type buttonGroup from './button-group.vue';
export declare const buttonGroupProps: {
    readonly size: import("../../../utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
    readonly type: import("../../../utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
};
export declare type ButtonGroupProps = ExtractPropTypes<typeof buttonGroupProps>;
export declare type ButtonGroupInstance = InstanceType<typeof buttonGroup>;
