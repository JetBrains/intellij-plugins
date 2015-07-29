package {
    public class Usage extends UsageBase {
        override public function us():From {
            return us();
        }

        override public function us2():From {
            return us2();
        }

        override public function us3():From {
            return us3();
        }

        override public function us4():From {
            return us4();
        }

        override function us5():Vector.<From> {
            return us5();
        }

        override function us6():Vector.<From> {
            return us6();
        }

        private function ttt() {
            us().moved();
            us2().notMoved();
            us3().moved();
            us4().moved();

            us5()[0].notMoved();
            us6()[0].moved();

            def(abc(), abc2());

            var v : Vector.<From>;
            var v2 : Vector.<From> = v;
            v2[0].notMoved();
        }

        function abc():Vector.<From>() {}

        function abc2():Vector.<From>() {}

        function def(p: Vector.<From>, p2: Vector.<From>) {
            p[0].moved();
            p2[0].notMoved();
        }
    }
}
