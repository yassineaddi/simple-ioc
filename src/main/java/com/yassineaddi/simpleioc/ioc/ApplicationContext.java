package com.yassineaddi.simpleioc.ioc;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import com.yassineaddi.simpleioc.ioc.annotations.Component;
import com.yassineaddi.simpleioc.ioc.annotations.ComponentScan;
import com.yassineaddi.simpleioc.ioc.utils.BeanUtils;

public class ApplicationContext {
  private final Class<?> configClass;
  private final List<Class<?>> classesToScan = new ArrayList<>();
  private final Map<Class<?>, Object> beans = new HashMap<>();
  private final Map<String, Class<?>> beanNames = new HashMap<>();

  private final Logger log = Logger.getLogger(ApplicationContext.class.getName());

  public ApplicationContext(Class<?> configClass) {
    this.configClass = configClass;
    this.bootstrap();
  }

  /**
   * Bootstrap the application context.
   */
  private void bootstrap() {
    // Check if the config class is annotated with @ComponentScan, and freak out if
    // it isn't
    Annotation annotation = configClass.getAnnotation(ComponentScan.class);
    Objects.requireNonNull(annotation,
        String.format("No @ComponentScan annotation found on class %s", configClass.getName()));

    // Get the value of the @ComponentScan annotation
    ComponentScan componentScan = (ComponentScan) annotation;
    var basePackages = componentScan.basePackages();
    // Scan base packages and their sub-packages for components
    for (var basePackage : basePackages) {
      this.scanAll(basePackage);
    }

    // Now that we have all the classes to scan, we can create and register the
    // beans
    for (var clazz : classesToScan) {
      this.registerBean(clazz);
    }
  }

  /**
   * Scans all classes annotated with {@link Component} in the application
   * context classpath
   */
  private void scanAll(String packageName) {
    var classLoader = getClass().getClassLoader();
    var resources = classLoader.getResource(packageName.replace('.', '/'));
    var file = new File(resources.getPath());

    var files = file.listFiles();
    for (var f : files) {
      if (f.isFile()) {
        // Here we check if the file is a class file
        if (!f.getName().endsWith(".class")) {
          // Skip non class files
          continue;
        }

        var className = f.getName().split("\\.")[0];
        try {
          // Only add classes that are annotated with @Component
          var clazz = Class.forName(packageName + "." + className);
          if (clazz.isAnnotationPresent(Component.class)) {
            classesToScan.add(clazz);
          }
        } catch (ClassNotFoundException e) {
          log.warning(String.format("Class %s not found", packageName + "." + className));
        }
      } else if (f.isDirectory()) {
        // Recursively scan sub-packages
        this.scanAll(packageName + "." + f.getName());
      }
    }
  }

  /**
   * Registers a bean in the application context
   *
   * @param clazz The class to register
   */
  private void registerBean(Class<?> clazz) {
    // Don't register the same class twice
    if (beans.containsKey(clazz)) {
      return;
    }

    // If the bean is an interface, we can't instantiate it, so skip it
    if (clazz.isInterface()) {
      return;
    }

    try {
      // Get the constructor with the most parameters (if any)
      var constructor = BeanUtils.getConstructorWithMostParameters(clazz);
      // Get the parameters of the constructor
      var parameters = constructor.getParameters();
      // Create an array of objects to pass to the constructor
      var args = new ArrayList<>(parameters.length);
      // For each parameter, get the bean of the same type and inject it
      for (var parameter : parameters) {
        // If the parameter is an interface, we look for an implementation in our beans
        // map if found (if not, we skip it)
        if (parameter.getType().isInterface()) {
          // Since this is a simple IoC demo, we take the first implementation of the
          // interface
          var bean = this.getImplementationOf(parameter.getType());
          args.add(bean);
        } else {
          var bean = this.getBean(parameter.getType());
          args.add(bean);
        }
      }
      // Create the bean and register it
      var bean = constructor.newInstance(args.toArray());
      // Get the bean name if specified or use the class name
      var beanName = BeanUtils.getBeanName(clazz);
      this.beanNames.put(beanName, clazz);
      this.beans.put(clazz, bean);

      log.info(String.format("Created instance of %s with args %s", clazz.getName(), args));

    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | SecurityException e) {
      log.severe(String.format("Cannot create bean for class %s", clazz.getName()));
      e.printStackTrace();
    }
  }

  /**
   * Gets the implementation of the given interface
   * 
   * @param type The interface
   * @return The implementation
   */
  private Object getImplementationOf(Class<?> type) {
    // Maybe the class hasn't been registered yet
    this.classesToScan.stream()
        .filter(c -> type.isAssignableFrom(c))
        .findFirst()
        .ifPresent(c -> this.registerBean(c));

    var bean = this.beans.entrySet().stream()
        .filter(c -> type.isAssignableFrom(c.getKey()))
        .findFirst()
        .orElse(null);

    return Objects.isNull(bean) ? null : bean.getValue();
  }

  /**
   * Gets the bean of the specified class
   * 
   * @param type The class type
   * @return The bean of the given class type
   */
  public <T> T getBean(Class<T> _class) {
    if (_class.isInterface()) {
      return (T) this.getImplementationOf(_class);
    }
    if (!this.beans.containsKey(_class)) {
      this.registerBean(_class);
    }
    return _class.cast(this.beans.get(_class));
  }

  /**
   * Gets the bean of the specified name
   * 
   * @param name The bean name
   * @return The bean of the given name
   */
  public <T> T getBean(String className) {
    return (T) this.getBean(this.beanNames.get(className));
  }

}
