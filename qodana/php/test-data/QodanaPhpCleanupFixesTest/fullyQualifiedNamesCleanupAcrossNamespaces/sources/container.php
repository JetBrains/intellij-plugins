<?php

namespace App\First {
    final class FirstContainer
    {
        public function boot(): void
        {
            new \Vendor\Alpha\Service();
        }
    }
}

namespace App\Second {
    final class SecondContainer
    {
        public function boot(): void
        {
            new \Vendor\Beta\Factory();
        }
    }
}

namespace App\Third {
    final class ThirdContainer
    {
        public function boot(): void
        {
            new \Vendor\Gamma\Mapper();
        }
    }
}
