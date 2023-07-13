import type { ExtractPropTypes } from 'vue';
import type Alert from './alert.vue';
export declare const alertEffects: readonly ["light", "dark"];
export declare const alertProps: {
    readonly title: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly description: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "info", unknown, "success" | "warning" | "info" | "error", unknown>;
    readonly closable: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly closeText: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly showIcon: BooleanConstructor;
    readonly center: BooleanConstructor;
    readonly effect: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "light", unknown, "dark" | "light", unknown>;
};
export declare type AlertProps = ExtractPropTypes<typeof alertProps>;
export declare const alertEmits: {
    close: (evt: MouseEvent) => boolean;
};
export declare type AlertEmits = typeof alertEmits;
export declare type AlertInstance = InstanceType<typeof Alert>;
