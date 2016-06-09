package com.acessoatodos;

import org.junit.Test;

public class AppTest extends BaseTest {
  @Test
  public void ping() throws Exception {
    server.get("/ping").expect(200);
  }
  @Test
  public void tempRedirect() throws Exception {
    server.get("/foooo").expect(200);
  }
}
