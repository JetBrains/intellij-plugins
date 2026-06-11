<?php

namespace App;

final class Container
{
    public function boot(): void
    {
        new \Vendor\Alpha\Service();
        new \Vendor\Beta\Factory();
        new \Vendor\Delta\Client();
        new \Vendor\Epsilon\Command();
        new \Vendor\Gamma\Mapper();
    }
}
