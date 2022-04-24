package com.yassineaddi.simpleioc.ioc.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Objects;

import com.yassineaddi.simpleioc.ioc.annotations.Component;

public class BeanUtils {
  /**
   * Get the constructor with most parameters of the given class
   * 
   * @param clazz the class to get the constructor from
   * @return the constructor with most parameters
   */
  public static Constructor<?> getConstructorWithMostParameters(Class<?> clazz) {
    var constructors = clazz.getDeclaredConstructors();
    if (constructors.length == 0) {
      throw new IllegalArgumentException("No constructor found for class " + clazz.getName());
    }
    var constructorWithMostParameters = constructors[0];
    for (var constructor : constructors) {
      if (constructor.getParameterCount() > constructorWithMostParameters.getParameterCount()) {
        constructorWithMostParameters = constructor;
      }
    }
    if (!Modifier.isPublic(constructorWithMostParameters.getModifiers())) {
      constructorWithMostParameters.setAccessible(true);
    }
    return constructorWithMostParameters;
  }

  /**
   * Converts a CamelCase string to a lowerCamelCase string
   * 
   * @param s the string to convert
   * @return the converted string
   */
  private static String toLowerCamelCase(String s) {
    return s.substring(0, 1).toLowerCase() + s.substring(1);
  }

  /**
   * Returns the name from the {@link Component} annotation or the class name if
   * the annotation is not present
   * 
   * @param clazz the class to get the name from
   * @return the name from the {@link Component} annotation or the class name
   *         otherwise
   */
  public static String getBeanName(Class<?> clazz) {
    var annotation = clazz.getAnnotation(Component.class);
    Objects.requireNonNull(annotation);
    return annotation.value().isEmpty()
        ? toLowerCamelCase(clazz.getSimpleName())
        : annotation.value();
  }
}
