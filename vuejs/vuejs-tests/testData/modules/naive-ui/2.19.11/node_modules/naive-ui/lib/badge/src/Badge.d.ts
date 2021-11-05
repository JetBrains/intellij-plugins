import { PropType } from 'vue';
import type { ExtractPublicPropTypes } from '../../_utils';
declare const badgeProps: {
    readonly value: PropType<string | number>;
    readonly max: NumberConstructor;
    readonly dot: {
        readonly type: BooleanConstructor;
        readonly default: false;
    };
    readonly type: {
        readonly type: PropType<"default" | "error" | "info" | "success" | "warning">;
        readonly default: "default";
    };
    readonly show: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    readonly showZero: {
        readonly type: BooleanConstructor;
        readonly default: false;
    };
    readonly processing: {
        readonly type: BooleanConstructor;
        readonly default: false;
    };
    readonly color: StringConstructor;
    readonly theme: PropType<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>>;
    readonly themeOverrides: PropType<import("../../_mixins/use-theme").ExtractThemeOverrides<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>>>;
    readonly builtinThemeOverrides: PropType<import("../../_mixins/use-theme").ExtractThemeOverrides<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>>>;
};
export declare type BadgeProps = ExtractPublicPropTypes<typeof badgeProps>;
declare const _default: import("vue").DefineComponent<{
    readonly value: PropType<string | number>;
    readonly max: NumberConstructor;
    readonly dot: {
        readonly type: BooleanConstructor;
        readonly default: false;
    };
    readonly type: {
        readonly type: PropType<"default" | "error" | "info" | "success" | "warning">;
        readonly default: "default";
    };
    readonly show: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    readonly showZero: {
        readonly type: BooleanConstructor;
        readonly default: false;
    };
    readonly processing: {
        readonly type: BooleanConstructor;
        readonly default: false;
    };
    readonly color: StringConstructor;
    readonly theme: PropType<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>>;
    readonly themeOverrides: PropType<import("../../_mixins/use-theme").ExtractThemeOverrides<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>>>;
    readonly builtinThemeOverrides: PropType<import("../../_mixins/use-theme").ExtractThemeOverrides<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>>>;
}, {
    mergedClsPrefix: import("vue").ComputedRef<string>;
    appeared: import("vue").Ref<boolean>;
    showBadge: import("vue").ComputedRef<boolean>;
    handleAfterEnter: () => void;
    handleAfterLeave: () => void;
    cssVars: import("vue").ComputedRef<{
        '--font-size': string;
        '--font-family': string;
        '--color': string;
        '--ripple-color': string;
        '--bezier': string;
        '--ripple-bezier': string;
    }>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<{
    readonly value?: unknown;
    readonly max?: unknown;
    readonly dot?: unknown;
    readonly type?: unknown;
    readonly show?: unknown;
    readonly showZero?: unknown;
    readonly processing?: unknown;
    readonly color?: unknown;
    readonly theme?: unknown;
    readonly themeOverrides?: unknown;
    readonly builtinThemeOverrides?: unknown;
} & {
    type: "default" | "error" | "info" | "success" | "warning";
    show: boolean;
    dot: boolean;
    showZero: boolean;
    processing: boolean;
} & {
    value?: string | number | undefined;
    color?: string | undefined;
    max?: number | undefined;
    theme?: import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any> | undefined;
    themeOverrides?: import("../../_mixins/use-theme").ExtractThemeOverrides<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>> | undefined;
    builtinThemeOverrides?: import("../../_mixins/use-theme").ExtractThemeOverrides<import("../../_mixins").Theme<"Badge", {
        color: string;
        colorInfo: string;
        colorSuccess: string;
        colorError: string;
        colorWarning: string;
        fontSize: string;
        fontFamily: string;
    }, any>> | undefined;
}>, {
    type: "default" | "error" | "info" | "success" | "warning";
    show: boolean;
    dot: boolean;
    showZero: boolean;
    processing: boolean;
}>;
export default _default;
