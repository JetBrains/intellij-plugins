import type { E2EPage } from "../../../../utils/test/playwright/index";
export declare class ActionSheetFixture {
    readonly page: E2EPage;
    readonly screenshotFn?: (file: string) => string;
    private actionSheet;
    constructor(page: E2EPage, screenshot?: (file: string) => string);
    open(selector: string): Promise<void>;
    dismiss(): Promise<void>;
    screenshot(modifier: string): Promise<void>;
}
