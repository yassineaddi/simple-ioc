package com.yassineaddi.simpleioc.services;

import java.util.List;

import com.yassineaddi.simpleioc.ioc.annotations.Component;
import com.yassineaddi.simpleioc.repositories.UserRepository;

@Component
public class UserServiceImpl implements UserService {
  private UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public List<String> getUsers() {
    return userRepository.findAll();
  }

}
