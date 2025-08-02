import type { StatusBarPlugin, Style as StatusBarStyle } from '@capacitor/status-bar';
interface StyleOptions {
    style: StatusBarStyle;
}
export declare enum Style {
    Dark = "DARK",
    Light = "LIGHT",
    Default = "DEFAULT"
}
export declare const StatusBar: {
    getEngine(): StatusBarPlugin | undefined;
    setStyle(options: StyleOptions): void;
    getStyle: () => Promise<StatusBarStyle>;
};
export {};
