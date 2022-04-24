package com.yassineaddi.simpleioc.client;

import com.yassineaddi.simpleioc.ioc.annotations.Component;

@Component
public class ExternalWebClient implements WebClient {

  @Override
  public String get(String url) {
    return "externalUser1, externalUser2, externalUser3";
  }

}
