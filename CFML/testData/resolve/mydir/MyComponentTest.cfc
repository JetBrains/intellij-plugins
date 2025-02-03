component extends="coldbox.system.Coldbox"
{
	this.name = "jiffyshirts_#hash(expandPath('/'))#";
	this.clientManagement = false;

	// Session management enabled for Squabble (And disabled again)
	// this.sessionManagement = false;
	// this.sessionTimeout = createTimeSpan(0, 0, 5, 0);

	this.setDomainCookies = false;
	this.scriptProtect = "all";

	this.mappings["/app"] = expandPath("/../mapped/app");

	//set up environment cache, annoying you can't access the application scope from here
	//and that applicationStart doesn't fire first.
	if(!StructkeyExists(server, this.name))
	{
		lock name="#this.name#-startup" throwontimeout="true" timeout="60"
		{
			if(!StructkeyExists(server, this.name))
			{
				com = {};
				com.environment = new config.Environment();

				server[this.name] = com;
			}
		}
	}

	environment = server[this.name].environment;
	this.datasource = environment.getSettings().ds;

	//orm settings
	this.ormEnabled = true;
	this.ormSettings.automanageSession = false;
	this.ormSettings.flushatrequestend = false;

	//this means you can't use annotations. This is here for a reason.
	this.ormSettings.autogenmap = false;
	this.ormSettings.cfclocation = expandPath("/../mapped/app");
	this.ormSettings.dialect = "MySQLwithInnoDB";
	this.ormSettings.useDBForMapping = false;

	this.ormsettings.eventhandling = true;
	this.ormsettings.eventhandler = "coldspring.orm.hibernate.BeanInjectorEventHandler";

	this.ormsettings.secondarycacheenabled = true;

	if(environment.getBox() eq "dev")
	{
		//I would drop create, but the data need to be built, and it's too much for an import script.
		this.ormSettings.dbcreate = "update";

		//this.ormSettings.sqlscript = expandPath("/build/import.sql");

		include "build/disableDebug.cfm";
	}

	//COLDBOX STATIC PROPERTY, DO NOT CHANGE UNLESS THIS IS NOT THE ROOT OF YOUR COLDBOX APP
	//changed as there are subfolders currently, as we want this to always load with right path
	COLDBOX_APP_ROOT_PATH = expandPath("/");

	//The web server mapping to this application. Used for remote purposes or static purposes
	COLDBOX_APP_MAPPING = "/";
	//COLDBOX PROPERTIES
	COLDBOX_CONFIG_FILE = "/app/config/Coldbox.cfc";
	//COLDBOX APPLICATION KEY OVERRIDE
	COLDBOX_APP_KEY = "";

	/**
	 * Runs when ColdFusion receives the first request for a page in the application.
	 */
	public boolean function onApplicationStart() output="false"
	{
		application.com = initServices();

		//Load ColdBox
		loadColdBox();

		// Set up Squabble and any AB/Multivariate Tests
		application.squabble = new squabble.Squabble();

		application.squabble.registerTest("Smart Post", { display = ["on"] }, 100);
		application.squabble.disableTest("Smart Post");

		application.squabble.registerTest("Saturday Delivery Test", { saturdayDelivery = ["Disabled", "Enhanced Messaging"] }, 100);
		application.squabble.disableTest("Saturday Delivery Test");

		application.squabble.registerTest("Hoodie Landing Page", {landingPage = ["on"]}, 100);
		application.squabble.disableTest("Hoodie Landing Page");

		application.squabble.registerTest("Pin It Button", { pinIt = ["left", "center", "right"] }, 100);

		application.squabble.registerTest("Safety", { safety = ["replace", "far-left", "left-of-socks"] }, 100);

		//start job queue for logging
		application.coldspring.getBean("jobQueue").start();

		//if 'reIndex' in the url, it does it then.
		if (structKeyExists(url, "reIndex"))
		{
			silent = true;
			include "/build/solr/reload.cfm";

			//maybe push this onto it's own thread, and clear caches at the end?
			include "/build/solr/reIndex.cfm";
		}

		configureMongo();

		return true;
	}

	/**
	 * Runs when the ColdFusion application ends or is stopped.
	 */
	public boolean function onApplicationEnd(required struct applicationScope)
	{
		arguments.applicationScope.coldspring.getBean("jobQueue").stop();
		arguments.applicationScope.coldspring.getBean("loggingDelegate").closeNoSQLServer();
		arguments.applicationScope.coldspring.getBean("nosqlDelegate").closeNoSQLServer();
	}

	/**
	 * @hint "Runs when a request starts."
	 * @TargetPage "Path from the web root to the requested page."
	 */
	public boolean function onRequestStart(required string targetPage)
	{
		// emergency squabble cookie killer
		//clearSquabbleCookies();

		//aggressivly clear out any squabble cookies that don't exist anymore.
		application.squabble.removeUnlistedCookies();

		// uncomment this to override the system time globally
		//request.timezoneOverride = "{ts '2011-10-25 20:59:10'}";

		//put settings in request, and variables scopes, as some use 1, and some use the other
		request.box = application.com.environment.getBox();

		// Force variation for hudson
		if (request.box == "dev" && listFindNoCase("jenkins.sdtechdev.com,jiffyshirts.local", cgi.HTTP_HOST) && !structKeyExists(url, "comReset") && cgi.SCRIPT_NAME != "/build/sql.cfm")
		{
			try
			{
				//application.squabble.runTest("Safety", { safety = "control" });
			}
			catch (any exception) { /* Do nothing */ }
		}

		structAppend(request, application.com.environment.getSettings());

		request.cache = CreateTimeSpan(0, 0, 6, 0);
		request.majorProblem = "";
		request.newOrderCount = 0;
		request.noNewOrderChecks = 0;
		Application.noNewOrderChecks = 0;

		// Place temporary ColdSpring facade function into the request scope
		request.getCSBean = getCSBean;

		request.stylesheetLocation = "stylesheetv12.css";

		request.resourceMap = createResourceMap();

		// Clear cache if disabled
		if (structKeyExists(request, "enableCaching") && !request.enableCaching)
		{
			flushCache();
		}

		if(FileExists(expandPath("/_chrisStuff.cfm")))
		{
			include "/_chrisStuff.cfm";
		}

		// /new coldbox system
		if(findNoCase('coldbox.cfm', listLast(arguments.targetPage, '/')))
		{
			//Reload Checks
			reloadChecks();
			manageSession();

			//var start = getTickCount();

			processColdBoxRequest();

			//writeLog("Time [#targetPage#?#CGI.query_string#]: #(getTickCount() - start)#ms");
		}
		else
		{
			manageSession();
		}

		// legacy system
		if(structKeyExists(url, "comReset"))
		{
			lock name="Application-reset" throwontimeout="false" timeout="1"
			{
				//we place this include here, as it allows some of our build scripts to run, before we stop and relocate
				//we put this in a try/catch as there may well be an error due to new code not being fixed
				try
				{
					include arguments.targetPage;
				}
				catch(Any exc)
				{
					writeDump(var=exc, output="console");
				}

				include "/build/objectcache.cfm";

				//Clear the cache
				flushCache();

				applicationStop();

				structDelete(server, this.name);

				if(structKeyExists(url, "reIndex"))
				{
					location("/?reIndex=1", false);
				}
				else
				{
					location("/", false);
				}
			}
		}

		enableDefaultFilters();

		return true;
	}

	/**
	 * Temporary facade function to retrieve ColdSpring beans in the legacy system
	 *
	 * @beanName The name of the ColdSpring bean to return
	*/
	public any function getCSBean(required string beanName)
	{
		if (!structKeyExists(request, "CSBeans"))
		{
			request.CSBeans = {};
		}

		if (!structKeyExists(request.CSBeans, arguments.beanName))
		{
			request.CSBeans[arguments.beanName] = application.coldspring.getBean(arguments.beanName);
		}

		return request.CSBeans[arguments.beanName];
	}

	/**
	 * creates application scope cfcs. Eventually moved to ColdSpring
	 */
	private struct function initServices()
	{
		var com = {};

		com.environment = new config.Environment();

		com.timezone = new util.Timezone(listFindNoCase("dev,stage", com.environment.getBox()));
		com.viewMeasureMents = new util.ViewMeasurements(com.environment.getSettings().ds);

		return com;
	}

	/**
	 * Creates the resource map for all css/js resources for versioning
	 */
	private struct function createResourceMap()
	{
		//update this if you want to update an individual resource
		var resourceMap = {};

		// Javascript
		resourceMap["bootstrap.js"] = "3";
		resourceMap["checkout-dev.js"] = "4";
		resourceMap["checkout-prod.js"] = "4";
		resourceMap["checkout-stage.js"] = "4";
		resourceMap["facebook-like.js"] = "4";
		resourceMap["modernizr.js"] = "4";
		resourceMap["scripts-desktop-dev.js"] = "64";
		resourceMap["scripts-desktop-prod.js"] = "64";
		resourceMap["scripts-desktop-stage.js"] = "64";
		resourceMap["scripts-mobile-dev.js"] = "63";
		resourceMap["scripts-mobile-prod.js"] = "63";
		resourceMap["scripts-mobile-stage.js"] = "63";

		// AngularJS
		resourceMap["angular.js"] = "2";
		resourceMap["angular-app.js"] = "4";

		// CSS
		resourceMap["cartold.css"] = "2";
		resourceMap["checkout.css"] = "3";
		resourceMap["common.css"] = "32";
		resourceMap["handheld.css"] = "3";
		resourceMap["jquery-ui-dialog.css"] = "2";
		resourceMap["jquery-ui-red-button.css"] = "2";
		resourceMap["template.css"] = "89";

		// CSS (LESS)
		resourceMap["bootstrap.css"] = "5";
		resourceMap["cart.css"] = "35";
		resourceMap["category.css"] = "25";
		resourceMap["contact.css"] = "6";
		resourceMap["general.css"] = "3";
		resourceMap["help.css"] = "4";
		resourceMap["list.css"] = "9";
		resourceMap["product.css"] = "8";
		resourceMap["shipping.css"] = "6";
		resourceMap["template-bootstrap.css"] = "7";

		/* Brand & Category Image-Map Resources */

		// Brands
		resourceMap["brand-180s.css"] = "24";
		resourceMap["brand-180s.js"] = "24";
		resourceMap["brand-adams.css"] = "24";
		resourceMap["brand-adams.js"] = "24";
		resourceMap["brand-adidas.css"] = "24";
		resourceMap["brand-adidas.js"] = "24";
		resourceMap["brand-alo.css"] = "24";
		resourceMap["brand-alo.js"] = "24";
		resourceMap["brand-alternative.css"] = "24";
		resourceMap["brand-alternative.js"] = "24";
		resourceMap["brand-anvil.css"] = "25";
		resourceMap["brand-anvil.js"] = "25";
		resourceMap["brand-applesandoranges.css"] = "24";
		resourceMap["brand-applesandoranges.js"] = "24";
		resourceMap["brand-ashworth.css"] = "24";
		resourceMap["brand-ashworth.js"] = "24";
		resourceMap["brand-augusta.css"] = "24";
		resourceMap["brand-augusta.js"] = "24";
		resourceMap["brand-authenticpigment.css"] = "24";
		resourceMap["brand-authenticpigment.js"] = "24";
		resourceMap["brand-bagedge.css"] = "24";
		resourceMap["brand-bagedge.js"] = "24";
		resourceMap["brand-bella.css"] = "25";
		resourceMap["brand-bella.js"] = "25";
		resourceMap["brand-bigaccessories.css"] = "24";
		resourceMap["brand-bigaccessories.js"] = "24";
		resourceMap["brand-canvas.css"] = "24";
		resourceMap["brand-canvas.js"] = "24";
		resourceMap["brand-champion.css"] = "24";
		resourceMap["brand-champion.js"] = "24";
		resourceMap["brand-chestnuthill.css"] = "25";
		resourceMap["brand-chestnuthill.js"] = "25";
		resourceMap["brand-comfortcolors.css"] = "24";
		resourceMap["brand-comfortcolors.js"] = "24";
		resourceMap["brand-codev.css"] = "24";
		resourceMap["brand-codev.js"] = "24";
		resourceMap["brand-devonandjones.css"] = "24";
		resourceMap["brand-devonandjones.js"] = "24";
		resourceMap["brand-dickies.css"] = "25";
		resourceMap["brand-dickies.js"] = "25";
		resourceMap["brand-econscious.css"] = "24";
		resourceMap["brand-econscious.js"] = "24";
		resourceMap["brand-fruitoftheloom.css"] = "24";
		resourceMap["brand-fruitoftheloom.js"] = "24";
		resourceMap["brand-gildan.css"] = "25";
		resourceMap["brand-gildan.js"] = "25";
		resourceMap["brand-hanes.css"] = "24";
		resourceMap["brand-hanes.js"] = "24";
		resourceMap["brand-harriton.css"] = "24";
		resourceMap["brand-harriton.js"] = "24";
		resourceMap["brand-harvardsquare.css"] = "24";
		resourceMap["brand-harvardsquare.js"] = "24";
		resourceMap["brand-hookandtackle.css"] = "24";
		resourceMap["brand-hookandtackle.js"] = "24";
		resourceMap["brand-hyp.css"] = "24";
		resourceMap["brand-hyp.js"] = "24";
		resourceMap["brand-izod.css"] = "24";
		resourceMap["brand-izod.js"] = "24";
		resourceMap["brand-jerzees.css"] = "24";
		resourceMap["brand-jerzees.js"] = "24";
		resourceMap["brand-lat.css"] = "24";
		resourceMap["brand-lat.js"] = "24";
		resourceMap["brand-lat.js"] = "24";
		resourceMap["brand-lat.js"] = "24";
		resourceMap["brand-libertybags.css"] = "24";
		resourceMap["brand-libertybags.js"] = "24";
		resourceMap["brand-outerbanks.css"] = "24";
		resourceMap["brand-outerbanks.js"] = "24";
		resourceMap["brand-playback.css"] = "24";
		resourceMap["brand-playback.js"] = "24";
		resourceMap["brand-rabbitskins.css"] = "24";
		resourceMap["brand-rabbitskins.js"] = "24";
		resourceMap["brand-rossignol.css"] = "24";
		resourceMap["brand-rossignol.js"] = "24";
		resourceMap["brand-tiedye.css"] = "24";
		resourceMap["brand-tiedye.js"] = "24";
		resourceMap["brand-vanheusen.css"] = "24";
		resourceMap["brand-vanheusen.js"] = "24";
		resourceMap["brand-weatherproof.css"] = "24";
		resourceMap["brand-weatherproof.js"] = "24";
		resourceMap["brand-yupoong.css"] = "24";
		resourceMap["brand-yupoong.js"] = "24";

		resourceMap["brand-occunomix.css"] = "24";
		resourceMap["brand-occunomix.js"] = "24";
		resourceMap["brand-adidasgolf.css"] = "24";
		resourceMap["brand-adidasgolf.js"] = "24";
		resourceMap["brand-sublivie.css"] = "24";
		resourceMap["brand-sublivie.js"] = "24";
		resourceMap["brand-robinsonapparel.css"] = "24";
		resourceMap["brand-robinsonapparel.js"] = "24";

		// Categories
		resourceMap["category-all.css"] = "28";
		resourceMap["category-all.js"] = "28";
		resourceMap["category-tshirts.css"] = "26";
		resourceMap["category-tshirts.js"] = "26";
		resourceMap["category-poloshirts.css"] = "24";
		resourceMap["category-poloshirts.js"] = "24";
		resourceMap["category-dressshirts.css"] = "25";
		resourceMap["category-dressshirts.js"] = "25";
		resourceMap["category-women.css"] = "25";
		resourceMap["category-women.js"] = "25";
		resourceMap["category-kids.css"] = "24";
		resourceMap["category-kids.js"] = "24";
		resourceMap["category-sweatshirts.css"] = "25";
		resourceMap["category-sweatshirts.js"] = "25";
		resourceMap["category-pantsandshorts.css"] = "25";
		resourceMap["category-pantsandshorts.js"] = "25";
		resourceMap["category-jackets.css"] = "26";
		resourceMap["category-jackets.js"] = "26";
		resourceMap["category-hats.css"] = "24";
		resourceMap["category-hats.js"] = "24";
		resourceMap["category-bags.css"] = "24";
		resourceMap["category-bags.js"] = "24";
		resourceMap["category-kitchen-and-bath.css"] = "24";
		resourceMap["category-kitchen-and-bath.js"] = "24";
		resourceMap["category-socks.css"] = "24";
		resourceMap["category-socks.js"] = "24";
		resourceMap["category-gift-ideas.css"] = "24";
		resourceMap["category-gift-ideas.js"] = "24";

		return resourceMap;
	}

	/**
	 * Manages the current session
	 */
	private void function manageSession()
	{
		local.cookieData = structCopy(cookie);
		local.eventData = {};

		//do check for bad SID, and reset it.
		if(StructKeyExists(local.cookieData, "sid") AND local.cookieData.sid eq "02B98E8C-C0C5-CD45-AC2C74E3467C0339")
		{
			StructDelete(local.cookieData, "sid");
		}

		//remove non-permanent cookie values
		for (var name in local.cookieData)
		{
			if (!listFindNoCase("sid,referer.first,referer.qs,when.first", name))
			{
				structDelete(local.cookieData, name);
			}
		}

		structAppend(local.eventData, url);
		structAppend(local.eventData, form);

		local.sessionFilter = application.coldspring.getBean("sessionFilter");
		local.cookie = application.coldspring.getBean("cookie");

		local.sessionFilter.filter(local.cookieData, local.eventData);

		//add tracking data, do check for 'blank, as many old ones may well be'
		if(NOT structKeyExists(local.cookieData, "referer.first"))
		{
			local.cookieData["referer.first"] = CGI.HTTP_REFERER;
			local.cookieData["referer.qs"] = CGI.QUERY_STRING;
			local.cookieData["when.first"] = application.com.timezone.getTZCurrentTime();
		}

		local.cookie.apply(local.cookieData, "never");

		request.session = sessionFilter;

		// Set up GEO Location Data
		// getCSBean("geoLocation").manageLocation();
	}

	/**
	 * configure mongodb for replicated status
	 */
	public void function configureMongo()
	{
		var delegate = application.coldspring.getBean("nosqlDelegate");
		var mongo = delegate.getNoSQLServer();

		var readSecondary = delegate.getReadPreference().SECONDARY;

		mongo.getMongo().setReadPreference(readSecondary);

		if(environment.getBox() != "prod")
		{
			//for dev / stage, there is only 1 box
			var wc = delegate.getWriteConcern().SAFE;
		}
		else
		{
			//for prod, there should always be at least 2.
			var wc = delegate.getWriteConcern().REPLICAS_SAFE;
		}

		var carts = delegate.getCollection("carts");

		carts.setWriteConcern(wc);
		carts.setReadPreference(delegate.getReadPreference().PRIMARY);
	}

	/**
	 * enable default hiberante filters filters
	 */
	private void function enableDefaultFilters()
	{
		var service = getCSBean("InventoryService");

		try
		{
			service.enableFilterActive(active=javaCast("boolean", true));
		}
		catch(Any exc)
		{
			writeLog("Error setting active filter: #exc.message#");
		}
	}

	/**
	 * Flushes the object cache
	 */
	private void function flushCache()
	{
		var cache = cacheGetSession("object");
		if(!isNull(cache))
		{
			cache.removeAll();
		}
	}

	private void function clearSquabbleCookies()
	{
		var cookies = structKeyArray(cookie);

	    for (var key in cookies)
	    {
	    	if (LCase(key).startsWith("s-"))
	    	{
	    		structDelete(cookie, key);
	    	}
	    }
	}
}
