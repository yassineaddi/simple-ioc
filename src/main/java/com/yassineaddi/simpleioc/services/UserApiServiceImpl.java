package com.yassineaddi.simpleioc.services;

import java.util.List;

import com.yassineaddi.simpleioc.client.WebClient;
import com.yassineaddi.simpleioc.ioc.annotations.Component;

@Component
public class UserApiServiceImpl implements UserService {

  private WebClient webClient;

  public UserApiServiceImpl(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public List<String> getUsers() {
    var res = webClient.get("/users");
    return List.of(res.split(",")).stream()
        .map(userName -> userName.trim())
        .toList();
  }

}
