import { VNodeChild } from 'vue';
import type { SelectOption } from '../../../select/src/interface';
export declare type RenderTag = (props: {
    option: SelectOption;
    handleClose: () => void;
}) => VNodeChild;
