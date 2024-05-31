package com.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class DataRizer {

  private static final Random RANDOM = new Random();

  private static final int STRING_LENGTH = 10;
  private static final int SECONDS_IN_DAY = 86_400;
  private static final int SECONDS_IN_HOUR = 3600;
  private static final int DAYS_IN_YEAR_ROUGH = 365;
  private static final int MAX_RANDOM_LIST_LENGTH = 10;

  private static final Map<Class<?>, Supplier<?>> BASE_OBJECTS = new HashMap<>();

  static {
    BASE_OBJECTS.put(String.class, () -> getRandomString(STRING_LENGTH));
    BASE_OBJECTS.put(Integer.class, () -> RANDOM.nextInt(Integer.MAX_VALUE));
    BASE_OBJECTS.put(Long.class, RANDOM::nextLong);
    BASE_OBJECTS.put(Boolean.class, RANDOM::nextBoolean);
    BASE_OBJECTS.put(Float.class, RANDOM::nextFloat);
    BASE_OBJECTS.put(Double.class, RANDOM::nextDouble);
    // Scaled down to smaller precision
    BASE_OBJECTS.put(
        BigDecimal.class,
        () -> BigDecimal.valueOf(RANDOM.nextDouble()).setScale(20, RoundingMode.DOWN));
    BASE_OBJECTS.put(
        Instant.class,
        () ->
            Instant.now()
                .plusSeconds(RANDOM.nextInt(SECONDS_IN_DAY))
                .truncatedTo(ChronoUnit.MILLIS));
    BASE_OBJECTS.put(
        LocalDate.class, () -> LocalDate.now().plusDays(RANDOM.nextInt(DAYS_IN_YEAR_ROUGH)));
    BASE_OBJECTS.put(
        LocalDateTime.class,
        () ->
            LocalDateTime.now()
                .plusSeconds(RANDOM.nextInt(SECONDS_IN_DAY))
                .truncatedTo(ChronoUnit.MILLIS));
    BASE_OBJECTS.put(
        LocalTime.class,
        () ->
            LocalTime.now()
                .plusSeconds(RANDOM.nextInt(SECONDS_IN_HOUR))
                .truncatedTo(ChronoUnit.MINUTES));
    BASE_OBJECTS.put(Duration.class, () -> Duration.ofMillis(RANDOM.nextInt(10_000)));
    BASE_OBJECTS.put(Object.class, Object::new);
  }

  /**
   * Instantiate a random object from a set of out of the box non-collection data objects.
   *
   * @param clazz Class object wrapping the generic
   * @param <T> Class generic to instantiate
   * @return (T) An instantiated object of Type T or null if nothing found in set
   */
  static <T> T instantiateFromBaseObjects(Class<T> clazz) {
    return (T) BASE_OBJECTS.getOrDefault(clazz, () -> null).get();
  }

  /**
   * Generate an alphanumeric random string of provided length
   *
   * @param length number of characters of random String
   * @return random string
   */
  static String getRandomString(int length) {
    final int leftLimit = 48; // numeral '0'
    final int rightLimit = 122; // letter 'z'

    return RANDOM
        .ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  /**
   * Instantiate a random enum from an enum object. Throws ClassPopulatorException if enum doesn't
   * have any to choose from.
   *
   * @param clazz class to get enum from
   * @param <T> desired object
   * @return instantiated enum or null if not possible
   */
  static <T> T instantiateFromEnum(Class<T> clazz) {
    if (clazz.isEnum() && clazz.getEnumConstants().length > 0) {
      T[] vals = clazz.getEnumConstants();
      return vals[RANDOM.nextInt(vals.length)];
    } else if (clazz.isEnum() && clazz.getEnumConstants().length == 0) {
      throw new DataRizerException("Can't instantiate an enum with no values");
    }
    return null;
  }

  /**
   * Given a list of static constructor methods used to instantiate a private constructor try and
   * find an implementation that exists and return that Method. Example below with build method
   * "of":
   *
   * <pre>
   * class Cat {
   *
   *    private Cat(){}
   *
   *    public static Cat of(){ return new Cat(); }
   *
   * }
   * </pre>
   *
   * @param clazz Class object wrapping the generic
   * @param builders List of static method names to search for
   * @param <T> Class generic to instantiate
   * @return Method that can be invoked or null if none found
   */
  static <T> Method getStaticBuilderMethods(Class<T> clazz, List<String> builders) {
    return Arrays.stream(clazz.getMethods())
        .filter(
            method ->
                (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC
                    && method.getReturnType() == clazz
                    && builders.contains(method.getName()))
        .findAny()
        .orElse(null);
  }

  /**
   * Given a Method return an Object Array of generated random data based on the Method arguments
   *
   * @param method method name to get parameters from
   * @param recursion how many times should the method be called if parameter object is same as
   *     class containing method
   * @return List of generated objects mapping to method parameter types. Return nulls when
   *     recursion value is reached
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   */
  static Object[] generateMethodParametersData(Method method, int recursion)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {

    Parameter[] parameters = method.getParameters();
    List<Object> instances = new ArrayList<>(parameters.length);

    int counter = 0;
    for (Parameter parameter : parameters) {
      Class<?> parameterType = parameter.getType();
      if (parameterType == method.getDeclaringClass()) {
        instances.add(
            recursion > 0 ? instantiate(method.getDeclaringClass(), recursion - 1) : null);
      } else if (Collection.class.isAssignableFrom(parameterType)) {
        ParameterizedType genericParameterType =
            (ParameterizedType) method.getGenericParameterTypes()[counter];
        instances.add(generateParameterizedTypeData(parameter.getType(), 2, genericParameterType));
      } else {
        instances.add(instantiate(parameterType));
      }
      counter++;
    }
    return instances.toArray();
  }

  /**
   * Instantiate data for a ParameterizedType (ie, String in List<String>)
   *
   * @param collectionClazz Class object wrapping the generic
   * @param maxRecursion The number of times to instantiate the same class before returning null
   * @param parameterizedType data
   * @param <T> data
   * @return instantiated object or null of maxRecursion value reached
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   */
  private static <T> Object generateParameterizedTypeData(
          Class<T> collectionClazz, int maxRecursion, ParameterizedType parameterizedType)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {

    if (List.class == parameterizedType.getRawType()) {
      Type parameterizedTypeActualTypeArgument = parameterizedType.getActualTypeArguments()[0];
      var list = new ArrayList<>();
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        Class<?> rawType;
        Class<?> actualTypeArgument;
        try {
          rawType =
              (Class<?>)
                  ((java.lang.reflect.ParameterizedType) parameterizedTypeActualTypeArgument)
                      .getRawType();

          if (List.class.isAssignableFrom(rawType) || Set.class.isAssignableFrom(rawType)) {
            actualTypeArgument =
                (Class<?>)
                    ((java.lang.reflect.ParameterizedType) parameterizedTypeActualTypeArgument)
                        .getActualTypeArguments()[0];

            list.add(instantiate(rawType, actualTypeArgument));
          } else {
            var map = (Map) instantiateFromConstructor(rawType, maxRecursion - 1);
            if (Objects.isNull(map)) {
              map = new java.util.HashMap();
            }
            for (int j = 0; j < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; j++) {
              var keyObject =
                  instantiate(
                      (Class<?>)
                          ((java.lang.reflect.ParameterizedType)
                                  parameterizedTypeActualTypeArgument)
                              .getActualTypeArguments()[0]);
              var valueObject =
                  instantiate(
                      (Class<?>)
                          ((java.lang.reflect.ParameterizedType)
                                  parameterizedTypeActualTypeArgument)
                              .getActualTypeArguments()[1]);
              map.put(keyObject, valueObject);
              list.add(map);
            }
          }
        } catch (ClassCastException cce) {
          rawType = (Class<?>) parameterizedTypeActualTypeArgument;
          list.add(instantiate(rawType));
        }
      }
      return list;
    } else if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
      System.out.println((Class<?>) parameterizedType.getRawType());
      var list =
          (List)
              instantiateFromConstructor(
                  (Class<?>) parameterizedType.getRawType(), maxRecursion - 1);

      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        list.add(instantiate((Class<?>) parameterizedType.getActualTypeArguments()[0]));
      }
      return list;

    } else if (Set.class == parameterizedType.getRawType()) {
      var setType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
      var set = new HashSet<>();
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        set.add(instantiate(setType));
      }
      return set;
    } else if (Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
      var set =
          (Set)
              instantiateFromConstructor(
                  (Class<?>) parameterizedType.getRawType(), maxRecursion - 1);

      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        set.add(instantiate((Class<?>) parameterizedType.getActualTypeArguments()[0]));
      }
      return set;
    } else if (Map.class == parameterizedType.getRawType()) {
      var map = new HashMap<>();
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        var keyObject = instantiate((Class<?>) parameterizedType.getActualTypeArguments()[0]);
        var valueObject = instantiate((Class<?>) parameterizedType.getActualTypeArguments()[1]);
        map.put(keyObject, valueObject);
      }
      return map;
    } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
      var map =
          (Map)
              instantiateFromConstructor(
                  (Class<?>) parameterizedType.getRawType(), maxRecursion - 1);
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        var keyObject = instantiate((Class<?>) parameterizedType.getActualTypeArguments()[0]);
        var valueObject = instantiate((Class<?>) parameterizedType.getActualTypeArguments()[1]);
        map.put(keyObject, valueObject);
      }
      return map;
    } else {
      return null;
    }
  }

  // Given a list of setter methods and a clazz generate data for those setter methods
  private static <T> void setMethodParameters(
          List<Method> methodList, Class<T> clazz, T finalObject, int sameClassRecursion)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {
    AtomicInteger v = new AtomicInteger();
    v.set(sameClassRecursion);

    for (Method method : methodList) {
      Type[] genericParameterTypes = method.getGenericParameterTypes();
      List list = new ArrayList();
      for (Type type : genericParameterTypes) {
        if (type == clazz) {
          v.decrementAndGet();
        }
        list.add(v.get() != 0 ? DataRizer.instantiate((Class<?>) type, v.get()) : null);
      }
      method.invoke(finalObject, list.toArray());
    }
  }

  /**
   * Given a class get the methods starting with "set"
   *
   * @param object supplied class
   * @param <T> Type of class
   * @return List of methods
   */
  private static <T> List<Method> getSetterMethods(T object) {
    List<Method> methodList =
        Arrays.asList(object.getClass().getMethods()).stream()
            .filter(
                method ->
                    method.getName().startsWith("set")
                        && (method.getModifiers() & Modifier.PRIVATE) == 0)
            .collect(Collectors.toList());
    return methodList;
  }

  /**
   * @param clazz Class object wrapping the generic
   * @param <T>
   * @return
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   */
  static <T> T instantiate(Class<T> clazz)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {
    return instantiate(clazz, 2);
  }

  /**
   * @param clazz Class object wrapping the generic
   * @param sameClassRecursion
   * @param <T>
   * @return
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   */
  private static <T> T instantiate(Class<T> clazz, int sameClassRecursion)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {
    T object = instantiateFromBaseObjects(clazz);

    if (Objects.isNull(object)) {
      object = instantiateFromEnum(clazz);

      // instantiate from constructor
      if (Objects.isNull(object)) {
        object = instantiateFromConstructor(clazz, sameClassRecursion);
      }

      // builder pattern
      if (Objects.isNull(object)) {
        object = instantiateFromBuilderPattern(clazz, sameClassRecursion, "of");
      }

      //     get setter methods and invoke
      if (!(object instanceof Collection)) {
        List<Method> methodList = getSetterMethods(object);
        setMethodParameters(methodList, clazz, object, sameClassRecursion);
      }
    }

    return object;
  }

  /**
   * Look for a constructor of the given class to instantiate. Ordering 1. Look for a non-private
   * empty argument constructor 2. Look for any non-private constructor
   *
   * @param clazz Class object wrapping the generic
   * @param sameClassRecursion if the class is self-referential how many recursive calls should be
   *     made before returning null
   * @param <T> The generic type of clazz to be instantiated. <T> is the same type as clazz
   * @return instantiated clazz
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   */
  private static <T> T instantiateFromConstructor(Class<T> clazz, int sameClassRecursion)
      throws InvocationTargetException, InstantiationException, IllegalAccessException {
    // look for public empty constructors
    Optional<Constructor<?>> any =
        Arrays.stream(clazz.getDeclaredConstructors())
            .filter(
                constructor ->
                    constructor.getParameterCount() == 0
                        && (constructor.getModifiers() & Modifier.PRIVATE) != Modifier.PRIVATE)
            .findAny();
    if (any.isPresent()) {
      return (T) any.get().newInstance();
    }

    // if empty constructors are not found then look for other options
    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
      if ((constructor.getModifiers() & Modifier.PUBLIC) != 0) {
        Type[] types = constructor.getGenericParameterTypes();
        Object[] constructorParameters = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructorParameters.length; i++) {
          Class<?> basicType = constructor.getParameters()[i].getType();
          Type possibleParameterizedType = types[i];
          if (possibleParameterizedType instanceof ParameterizedType) {
            constructorParameters[i] =
                generateParameterizedTypeData(
                    clazz, sameClassRecursion, (ParameterizedType) possibleParameterizedType);
          } else {
            constructorParameters[i] = instantiate(basicType);
          }
        }
        return (T) constructor.newInstance(constructorParameters);
      }
    }
    return null;
  }

  /**
   * Instantiate following a builder pattern.
   *
   * @param clazz Class object wrapping the generic
   * @param sameClassRecursion number of times to run recursion. Throws exception if runs past
   *     recursion number
   * @param args Builder methods names to use
   * @param <T> Generic class
   * @return Instantiated class
   */
  private static <T> T instantiateFromBuilderPattern(
          Class<T> clazz, int sameClassRecursion, String... args) {
    Method method = getStaticBuilderMethods(clazz, List.of(args));
    if (!Objects.isNull(method)) {
      try {
        return (T) method.invoke(null, generateMethodParametersData(method, sameClassRecursion));
      } catch (InvocationTargetException | IllegalAccessException | InstantiationException ite) {
        throw new DataRizerException(
            "Recursion value: "
                + sameClassRecursion
                + ", if zero may have been cause by NPE, "
                + ite);
      }
    }
    return null;
  }

  /**
   * Designed to get a Map with supplied key and value types
   *
   * @param collectionClazz Class object wrapping the generic
   * @param clazzKey Class object wrapping the generic
   * @param clazzValue Class object wrapping the generic
   * @param <Collection> class
   * @param <K> class
   * @param <E> class
   * @return class
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   */
  static <Collection, K, E> Map instantiate(
          Class<Collection> collectionClazz, Class<K> clazzKey, Class<E> clazzValue)
      throws InvocationTargetException, InstantiationException, IllegalAccessException {
    if (Map.class == collectionClazz) {
      var map = new HashMap<>();
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        map.put(instantiate(clazzKey), instantiate(clazzValue));
      }
      return map;
    } else if (Map.class.isAssignableFrom(collectionClazz)) {
      Map map = (Map) instantiateFromConstructor(collectionClazz, 2);
      Optional<Method> put =
          Arrays.stream(map.getClass().getMethods())
              .filter(method -> method.getName().equals("put"))
              .filter(method -> (method.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC)
              .findAny();
      if (put.isPresent()) {
        for (int i = 0; i < 10; i++) {
          put.get()
              .invoke(
                  map,
                  DataRizer.instantiate(clazzKey),
                  DataRizer.instantiate(clazzValue));
        }
      }
      return map;
    }
    return null;
  }

  /**
   * Instantiates List or Set Interfaces and implementations with given data type
   *
   * @param collectionClazz Class object wrapping the generic
   * @param valueClazz Class object wrapping the generic
   * @param <Collection> data
   * @param <V> data
   * @return data
   * @throws InvocationTargetException exception thrown during reflection calls
   * @throws InstantiationException exception thrown during reflection calls
   * @throws IllegalAccessException exception thrown during reflection calls
   */
  static <Collection, V> Collection instantiate(
          Class<Collection> collectionClazz, Class<V> valueClazz)
      throws InvocationTargetException, InstantiationException, IllegalAccessException {
    if (List.class == collectionClazz) {
      var list = new ArrayList<>();
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        var setData = instantiate(valueClazz);
        list.add(setData);
      }
      return (Collection) list;
    } else if (List.class.isAssignableFrom(collectionClazz)) {
      List list = (List) instantiateFromConstructor(collectionClazz, 2);
      Optional<Method> add =
          Arrays.stream(list.getClass().getMethods())
              .filter(method -> method.getName().equals("add"))
              .filter(
                  method ->
                      (method.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC
                          && method.getParameters().length == 1)
              .findAny();
      if (add.isPresent()) {
        for (int i = 0; i < 10; i++) {
          add.get().invoke(list, DataRizer.instantiate(valueClazz));
        }
      }
      return (Collection) list;

    } else if (Set.class == collectionClazz) {
      var set = new HashSet<>();
      for (int i = 0; i < RANDOM.nextInt(MAX_RANDOM_LIST_LENGTH) + 1; i++) {
        set.add(instantiate(valueClazz));
      }
      return (Collection) set;
    } else if (Set.class.isAssignableFrom(collectionClazz)) {
      Set list = (Set) instantiateFromConstructor(collectionClazz, 2);
      Optional<Method> add =
          Arrays.stream(list.getClass().getMethods())
              .filter(method -> method.getName().equals("add"))
              .filter(
                  method ->
                      (method.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC
                          && method.getParameters().length == 1)
              .findAny();
      if (add.isPresent()) {
        for (int i = 0; i < 10; i++) {
          add.get().invoke(list, DataRizer.instantiate(valueClazz));
        }
      }
      return (Collection) list;
    }
    throw new DataRizerException("Unknown Collection and type to instantiate:");
  }

  private static <Z> List<Z> visitCollection(Class<Z> clazz) {
    return null;
  }
}
