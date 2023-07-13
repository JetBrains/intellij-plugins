import type { ExtractPropTypes } from 'vue';
import type { Dayjs } from 'dayjs';
import type DateTable from './date-table.vue';
export declare type CalendarDateCellType = 'next' | 'prev' | 'current';
export declare type CalendarDateCell = {
    text: number;
    type: CalendarDateCellType;
};
export declare const getPrevMonthLastDays: (date: Dayjs, count: number) => number[];
export declare const getMonthDays: (date: Dayjs) => number[];
export declare const toNestedArr: (days: CalendarDateCell[]) => CalendarDateCell[][];
export declare const dateTableProps: {
    readonly selectedDay: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<Dayjs>, unknown, unknown, unknown, unknown>;
    readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[Dayjs, Dayjs]>, unknown, unknown, unknown, unknown>;
    readonly date: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<Dayjs>, unknown, true, unknown, unknown>;
    readonly hideHeader: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
};
export declare type DateTableProps = ExtractPropTypes<typeof dateTableProps>;
export declare const dateTableEmits: {
    pick: (value: Dayjs) => boolean;
};
export declare type DateTableEmits = typeof dateTableEmits;
export declare type DateTableInstance = InstanceType<typeof DateTable>;
