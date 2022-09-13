import type { ExtractPropTypes } from 'vue';
export declare const backtopProps: {
    readonly visibilityHeight: {
        readonly type: NumberConstructor;
        readonly default: 200;
    };
    readonly target: {
        readonly type: StringConstructor;
        readonly default: "";
    };
    readonly right: {
        readonly type: NumberConstructor;
        readonly default: 40;
    };
    readonly bottom: {
        readonly type: NumberConstructor;
        readonly default: 40;
    };
};
export declare type BacktopProps = ExtractPropTypes<typeof backtopProps>;
export declare const backtopEmits: {
    click: (evt: MouseEvent) => boolean;
};
export declare type BacktopEmits = typeof backtopEmits;
