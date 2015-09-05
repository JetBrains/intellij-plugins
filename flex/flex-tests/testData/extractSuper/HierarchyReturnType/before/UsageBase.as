package {
    public class UsageBase {
        public function us():From {
            return us();
        }
        public function us2():From {
           return us2();
        }

        public function us3():From {
            return us3();
        }

        public function us4():From {
            return us4();
        }

        public function us5():Vector.<From> {
            return us5();
        }

        public function us6():Vector.<From> {
            return us6();
        }

        private function ttt() {
            us().notMoved();
            us2().moved();
            us3().moved();
            us4().moved();

            us5()[0].moved();
            us6()[0].moved();
        }
    }
}
