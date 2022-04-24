package com.yassineaddi.simpleioc.repositories;

import java.util.List;

import com.yassineaddi.simpleioc.ioc.annotations.Component;

@Component
public class UserRepository {
  private UserRepository() {
  }

  public List<String> findAll() {
    return List.of("user1", "user2", "user3");
  }
}
