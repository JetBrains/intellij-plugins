class Idea_70049 {
    public function onFriendAdd():void
    {
        if (foo()) {
            doSomething();
        }
    }
    // noinspection JSUnusedLocalSymbols
    public function doSomething():void
    {
    }

    private <caret>function onMessageClick():void
    {
    }
}