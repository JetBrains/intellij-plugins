import type { E2EPage, E2EPageOptions } from "../../../utils/test/playwright/index";
export declare class CardModalPage {
    private ionModalDidPresent;
    private ionModalDidDismiss;
    private page;
    constructor(page: E2EPage);
    navigate(url: string, config: E2EPageOptions): Promise<void>;
    openModalByTrigger(selector: string): Promise<import("@utils/test/playwright").E2ELocator>;
    swipeToCloseModal(selector: string, waitForDismiss?: boolean, swipeY?: number): Promise<void>;
}
