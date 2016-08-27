package br.com.acessoatodos;

import br.com.acessoatodos.place.PlaceResource;
import org.jooby.Jooby;
import org.jooby.json.Jackson;

/**
 * @author jooby generator
 */
public class App extends Jooby {

  {
    use(new Jackson());
    use(PlaceResource.class);

    get("/", () -> "Hello World!");
  }

  public static void main(final String[] args) throws Throwable {
    run(App::new, args);
  }

}
