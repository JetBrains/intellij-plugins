export declare type OnUpdateValue = (value: number, time: DateItem) => void;
export interface DateItem {
    year: number;
    month: number;
    date: number;
}
export declare type OnPanelChange = (info: {
    year: number;
    month: number;
}) => void;
