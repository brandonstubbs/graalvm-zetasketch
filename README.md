## Graalvm + Zetasketch Invalid Serialization Values

NOTE: This is no longer failing with the committed native-image reflect-config.json




This is a simple reproduction of serializing HyperLogLogPlusPlus data and
deserializing it again.
The docker files test the following:
- jvm run
- native run

In both docker images the `jvm run` works. In the ubuntu image the `native run`
fails.

Clojure Version: https://github.com/clj-easy/graalvm-clojure/tree/master/zetasketch \
Similar Issue: https://github.com/quarkusio/quarkus/issues/35125

### Steps to reproduce
checkout project

#### Working Test (GraalVM Image)
Run the test within the graalvm-community docker image
```sh
docker build --progress=plain -t graalvm-zetasketch -f Dockerfile.graal .
```

You will see outputs:
```
...
>>>> JVM run successful
#10 [6/8] RUN ./gradlew run
...
#10 61.21 RESULT1: 3
#10 61.31 DATA1: [8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, 16, 3, 24, 15, 32, 20, 50, 9, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#10 61.31 RESULT2: 3
#10 61.31 DATA2: [8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, 16, 3, 24, 15, 32, 20, 50, 9, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#10 61.32 Done
...

>>>> native build
#11 [7/8] RUN ./gradlew nativeCompile
...
#11 86.06  Java version: 20.0.2+9, vendor version: GraalVM CE 20.0.2+9.1
#11 86.15  Graal compiler: optimization level: 2, target machine: compatibility
#11 86.16  C compiler: gcc (redhat, x86_64, 4.8.5)
#11 86.16  Garbage collector: Serial GC (max heap size: 80% of RAM)
...

>>>> native run
...
#12 37.37 > Task :app:nativeRun
#12 37.37 RESULT1: 3
#12 37.37 DATA1: [8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, 16, 3, 24, 15, 32, 20, 50, 9, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#12 37.37 RESULT2: 3
#12 37.37 DATA2: [8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, 16, 3, 24, 15, 32, 20, 50, 9, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#12 37.37 Done
...
```
In all cases `RESULT1 == RESULT2 && DATA1 == DATA2`


#### Failing Test (Ubuntu Image)
Run the test within the ubuntu docker image
```sh
docker build --progress=plain -t graalvm-zetasketch -f Dockerfile.ubuntu .
```

You will see outputs:
```
...
>>>> JVM run successful
#12 [ 8/10] RUN ./gradlew run
...
#12 6.536 RESULT1: 3
#12 6.536 DATA1: [8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, 16, 3, 24, 15, 32, 20, 50, 9, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#12 6.537 RESULT2: 3
#12 6.537 DATA2: [8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, 16, 3, 24, 15, 32, 20, 50, 9, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#12 6.537 Done
...

>>>> native build
#13 [ 9/10] RUN ./gradlew nativeCompile
...
#13 9.133  Java version: 20.0.2+9, vendor version: Oracle GraalVM 20.0.2+9.1
#13 9.134  Graal compiler: optimization level: 2, target machine: armv8-a, PGO: ML-inferred
#13 9.135  C compiler: gcc (linux, aarch64, 11.4.0)
#13 9.136  Garbage collector: Serial GC (max heap size: 80% of RAM)
...

>>>> native run
#14 [10/10] RUN ./gradlew nativeRun
...
#14 3.644 RESULT1: 3
#14 3.645 Exception in thread "main" java.lang.IllegalArgumentException: com.google.zetasketch.shaded.com.google.protobuf.InvalidProtocolBufferException: Protocol message contained an invalid tag (zero).
#14 3.645 DATA1: [0, 0, 0, 0, 0, 0, 0, 0, 8, 112, 16, 3, 24, 2, 32, 11, -126, 7, 17, -85, -36, 7, -57, -105, 10, -39, -93, 34]
#14 3.645       at com.google.zetasketch.HyperLogLogPlusPlus.forProto(HyperLogLogPlusPlus.java:131)
#14 3.646       at com.google.zetasketch.HyperLogLogPlusPlus.forProto(HyperLogLogPlusPlus.java:119)
#14 3.646       at graalvm.zetasketch.App.main(App.java:27)
#14 3.647 Caused by: com.google.zetasketch.shaded.com.google.protobuf.InvalidProtocolBufferException: Protocol message contained an invalid tag (zero).
#14 3.647       at com.google.zetasketch.shaded.com.google.protobuf.CodedInputStream$ArrayDecoder.readTag(CodedInputStream.java:652)
#14 3.647       at com.google.zetasketch.internal.hllplus.State.parse(State.java:197)
#14 3.647       at com.google.zetasketch.HyperLogLogPlusPlus.forProto(HyperLogLogPlusPlus.java:128)
#14 3.647       ... 2 more
#14 3.647
#14 3.647 FAILURE: Build failed with an exception.
...
```

In this case the JVM run works, the native run fails. You can see the leading
`0, 0, 0, 0, 0, 0, 0, 0,` in the serialized data.
