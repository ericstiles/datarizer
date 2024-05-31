# datarizer: Object Instantiation With Random Data

This project is built as a quick way to instantiate any type of java object with random data for testing. Instantiation is reduced to one line.

# Why
In a lot of testing scenarios, test objects are hardcoded with same exact test values and generally that is done, at best, through a "create" method in the test class that is many lines of code. This is a gap in the testing process. This is gap in that it tests in a minimally bound set properties. 
This project is built to fill that gap. 

Certainly key values will need to be set in the test, but this project is built to fill in the rest of the data.

## Yet Another Object Instantiator...
There are some other projects out there that do this and some do more. I found, hopefully objectively, that the one or two I looked at were either too complex or didn't provide the flexibility I wanted.

# How

This project uses reflection to instantiate objects. It will instantiate the object and then fill out the data fields
It is designed to look for constructors or builders. If a constructor is found, it will use that. If a builder is found, it will use that. If neither is found, it will use the default constructor.

To minimize recursive object creation, the project will only go two levels deep. If an object is null, it will not go any deeper.

## Security
Reflection isn't the most secure mechanism in production code. This project is designed for testing and mocking data. It is not recommended to use this in production code.

# Building
```Java
    mvn clean package
```

See the jar file in the target directory: datarizer-1.0-SNAPSHOT.jar

# Example Usage

**See The [Tests!](src/test/java/com/test/)**

## Simple Example

### Constructor
```java
    class Cat() {
    }
    
    Cat cat = RandomInstantiator.instantiate(Cat.class);
    
    assertNotNull(cat);
```

### Builder Pattern

```java
    class ClassOfMethodReturnsString {
        public static final String of() {
            return "hello";
        }
    }
    
    ClassOfMethodReturnsString classOfMethodReturnsString = RandomInstantiator.instantiate(ClassOfMethodReturnsString.class);
    
    assertNotNull(classOfMethodReturnsString);
```

## More Complex Example

### Constructors

```Java
    class Engine(){
        String type;
    }
    
    class Car() {
        Engine engine;
        int seats;
    }

    Car car = RandomInstantiator.instantiate(Car.class);

    assertNotNull(car.engine.type);
```

### Builder Pattern

```Java
    class ClassFromNonEmptyBuilder {
        private final @NonNull String name;
    
        private ClassFromNonEmptyBuilder(@NonNull String name) {
            if (name == null) {
                throw new NullPointerException("name is marked non-null but is null");
            } else {
                this.name = name;
            }
        }
    
        public static ClassFromNonEmptyBuilder of(@NonNull String name) {
            return new ClassFromNonEmptyBuilder(name);
        }
    }

    assertNotNull(RandomInstantiator.instantiate(ClassFromNonEmptyBuilder.class));
```
Another example of a builder pattern with a list

```Java
    private ClassFromNonEmptyBuilderWithList(@NonNull List<String> stringList, @NonNull List<Integer> integerList) {
        if (stringList == null) {
            throw new NullPointerException("stringList is marked non-null but is null");
        } else if (integerList == null) {
            throw new NullPointerException("integerList is marked non-null but is null");
        } else {
            this.stringList = stringList;
            this.integerList = integerList;
        }
    }

    public static ClassFromNonEmptyBuilderWithList of(@NonNull List<String> stringList, @NonNull List<Integer> integerList) {
        return new ClassFromNonEmptyBuilderWithList(stringList, integerList);
    }

    ClassFromNonEmptyBuilderWithListOfList instantiate =
        RandomInstantiator.instantiate(ClassFromNonEmptyBuilderWithListOfList.class);

    assertFalse(instantiate.getListOfListsOfIntegers().get(0).isEmpty());
```

