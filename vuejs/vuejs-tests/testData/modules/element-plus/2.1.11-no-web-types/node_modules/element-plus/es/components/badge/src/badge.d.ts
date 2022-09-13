import type { ExtractPropTypes } from 'vue';
import type Badge from './badge.vue';
export declare const badgeProps: {
    readonly value: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
    readonly max: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 99, unknown, unknown, unknown>;
    readonly isDot: BooleanConstructor;
    readonly hidden: BooleanConstructor;
    readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "danger", unknown, "primary" | "success" | "warning" | "info" | "danger", unknown>;
};
export declare type BadgeProps = ExtractPropTypes<typeof badgeProps>;
export declare type BadgeInstance = InstanceType<typeof Badge>;
