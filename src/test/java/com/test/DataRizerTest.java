package com.test;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

enum EnumTestHasEnums {
  A,
  B
}

enum EnumTestHasNoEnum {}

class DataRizerTest {

  @DisplayName("Constructor")
  @Nested
  class Constructor {

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorMap()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorMap catFromCatNonEmptyConstructorTwo =
          DataRizer.instantiate(ClassFromNonEmptyConstructorMap.class);
      assertTrue(catFromCatNonEmptyConstructorTwo.getStringIntegerMap().size() > 0);
    }

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorTreeMap()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorTreeMap catFromCatNonEmptyConstructorTwo =
          DataRizer.instantiate(ClassFromNonEmptyConstructorTreeMap.class);
      assertTrue(catFromCatNonEmptyConstructorTwo.getStringIntegerTreeMap().size() > 0);
    }

    @Test
    void testInstantiateShouldThrowErrorOnRecursionOfSameClassWithNonNull() {
      assertThrows(
          DataRizerException.class,
          () -> DataRizer.instantiate(ClassFromClassNonEmptyRecursiveBuilder.class));
    }

    @Test
    void testInstantiateShouldHandleClassOnRecursion()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      assertNull(
          DataRizer.instantiate(com.test.ClassFromClassNull.class)
              .getClassFromClassNull());
    }

    @Test
    void testInstantiateShouldInstantiateEmptyConstructor()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      assertNotNull(DataRizer.instantiate(ClassEmptyConstructor.class));
    }

    @Test
    void testInstantiateShouldInstantiateEmptyConstructorAndSetter()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassEmptyConstructorWithSetter classEmptyConstructorWithSetter =
          DataRizer.instantiate(ClassEmptyConstructorWithSetter.class);
      assertNotNull(classEmptyConstructorWithSetter.getStringValue());
    }

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorListInterface()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorListInterface classFromNonEmptyConstructorListInterface =
          DataRizer.instantiate(ClassFromNonEmptyConstructorListInterface.class);
      assertNotNull(classFromNonEmptyConstructorListInterface.getStringList());
      assertTrue(classFromNonEmptyConstructorListInterface.getStringList().size() > 0);
      assertNotNull(classFromNonEmptyConstructorListInterface.getStringList().get(0));
    }

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorSpecificList()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorArrayList classFromNonEmptyConstructor =
          DataRizer.instantiate(ClassFromNonEmptyConstructorArrayList.class);
      assertNotNull(classFromNonEmptyConstructor.getStringArrayList());
      assertTrue(classFromNonEmptyConstructor.getStringArrayList().size() > 0);
      assertNotNull(classFromNonEmptyConstructor.getStringArrayList().get(0));
    }

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorSetInterface()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorSetInterface classFromNonEmptyConstructorListInterface =
          DataRizer.instantiate(ClassFromNonEmptyConstructorSetInterface.class);
      assertNotNull(classFromNonEmptyConstructorListInterface.getStringSet());
      assertTrue(classFromNonEmptyConstructorListInterface.getStringSet().size() > 0);
      assertTrue(
          classFromNonEmptyConstructorListInterface.getStringSet().toArray()[0] instanceof String);
    }

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorSpecificSet()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorHashSet classFromNonEmptyConstructor =
          DataRizer.instantiate(ClassFromNonEmptyConstructorHashSet.class);
      assertNotNull(classFromNonEmptyConstructor.getStringHashSet());
      assertTrue(classFromNonEmptyConstructor.getStringHashSet().size() > 0);
      assertTrue(classFromNonEmptyConstructor.getStringHashSet().toArray()[0] instanceof String);
    }

    @Test
    void testInstantiateShouldInstantiateNonEmptyConstructorTwo()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyConstructorTwo classFromNonEmptyConstructorTwo =
          DataRizer.instantiate(ClassFromNonEmptyConstructorTwo.class);
      assertNotNull(
          classFromNonEmptyConstructorTwo.getClassFromNonEmptyConstructorListInterfaceList());
      assertTrue(
          classFromNonEmptyConstructorTwo.getClassFromNonEmptyConstructorListInterfaceList().size()
              > 0);
      assertNotNull(
          classFromNonEmptyConstructorTwo
              .getClassFromNonEmptyConstructorListInterfaceList()
              .get(0));
    }
  }

  @DisplayName("Setter Methods")
  @Nested
  class SetterMethods {
    @Test
    void testGetParametersSetMethod()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Method of =
          Arrays.stream(ClassEmptyConstructorWithSetter.class.getMethods())
              .filter(method -> method.getName().startsWith("set"))
              .findAny()
              .get();
      Object[] parameters = DataRizer.generateMethodParametersData(of, 2);
      assertEquals(1, parameters.length);
    }

    @Test
    void testGetParametersSetMethodIsRecursive()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Method of =
          Arrays.stream(ClassEmptyConstructorWithSetterRecursive.class.getMethods())
              .filter(method -> method.getName().startsWith("set"))
              .findAny()
              .get();
      Object[] parameters = DataRizer.generateMethodParametersData(of, 2);
      assertEquals(1, parameters.length);
    }

    @Test
    void testGetParametersEmptyStaticBuilder()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Method of =
          Arrays.stream(ClassEmptyOfStaticBuilder.class.getMethods())
              .filter(method -> method.getName().equals("of"))
              .findAny()
              .get();
      Object[] parameters = DataRizer.generateMethodParametersData(of, 2);
      assertEquals(0, parameters.length);
    }

    @Test
    void testGetParametersNonEmptyStaticBuilder()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Method of =
          Arrays.stream(ClassFromNonEmptyBuilder.class.getMethods())
              .filter(method -> method.getName().equals("of"))
              .findAny()
              .get();
      Object[] parameters = DataRizer.generateMethodParametersData(of, 2);
      assertEquals(1, parameters.length);
    }
  }

  @DisplayName("Collections - List")
  @Nested
  class Collections_List {

    @Test
    void testsListImplementationsNoGeneric() {
      List.of(ArrayList.class, Vector.class, LinkedList.class)
          .forEach(
              clazz -> {
                List<String> list = null;
                try {
                  list = DataRizer.instantiate(clazz);
                  assertEquals(0, list.size());
                } catch (InvocationTargetException
                    | IllegalAccessException
                    | InstantiationException e) {
                  e.printStackTrace();
                }
              });
    }

    @Test
    void testsListImplementationsStringGeneric() {
      List.of(ArrayList.class, Vector.class, LinkedList.class)
          .forEach(
              clazz -> {
                List<String> list = null;
                try {
                  list = DataRizer.instantiate(clazz, String.class);
                  assertTrue(0 < list.size());
                  assertTrue(10 >= list.size());
                  assertNotNull(list.get(0));
                } catch (InvocationTargetException
                    | IllegalAccessException
                    | InstantiationException e) {
                  e.printStackTrace();
                }
              });
    }

    @Test
    void testsListInterfaceNoGeneric()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      List list = DataRizer.instantiate(List.class);
      assertTrue(list.size() > 0);
    }

    @Test
    void testsListInterfaceStringGeneric()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      List<String> list = DataRizer.instantiate(List.class, String.class);
      assertTrue(list.size() > 0);
    }
  }

  @DisplayName("Collections - Set")
  @Nested
  class Collections_Set {
    @Test
    void testsSetInterfaceNoGeneric()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Collection<BigDecimal> instantiate = DataRizer.instantiate(Set.class);
      assertTrue(instantiate.size() > 0);
    }

    @Test
    void testsSetInterfaceStringGeneric()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {

      Collection<BigDecimal> instantiate =
          DataRizer.instantiate(Set.class, BigDecimal.class);
      assertTrue(instantiate.size() > 0);
    }

    @Test
    void testsSetImplementationsStringGeneric() {
      List.of(HashSet.class, TreeSet.class, LinkedHashSet.class)
          .forEach(
              clazz -> {
                Set<String> set = null;
                try {
                  set = DataRizer.instantiate(clazz, String.class);
                  assertTrue(0 < set.size());
                  assertTrue(10 >= set.size());
                  assertTrue(set.toArray()[0] instanceof String);
                } catch (InvocationTargetException
                    | IllegalAccessException
                    | InstantiationException e) {
                  e.printStackTrace();
                }
              });
    }

    @Test
    void testsSetImplementationsNoGeneric() {
      List.of(HashSet.class, TreeSet.class, LinkedHashSet.class)
          .forEach(
              clazz -> {
                Set<String> set = null;
                try {
                  set = DataRizer.instantiate(clazz);
                  assertEquals(0, set.size());
                } catch (InvocationTargetException
                    | IllegalAccessException
                    | InstantiationException e) {
                  e.printStackTrace();
                }
              });
    }
  }

  @DisplayName("Collections - Map")
  @Nested
  class Collections_Map {

    @Test
    void testMapImplementationsKeyStringValueIntegerGenerics()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Map map = DataRizer.instantiate(HashMap.class, String.class, Integer.class);
      assertTrue(map.size() > 0);
    }

    @Test
    void testMapImplementationsNoGenerics()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Map map = DataRizer.instantiate(HashMap.class);
      assertEquals(0, map.size());
    }

    @Test
    void testMapInterfaceNoGeneric()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Map map = DataRizer.instantiate(Map.class);
      assertNotNull(map);
    }

    @Test
    void testMapInterfaceKeyStringValueIntegerGeneric()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Map map = DataRizer.instantiate(Map.class, String.class, Integer.class);
      assertNotNull(map);
      assertFalse(map.isEmpty());
      assertTrue(map.keySet().toArray()[0] instanceof String);
      assertTrue(map.values().toArray()[0] instanceof Integer);
    }
  }

  @DisplayName("Builder Patterns")
  @Nested
  class BuilderPatterns {

    @Test
    void testEmptyStaticBuilderMethodReturnsMethod() {
      Method method =
          DataRizer.getStaticBuilderMethods(
              ClassEmptyOfStaticBuilder.class, List.of("of"));
      assertNotNull(method);
    }

    @Test
    void testNonDefaultStaticBuilderMethodReturnsMethod() {
      Method method =
          DataRizer.getStaticBuilderMethods(
              ClassNonDefaultStaticBuilder.class, List.of("create"));
      assertNotNull(method);
    }

    @Test
    void testEmptyBuilderMethodNotFoundReturnsNull() {
      Method method =
          DataRizer.getStaticBuilderMethods(
              ClassEmptyOfStaticBuilder.class, List.of("create"));
      assertNull(method);
    }

    @Test
    void testEmptyBuilderMethodFoundDifferentClassReturnTypeShouldBeNull() {
      Method method =
          DataRizer.getStaticBuilderMethods(
              ClassOfMethodReturnsString.class, List.of("of"));
      assertNull(method);
    }

    @Test
    void testInstantiateShouldReturnClassFromEmptyBuilder()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      assertNotNull(DataRizer.instantiate(ClassEmptyOfStaticBuilder.class));
    }

    @Test
    void testInstantiateShouldReturnClassFromNonEmptyBuilder()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      assertNotNull(DataRizer.instantiate(ClassFromNonEmptyBuilder.class));
    }

    @Test
    void testInstantiateShouldReturnClassFromNonEmptyBuilderListGenericString()
        throws InvocationTargetException, IllegalAccessException, InstantiationException {
      ClassFromNonEmptyBuilderWithList classFromNonEmptyBuilderWithList =
          DataRizer.instantiate(ClassFromNonEmptyBuilderWithList.class);
      assertNotNull(classFromNonEmptyBuilderWithList.getStringList().get(0));
      assertNotNull(classFromNonEmptyBuilderWithList.getIntegerList().get(0));
    }

    @Test
    void testInstantiateShouldHaveListOfListOfIntegers()
        throws java.lang.reflect.InvocationTargetException, IllegalAccessException,
            InstantiationException {
      ClassFromNonEmptyBuilderWithListOfList instantiate =
          DataRizer.instantiate(ClassFromNonEmptyBuilderWithListOfList.class);
      assertFalse(instantiate.getListOfListsOfIntegers().get(0).isEmpty());
    }

    @Test
    void testInstantiateShouldHaveListOfSetOfIntegers()
        throws java.lang.reflect.InvocationTargetException, IllegalAccessException,
            InstantiationException {
      ClassFromNonEmptyBuilderWithListOfSet instantiate =
          DataRizer.instantiate(ClassFromNonEmptyBuilderWithListOfSet.class);
      assertFalse(instantiate.getListOfSetsOfIntegers().get(0).isEmpty());
    }

    @Test
    void testInstantiateShouldHaveListOfMapOfStringIntegers()
        throws java.lang.reflect.InvocationTargetException, IllegalAccessException,
            InstantiationException {
      ClassFromNonEmptyBuilderWithListOfMap instantiate =
          DataRizer.instantiate(ClassFromNonEmptyBuilderWithListOfMap.class);
      assertFalse(instantiate.getListOfMapOfStringIntegers().isEmpty());
    }

    @Test
    void testInstantiateShouldHaveMapOfStringIntegers()
        throws java.lang.reflect.InvocationTargetException, IllegalAccessException,
            InstantiationException {
      ClassFromNonEmptyBuilderWithMap instantiate =
          DataRizer.instantiate(ClassFromNonEmptyBuilderWithMap.class);
      assertFalse(instantiate.getStringIntegerMap().isEmpty());
    }
  }

  @DisplayName("Base Objects")
  @Nested
  class BaseObjects {
    @Test
    void testRandomString() {
      String string = DataRizer.getRandomString(10);
      assertTrue(string.matches("[a-zA-z0-9]+"));
      assertEquals(10, string.length());
    }

    @Test
    void testRandomStringFromGet() {
      String string = DataRizer.instantiateFromBaseObjects(String.class);
      assertTrue(string.matches("[a-zA-z0-9]+"));
      assertEquals(10, string.length());
    }

    @Test
    void testNullWhenNotInGet() {
      assertNull(DataRizer.instantiateFromBaseObjects(Thread.class));
    }

    @Test
    void testRandomInteger() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Integer.class));
    }

    @Test
    void testRandomLong() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Long.class));
    }

    @Test
    void testRandomBoolean() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Boolean.class));
    }

    @Test
    void testRandomFloat() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Float.class));
    }

    @Test
    void testRandomDouble() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Double.class));
    }

    @Test
    void testRandomBigDecimal() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(BigDecimal.class));
    }

    @Test
    void testRandomInstant() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Instant.class));
    }

    @Test
    void testRandomLocalDate() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(LocalDate.class));
    }

    @Test
    void testRandomLocalDateTime() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(LocalDateTime.class));
    }

    @Test
    void testRandomLocalTime() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(LocalTime.class));
    }

    @Test
    void testRandomDuration() {
      assertNotNull(DataRizer.instantiateFromBaseObjects(Duration.class));
    }
  }

  @DisplayName("Enums")
  @Nested
  class Enums {

    @Test
    void testGetEnumFromEnumWithValues() {
      assertTrue(
          Arrays.asList(EnumTestHasEnums.values())
              .contains(DataRizer.instantiateFromEnum(EnumTestHasEnums.class)));
    }

    @Test
    void testGetEnumFromEnumWithOutValuesShouldBeNull() {
      assertNull(DataRizer.instantiateFromEnum(String.class));
    }

    @Test
    void testThrowsExceptionFromEnumWithNoValues() {
      assertThrows(
          DataRizerException.class,
          () -> DataRizer.instantiateFromEnum(EnumTestHasNoEnum.class));
    }
  }
}

@RequiredArgsConstructor(staticName = "of")
class ClassFromNonEmptyBuilder {
  @NonNull private final String name;
}

@Getter
@RequiredArgsConstructor(staticName = "of")
class ClassFromNonEmptyBuilderWithList {
  @NonNull private final List<String> stringList;
  @NonNull private final List<Integer> integerList;
}

@Getter
@RequiredArgsConstructor(staticName = "of")
class ClassFromNonEmptyBuilderWithListOfList {
  @NonNull private final List<List<Integer>> listOfListsOfIntegers;
}

@Getter
@RequiredArgsConstructor(staticName = "of")
class ClassFromNonEmptyBuilderWithListOfSet {
  @NonNull private final List<Set<Integer>> listOfSetsOfIntegers;
}

@Getter
@RequiredArgsConstructor(staticName = "of")
class ClassFromNonEmptyBuilderWithListOfMap {
  @NonNull private final List<Map<String, Integer>> listOfMapOfStringIntegers;
}

@Getter
@RequiredArgsConstructor(staticName = "of")
class ClassFromNonEmptyBuilderWithMap {
  @NonNull private final Map<String, Integer> stringIntegerMap;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorListInterface {
  @NonNull private final List<String> stringList;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorTwo {
  @NonNull
  private final List<ClassFromNonEmptyConstructorListInterface>
      classFromNonEmptyConstructorListInterfaceList;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorArrayList {
  @NonNull private final ArrayList<String> stringArrayList;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorSetInterface {
  @NonNull private final Set<String> stringSet;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorHashSet {
  @NonNull private final HashSet<String> stringHashSet;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorMap {
  @NonNull private final Map<String, Integer> stringIntegerMap;
}

@Getter
@RequiredArgsConstructor()
class ClassFromNonEmptyConstructorTreeMap {
  @NonNull private final TreeMap<String, Integer> stringIntegerTreeMap;
}

@NoArgsConstructor(staticName = "of")
class ClassEmptyOfStaticBuilder {}

@NoArgsConstructor(staticName = "create")
class ClassNonDefaultStaticBuilder {}

class ClassOfMethodReturnsString {
  public static final String of() {
    return "hello";
  }
}

@RequiredArgsConstructor(staticName = "of")
class ClassFromClassNonEmptyRecursiveBuilder {
  @NonNull
  private final ClassFromClassNonEmptyRecursiveBuilder classFromClassNonEmptyRecursiveBuilder;
}

@Getter
@RequiredArgsConstructor(staticName = "of")
class ClassFromClassNull {
  private ClassFromClassNull classFromClassNull;
}

@NoArgsConstructor
class ClassEmptyConstructor {
  private String stringValue;
}

@NoArgsConstructor
@Setter
@Getter
class ClassEmptyConstructorWithSetter {
  private String stringValue;
}

@NoArgsConstructor
@Setter
@Getter
class ClassEmptyConstructorWithSetterRecursive {
  private ClassEmptyConstructorWithSetterRecursive classEmptyConstructorWithSetterRecursive;
}
