# SWTBot Tests

This project contains SWTBot UI tests for `example.rcp.app.ui`. Available test launches:

```text
swtbot_linux.gtk.x86-64.bndrun
swtbot_macosx.cocoa.aarch64.bndrun
swtbot_win32.win32.x86-64.bndrun
```

Each bndrun starts the full Eclipse 4 RCP application and runs `example.rcp.app.ui.swtbot.tests.SamplePartSWTBotTest` against the live SWT UI.

## CLI Launch

Run from the workspace root with Java 21:

```bash
export JAVA_HOME='~/.ecdev/java/ee/JAVA21'
export PATH='~/.ecdev/java/ee/JAVA21/bin':"$PATH"
java -jar ~/biz.aQute.bnd.jar runtests \
  example.rcp.app.ui.swtbot.tests/swtbot_win32.win32.x86-64.bndrun
```

Use the matching bndrun for the target platform:

```bash
java -jar ~/biz.aQute.bnd.jar runtests \
  example.rcp.app.ui.swtbot.tests/swtbot_linux.gtk.x86-64.bndrun

java -jar ~/biz.aQute.bnd.jar runtests \
  example.rcp.app.ui.swtbot.tests/swtbot_macosx.cocoa.aarch64.bndrun
```

Use `bnd runtests` for this `.bndrun` file. Do not use `bnd run`: it starts the framework but does not activate the bnd test runner. Do not use `bnd test` with a `.bndrun`: it only processes projects and `bnd.bnd` files.

CLI test discovery uses the `Test-Cases` property in the bndrun file. Reports are written to `reports/`; screenshots are written to `example.rcp.app.ui.swtbot.tests/screenshots/`.

## Screenshot Capture Modes

`SamplePartSWTBotTest` now captures three image variants at each checkpoint.

- `captureScreen(name)` uses `SWTUtils.captureScreenshot(...)` and captures the full desktop/screen.
- `captureShell(shell, name)` uses SWT `GC` on the shell widget and captures the shell client area.
- `captureSWT(shell, name)` uses native SWT APIs (`Display`, `GC.copyArea`, `ImageLoader`) to capture the shell rectangle from the SWT display.

For each logical checkpoint (`01_main_window_open`, `02_table_verified`, `03_before_quit`) the test writes:

```text
*_screen.png
*_shell.png
*_swt.png
```

All files are stored under `example.rcp.app.ui.swtbot.tests/screenshots/`.

## Gradle Launch

Gradle creates tasks for each bndrun file. Use the `testrun.*` tasks for SWTBot test execution:

```bash
export JAVA_HOME=~/.ecdev/java/ee/JAVA21
export PATH=~/.ecdev/java/ee/JAVA21/bin:$PATH

./gradlew :example.rcp.app.ui.swtbot.tests:testrun.swtbot_win32.win32.x86-64 --no-daemon
./gradlew :example.rcp.app.ui.swtbot.tests:testrun.swtbot_linux.gtk.x86-64 --no-daemon
./gradlew :example.rcp.app.ui.swtbot.tests:testrun.swtbot_macosx.cocoa.aarch64 --no-daemon
```

Resolver and export tasks are also generated per launch:

```bash
./gradlew :example.rcp.app.ui.swtbot.tests:resolve.swtbot_linux.gtk.x86-64 --no-daemon
./gradlew :example.rcp.app.ui.swtbot.tests:export.swtbot_linux.gtk.x86-64 --no-daemon
```

`testOSGi` is intentionally disabled for this project by `-nojunitosgi: true`; it would launch the bare `bnd.bnd` project without the SWTBot bndrun runtime.

To run the Linux GTK launch from WSL Ubuntu with WSLg:

```bash
wsl -d Ubuntu -- bash -lc 'cd example.bnd.rcp && \
  java -jar biz.aQute.bnd.jar runtests \
    example.rcp.app.ui.swtbot.tests/swtbot_linux.gtk.x86-64.bndrun'
```

## IDE Launch

In Eclipse with bndtools:

1. Open the platform bndrun, for example `swtbot_win32.win32.x86-64.bndrun`.
2. Resolve if the Run tab reports unresolved bundles.
3. Right-click the bndrun file.
4. Use **Debug As > Bnd OSGi Test Launcher (JUnit)** to debug breakpoints in the test class.

The IDE launch uses bnd's JUnit tester in automatic mode when launching the whole bndrun. For that reason, the test bundle must contain a `Test-Cases` manifest header. This is configured in `bnd.bnd`:

```bnd
Test-Cases: ${classes;CONCRETE;ANNOTATED;org.junit.Test}
```

Keep the explicit `Test-Cases` property in the bndrun as well; the CLI path reads that property directly.

## Important Settings

`-tester: biz.aQute.junit` tells bnd/bndtools to inject the JUnit tester. Do not also add `biz.aQute.junit` to `-runbundles`; that can start a second tester and cause duplicate JUnit package wiring.

`tester.separatethread=true` lets the SWT/E4 application own the main thread while SWTBot runs on a background test thread.

`-testunresolved: false` disables the unresolved-bundle pre-check in both CLI and IDE launch paths.

`-nojunitosgi: true` in `bnd.bnd` prevents Gradle's bare project `testOSGi` task from trying to launch this UI test without the bndrun runtime configuration.

## Documentation

- bnd testing: https://bnd.bndtools.org/chapters/310-testing.html
- SWTBot overview: https://wiki.eclipse.org/SWTBot/
- SWTBot user guide: https://wiki.eclipse.org/SWTBot/UsersGuide
- SWTBot troubleshooting: https://wiki.eclipse.org/SWTBot/Troubleshooting
- SWTBot API docs: https://download.eclipse.org/technology/swtbot/releases/latest/apidocs/