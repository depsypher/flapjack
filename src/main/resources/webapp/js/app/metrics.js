define(["jquery", "lib/metrics-watcher.min"], function ($) {

	function downloadMetricData() {
		$.ajax({
			contentType : "application/json",
			url : "admin/metrics",
			success : function(data) {
				metricsWatcher.updateGraphs(data);
			},
			error : function(xhr, ajaxOptions, thrownError) {
				alert("Error - " + xhr.status + "Message: " + thrownError);
			},
			async : false
 		});
	}

	return new function() {
		metricsWatcher.addTimer("timerExample", "flapjack.controller.HomeController", "home", 125, "Timer", "requests", 50);
		metricsWatcher.addCache("queryCache", "net.sf.ehcache.Cache.flapjack.org.hibernate.cache.internal.StandardQueryCache", "Ehcache");
		metricsWatcher.addWeb("webExample", "com.yammer.metrics.web.WebappMetricsFilter", "Web Server");
		metricsWatcher.addJvm("jvmExample", "jvm", "JVM");
		metricsWatcher.initGraphs();

		downloadMetricData();
		setTimeout(downloadMetricData, 5000);
	};

});
