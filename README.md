# JavaCard Template project with Gradle

[![Build Status](https://travis-ci.org/kategray/midas.svg?branch=master)](https://travis-ci.org/ph4r05/javacard-gradle-template)

Key diversification JavaCard applet.

What currently works:

* Generating a diversification key for arbitrary IDs using AES-128 CMAC and a static key.


## Building installable cap file

- Run Gradle wrapper `./gradlew` on Unix-like system or `./gradlew.bat` on Windows
to build the project for the first time (Gradle will be downloaded if not installed).

- Setup your Applet ID (`AID`) in the `./applet/build.gradle`.

- Run the `buildJavaCard` task:

```
./gradlew buildJavaCard  --info --rerun-tasks
```

Generates a new cap file `./applet/out/cap/applet.cap`

Note: `--rerun-tasks` is to force re-run the task even though the cached input/output seems to be up to date.

Typical output:

```
> Task :applet:buildJavaCard
Putting task artifact state for task ':applet:buildJavaCard' into context took 0.0 secs.
Executing task ':applet:buildJavaCard' (up-to-date check took 0.0 secs) due to:
  Task has not declared any outputs.
[ant:cap] INFO: using JavaCard v2.2.2 SDK in C:\Kate\JavaCard\midas\libs-sdks\jc222_kit
[ant:cap] Building CAP with 1 applet from package com.codebykate.smartcard
[ant:cap] com.codebykate.smartcard.MidasApplet F044696E65726F01
[ant:compile] Compiling 3 source files to C:\Users\kateb\AppData\Local\Temp\jccpro2374824316456133018
[ant:cap] CAP saved to C:\Kate\JavaCard\midas\applet\build\javacard\applet.cap
[ant:exp] EXP saved to C:\Kate\JavaCard\midas\applet\build\javacard\applet.exp\com\codebykate\smartcard\javacard\smartcard.exp
[ant:jca] JCA saved to C:\Kate\JavaCard\midas\applet\build\javacard\applet.jca

:applet:buildJavaCard (Thread[Task worker,5,main]) completed. Took 1.895 secs.
```

## Running tests

```
./gradlew test --info --rerun-tasks
```

Output:

```
Gradle suite > Gradle test STANDARD_OUT
    Connecting to card... Done.

Gradle suite > Gradle test > tests.AppletTest.testDiversify STANDARD_OUT
    --> [8021000000] 5
    <-- 97DD6E5A882CBD564C39AE7D1C5A31AA 9000 (16)
    --> [80210000106BC1BEE22E409F96E93D7E117393172A00] 22
    <-- D0BC5BB4D6F60D5B17B7BF794B45436D 9000 (16)
    --> [80210000286BC1BEE22E409F96E93D7E117393172AAE2D8A571E03AC9C9EB76FAC45AF8E5130C81C46A35CE41100] 46
    <-- 989BAFBFCE64B39B28EDF0379E6EF5DD 9000 (16)
    --> [80210000406BC1BEE22E409F96E93D7E117393172AAE2D8A571E03AC9C9EB76FAC45AF8E5130C81C46A35CE411E5FBC1191A0A52EFF69F2445DF4F9B17AD2B417BE66C371000] 70
    <-- 58279A2397F232989C4C28C1B1710979 9000 (16)

Gradle suite > Gradle test > tests.AppletTest.testGetRandom STANDARD_OUT
    --> [8010000000] 5
    <-- 5291AD4526C53FA4FBD9C8E330FAFBD1117E9DCF2307C1BFDAC41BCC64884A6A 9000 (32)
    --> [8010000000] 5
    <-- 9EE4A7461300511243BAF0031A36F6C12A6A7EA541B6BC86035642D31384BE86 9000 (32)
Finished generating test XML results (0.004 secs) into: C:\Kate\JavaCard\midas\applet\build\test-results\test
Generating HTML test report...
Finished generating test html results (0.006 secs) into: C:\Kate\JavaCard\midas\applet\build\reports\tests\test

:applet:test (Thread[Task worker,5,main]) completed. Took 0.576 secs.
```

### JavaCard support

Midas requires JavaCard 2.2.1 support or higher.