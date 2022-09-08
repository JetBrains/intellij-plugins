import { PropType } from 'vue';
import type { OffsetTarget } from './utils';
export interface BaseAnchorInst {
    setActiveHref: (href: string) => void;
}
export declare const baseAnchorProps: {
    readonly type: {
        readonly type: PropType<"block" | "rail">;
        readonly default: "rail";
    };
    readonly showRail: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    readonly showBackground: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    readonly bound: {
        readonly type: NumberConstructor;
        readonly default: 12;
    };
    readonly internalScrollable: BooleanConstructor;
    readonly ignoreGap: BooleanConstructor;
    readonly offsetTarget: PropType<string | OffsetTarget | (() => HTMLElement)>;
};
export declare const baseAnchorPropKeys: ("type" | "bound" | "showRail" | "showBackground" | "internalScrollable" | "ignoreGap" | "offsetTarget")[];
declare const _default: import("vue").DefineComponent<{
    mergedClsPrefix: {
        type: StringConstructor;
        required: true;
    };
    type: {
        readonly type: PropType<"block" | "rail">;
        readonly default: "rail";
    };
    showRail: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    showBackground: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    bound: {
        readonly type: NumberConstructor;
        readonly default: 12;
    };
    internalScrollable: BooleanConstructor;
    ignoreGap: BooleanConstructor;
    offsetTarget: PropType<string | OffsetTarget | (() => HTMLElement)>;
}, {
    selfRef: import("vue").Ref<HTMLElement | null>;
    barRef: import("vue").Ref<HTMLElement | null>;
    slotRef: import("vue").Ref<HTMLElement | null>;
    setActiveHref: (href: string, transition?: boolean) => void;
    activeHref: import("vue").Ref<string | null>;
    isBlockType: import("vue").ComputedRef<boolean>;
    mergedShowRail: import("vue").ComputedRef<boolean>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    mergedClsPrefix: {
        type: StringConstructor;
        required: true;
    };
    type: {
        readonly type: PropType<"block" | "rail">;
        readonly default: "rail";
    };
    showRail: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    showBackground: {
        readonly type: BooleanConstructor;
        readonly default: true;
    };
    bound: {
        readonly type: NumberConstructor;
        readonly default: 12;
    };
    internalScrollable: BooleanConstructor;
    ignoreGap: BooleanConstructor;
    offsetTarget: PropType<string | OffsetTarget | (() => HTMLElement)>;
}>>, {
    type: "block" | "rail";
    bound: number;
    showRail: boolean;
    showBackground: boolean;
    internalScrollable: boolean;
    ignoreGap: boolean;
}>;
export default _default;
