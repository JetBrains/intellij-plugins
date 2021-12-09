import { ComputedRef, PropType } from 'vue';
interface UseAdjustedToProps {
    to?: string | HTMLElement | boolean;
    [key: string]: unknown;
}
declare function useAdjustedTo(props: UseAdjustedToProps): ComputedRef<HTMLElement | string>;
declare namespace useAdjustedTo {
    var tdkey: string;
    var propTo: {
        type: PropType<string | boolean | HTMLElement>;
        default: undefined;
    };
}
export { useAdjustedTo };
