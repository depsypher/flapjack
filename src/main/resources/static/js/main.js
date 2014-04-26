requirejs.config({
	appDir: ".",
	baseUrl: "js",
	paths: {
		"jquery" : [ "//cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min", "js/jquery-min" ],
		"bootstrap" : [ "//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min", "js/bootstrap-min" ],
		"persona" : [ "https://login.persona.org/include" ]
	},
	shim: {
		/* Set bootstrap dependencies (just jQuery) */
		"bootstrap" : [ "jquery" ],
		"metrics-watcher" : [ "bootstrap" ]
	}
});

require([
	"jquery", "bootstrap", "persona", "site"
],
function($) {
	var $ = require("jquery"),
	// the start module is defined on the same script tag as data-main.
	// example: <script data-main="js/main" data-start="app/page" src="vendor/require.js"/>
	startModuleName = $("script[data-main][data-start]").attr("data-start");

	if (startModuleName) {
		require([startModuleName], function (startModule) {
			$(function () {
				var fn = $.isFunction(startModule) ? startModule : startModule.init;
				if (fn) { fn(); }
			});
		});
	}
});
