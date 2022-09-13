import dayjs from 'dayjs';
import type { CalendarDateCell, CalendarDateCellType } from './date-table';
import type { Dayjs } from 'dayjs';
declare const _default: import("vue").DefineComponent<{
    readonly selectedDay: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<dayjs.Dayjs>, unknown, unknown, unknown, unknown>;
    readonly range: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<[dayjs.Dayjs, dayjs.Dayjs]>, unknown, unknown, unknown, unknown>;
    readonly date: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<dayjs.Dayjs>, unknown, true, unknown, unknown>;
    readonly hideHeader: import("../../../utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
}, {
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly selectedDay: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<dayjs.Dayjs>, unknown, unknown, unknown, unknown>;
        readonly range: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<[dayjs.Dayjs, dayjs.Dayjs]>, unknown, unknown, unknown, unknown>;
        readonly date: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<dayjs.Dayjs>, unknown, true, unknown, unknown>;
        readonly hideHeader: import("../../../utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
    }>> & {
        onPick?: ((value: dayjs.Dayjs) => any) | undefined;
    }>>;
    emit: (event: "pick", value: dayjs.Dayjs) => void;
    t: import("element-plus/es/hooks").Translator;
    lang: import("vue").Ref<string>;
    nsTable: {
        namespace: import("vue").ComputedRef<string>;
        b: (blockSuffix?: string) => string;
        e: (element?: string | undefined) => string;
        m: (modifier?: string | undefined) => string;
        be: (blockSuffix?: string | undefined, element?: string | undefined) => string;
        em: (element?: string | undefined, modifier?: string | undefined) => string;
        bm: (blockSuffix?: string | undefined, modifier?: string | undefined) => string;
        bem: (blockSuffix?: string | undefined, element?: string | undefined, modifier?: string | undefined) => string;
        is: {
            (name: string, state: boolean | undefined): string;
            (name: string): string;
        };
    };
    nsDay: {
        namespace: import("vue").ComputedRef<string>;
        b: (blockSuffix?: string) => string;
        e: (element?: string | undefined) => string;
        m: (modifier?: string | undefined) => string;
        be: (blockSuffix?: string | undefined, element?: string | undefined) => string;
        em: (element?: string | undefined, modifier?: string | undefined) => string;
        bm: (blockSuffix?: string | undefined, modifier?: string | undefined) => string;
        bem: (blockSuffix?: string | undefined, element?: string | undefined, modifier?: string | undefined) => string;
        is: {
            (name: string, state: boolean | undefined): string;
            (name: string): string;
        };
    };
    now: dayjs.Dayjs;
    firstDayOfWeek: number;
    isInRange: import("vue").ComputedRef<boolean>;
    rows: import("vue").ComputedRef<CalendarDateCell[][]>;
    weekDays: import("vue").ComputedRef<string[]>;
    getFormattedDate: (day: number, type: CalendarDateCellType) => Dayjs;
    getCellClass: ({ text, type }: CalendarDateCell) => string[];
    handlePickDay: ({ text, type }: CalendarDateCell) => void;
    getSlotData: ({ text, type }: CalendarDateCell) => {
        isSelected: boolean;
        type: string;
        day: string;
        date: Date;
    };
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    pick: (value: dayjs.Dayjs) => boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly selectedDay: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<dayjs.Dayjs>, unknown, unknown, unknown, unknown>;
    readonly range: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<[dayjs.Dayjs, dayjs.Dayjs]>, unknown, unknown, unknown, unknown>;
    readonly date: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<dayjs.Dayjs>, unknown, true, unknown, unknown>;
    readonly hideHeader: import("../../../utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
}>> & {
    onPick?: ((value: dayjs.Dayjs) => any) | undefined;
}, {
    range: [dayjs.Dayjs, dayjs.Dayjs];
    selectedDay: dayjs.Dayjs;
    hideHeader: import("../../../utils").BuildPropType<BooleanConstructor, unknown, unknown>;
}>;
export default _default;
