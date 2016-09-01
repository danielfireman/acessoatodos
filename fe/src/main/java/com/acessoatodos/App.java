package com.acessoatodos;

import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.aws.Aws;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.whoops.Whoops;

import com.acessoatodos.places.PlacesTableResource;
import com.acessoatodos.dynamodb.DynamoDbModule;
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
		// Jooby modules.
		use(new Jackson());

		// Acessoatodos modules.
		use(new DynamoDbModule());

		// Resources from acessoatodos.
		use(PlacesResource.class);
		use(PlacesTableResource.class);

		// TODO(danielfireman): Endpoint addresses must be constants.
		// Static routes.
		assets("/favicon.ico", "favicon.ico");
		get("/ping", () -> Results.ok());

		// Temporarily redirecting to github project page while we don't have a
		// landing page.
		get("/*", () -> Results.tempRedirect("https://github.com/danielfireman/acessoatodos"));

		// Stuff that is enabled only in dev.
		on("dev", () -> {
			// Pretty page showing errors in development mode.
			use(new Whoops());

			// Enable metrics collection.
			use(new Metrics()
					.request()
					.threadDump()
					.metric("memory", new MemoryUsageGaugeSet())
					.metric("threads", new ThreadStatesGaugeSet())
					.metric("gc", new GarbageCollectorMetricSet())
					.metric("fs", new FileDescriptorRatioGauge()));

			// TODO(danielfireman): Move the db endpoint to a flag.
			use(new Aws().with(creds -> new AmazonDynamoDBClient(creds).withEndpoint("http://localhost:8000")));
		});

	}

	public static void main(final String[] args) throws Throwable {
		run(App::new, args);
	}
}