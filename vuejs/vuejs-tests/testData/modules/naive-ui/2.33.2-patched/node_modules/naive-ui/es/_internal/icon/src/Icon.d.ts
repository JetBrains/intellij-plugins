import { PropType } from 'vue';
declare const _default: import("vue").DefineComponent<{
    role: StringConstructor;
    ariaLabel: StringConstructor;
    ariaDisabled: {
        type: BooleanConstructor;
        default: undefined;
    };
    ariaHidden: {
        type: BooleanConstructor;
        default: undefined;
    };
    clsPrefix: {
        type: StringConstructor;
        required: true;
    };
    onClick: PropType<(e: MouseEvent) => void>;
    onMousedown: PropType<(e: MouseEvent) => void>;
    onMouseup: PropType<(e: MouseEvent) => void>;
}, void, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    role: StringConstructor;
    ariaLabel: StringConstructor;
    ariaDisabled: {
        type: BooleanConstructor;
        default: undefined;
    };
    ariaHidden: {
        type: BooleanConstructor;
        default: undefined;
    };
    clsPrefix: {
        type: StringConstructor;
        required: true;
    };
    onClick: PropType<(e: MouseEvent) => void>;
    onMousedown: PropType<(e: MouseEvent) => void>;
    onMouseup: PropType<(e: MouseEvent) => void>;
}>>, {
    ariaDisabled: boolean;
    ariaHidden: boolean;
}>;
export default _default;
