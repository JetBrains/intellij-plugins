<?php

namespace App;

use Vendor\Alpha\Service;
use Vendor\Beta\Factory;
use Vendor\Delta\Client;
use Vendor\Epsilon\Command;
use Vendor\Gamma\Mapper;

final class Container
{
    public function boot(): void
    {
        new Service();
        new Factory();
        new Client();
        new Command();
        new Mapper();
    }
}
