package com.acessoatodos;

import org.jooby.Jooby;
import org.jooby.Results;

import com.acessoatodos.places.PlacesResource;

/**
 * Entry point of FE server.
 */
public class App extends Jooby {
  // Simple HTTP endpoints.
  // TODO(danielfireman): Endpoint addresses must be constants.
  {
	assets("/favicon.ico", "favicon.ico");

    get("/ping", () -> Results.ok());

    use(PlacesResource.class);

    // Temporarily redirecting to github project page while we don't have a landing page.
    get("/*", () -> Results.tempRedirect("https://github.com/danielfireman/acessoatodos"));
  }

  public static void main(final String[] args) throws Throwable {
    run(App::new, args);
  }
}
