class Idea_70049 {
    private <caret>function onMessageClick():void
    {
    }
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
}