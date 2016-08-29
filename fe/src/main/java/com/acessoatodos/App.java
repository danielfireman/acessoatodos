package com.acessoatodos;

import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;

import com.acessoatodos.places.PlacesResource;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

/**
 * Entry point of FE server.
 */
public class App extends Jooby {
	{
		// Jooby modules installation.
		use(new Jackson());

		// Resources from acessoatodos.
		use(PlacesResource.class);

		// When in dev, enable metrics collection.
		on("dev", () -> {
			use(new Metrics()
					.request()
					.threadDump()
					.metric("memory", new MemoryUsageGaugeSet())
					.metric("threads", new ThreadStatesGaugeSet())
					.metric("gc", new GarbageCollectorMetricSet())
					.metric("fs", new FileDescriptorRatioGauge()));
		});

		// TODO(danielfireman): Endpoint addresses must be constants.
		// Static routes.
		assets("/favicon.ico", "favicon.ico");
		get("/ping", () -> Results.ok());

		// Temporarily redirecting to github project page while we don't have a
		// landing page.
		get("/*", () -> Results.tempRedirect("https://github.com/danielfireman/acessoatodos"));
	}

	public static void main(final String[] args) throws Throwable {
		run(App::new, args);
	}
}