# thrifty-maven-plugin

[![Build Status](https://www.travis-ci.org/timvlaer/thrifty-maven-plugin.svg?branch=master)](https://www.travis-ci.org/timvlaer/thrifty-maven-plugin)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.timvlaer/thrifty-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.timvlaer/thrifty-maven-plugin)

Thin wrapper around the [thrifty-compiler](https://github.com/microsoft/thrifty/tree/master/thrifty-compiler).
This maven plugin generates the placeholder code for a thrift IDL file
using the [thrifty](https://github.com/microsoft/thrifty) library.

Advantages of using Thrifty over the regular Thrift distribution for Java code generation:
* All compilation happens on the JVM, no need to install Thrift binaries.
* Thrifty generates better java code: immutable objects with builders.
* Thrifty has a smaller footprint, the generated byte code is perfectly compatible.

## Usage

Add the following plugin to the `<build>` part of your Maven pom.xml file
```xml
<plugin>
    <groupId>com.github.timvlaer</groupId>
    <artifactId>thrifty-maven-plugin</artifactId>
    <version>0.6.0</version>
    <configuration>
        <thriftFiles>
            <file>thrift-schema/internal.thrift</file>
        </thriftFiles>
        <enableConvenienceMethods>true</enableConvenienceMethods><!--default is false-->
        <generateGettersInBuilders>false</generateGettersInBuilders><!--default is false-->
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>thrifty-compiler</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The generated code depends on `thrifty-runtime`, so add the following to your dependency list. 
```xml
<dependency>
    <groupId>com.microsoft.thrifty</groupId>
    <artifactId>thrifty-runtime</artifactId>
    <version>2.1.1</version>
</dependency>
```

## Java 9 and above

Add the `maven.compiler.release` property to the pom.xml file. The plugin will pick this up and configure Thrifty accordingly. 
```xml
<maven.compiler.release>11</maven.compiler.release>
```

## Generated convenience methods
This Maven plugin generates extra methods if you set `<enableConvenienceMethods>` to `true` (default is `false`). 

On all classes:
* `public static Builder builder()`, as a shortcut to `new StructName.Builder()` 
* `public static Builder builder(StructName prototype)`, as a shortcut to `new StructName.Builder(struct)` 
* `public Builder toBuilder()`, as a shortcut to `new StructName.Builder(struct)` 

On classes that are based on Thrift `union` types:
* `public String tag()` which returns the name of the filled field
* `public Object value()` which returns the value of the filled field (untyped)
* For each union field, a static factory method, e.g. `public static UnionName unionValue(String unionValue)`

If you set `<generateGettersInBuilders>` to `true`, this plugin will generate getter methods on builders. 
This might ease migration from the default (mutable) thrift generated code. 
[Effective Java](https://www.goodreads.com/book/show/34927404-effective-java) doesn't recommend Getters on builders, 
so the default for this setting is `false`.