package com.acessoatodos;

import org.jooby.test.AppRule;
import org.jooby.test.Client;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * @author jooby generator
 */
public class BaseTest {

  /**
   * One app/server for all the test of this class. If you want to start/stop a new server per test,
   * remove the static modifier and replace the {@link ClassRule} annotation with {@link Rule}.
   */
  @ClassRule
  public static AppRule app = new AppRule(new App());

  /**
   * One client per test. It creates a new HTTP client per each of the test method you have.
   * Needs to be in sync with conf/application.dev.conf. 
   */
  @Rule
  public Client server = new Client("http://localhost:8081");
}
