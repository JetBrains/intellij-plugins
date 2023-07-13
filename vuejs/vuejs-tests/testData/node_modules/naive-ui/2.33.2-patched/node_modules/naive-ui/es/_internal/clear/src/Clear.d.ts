import { PropType } from 'vue';
declare const _default: import("vue").DefineComponent<{
    clsPrefix: {
        type: StringConstructor;
        required: true;
    };
    show: BooleanConstructor;
    onClear: PropType<(e: MouseEvent) => void>;
}, {
    handleMouseDown(e: MouseEvent): void;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    clsPrefix: {
        type: StringConstructor;
        required: true;
    };
    show: BooleanConstructor;
    onClear: PropType<(e: MouseEvent) => void>;
}>>, {
    show: boolean;
}>;
export default _default;
