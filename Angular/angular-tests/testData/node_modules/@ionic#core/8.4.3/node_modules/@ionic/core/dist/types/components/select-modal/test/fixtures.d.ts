import type { E2EPage, E2ELocator, EventSpy, E2EPageOptions, ScreenshotFn } from "../../../utils/test/playwright/index";
import type { SelectModalOption } from '../select-modal-interface';
export declare class SelectModalPage {
    private page;
    private multiple?;
    private options;
    modal: E2ELocator;
    selectModal: E2ELocator;
    ionModalDidDismiss: EventSpy;
    constructor(page: E2EPage);
    setup(config: E2EPageOptions, options: SelectModalOption[], multiple?: boolean): Promise<void>;
    screenshot(screenshot: ScreenshotFn, name: string): Promise<void>;
    clickOption(value: string): Promise<void>;
    pressSpaceOnOption(value: string): Promise<void>;
    private getOption;
}
