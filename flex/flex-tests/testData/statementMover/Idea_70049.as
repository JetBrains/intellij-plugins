class Idea_70049 {
    public function onFriendAdd():void
    {
        if (foo()) {
            doSomething();
        }
    }
    private <caret>function onMessageClick():void
    {
    }

    // noinspection JSUnusedLocalSymbols
    public function doSomething():void
    {
    }
}