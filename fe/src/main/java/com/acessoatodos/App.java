package com.acessoatodos;

import org.jooby.Jooby;
import org.jooby.Results;

/**
 * Entry point of FE server.
 */
public class App extends Jooby {

  // Simple HTTP endpoints.
  // TODO(danielfireman): Endpoint addresses must be constants.
  {
    get("/ping", () -> Results.ok());
  }

  public static void main(final String[] args) throws Throwable {
    run(App::new, args);
  }
}
