<?php

namespace App\First {

    use Vendor\Alpha\Service;

    final class FirstContainer
    {
        public function boot(): void
        {
            new Service();
        }
    }
}

namespace App\Second {

    use Vendor\Beta\Factory;

    final class SecondContainer
    {
        public function boot(): void
        {
            new Factory();
        }
    }
}

namespace App\Third {

    use Vendor\Gamma\Mapper;

    final class ThirdContainer
    {
        public function boot(): void
        {
            new Mapper();
        }
    }
}
