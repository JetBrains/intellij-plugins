package {
    public class UsageEx extends Usage {
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

        override public function us5():Vector.<From> {
            return us5();
        }

        override public function us6():Vector.<From> {
            return us6();
        }

        private function ttt() {
            us().moved();
            us2().moved();
            us3().notMoved();
            us4().moved();

            us5()[0].moved();
            us6()[0].moved();
        }
    }
}
