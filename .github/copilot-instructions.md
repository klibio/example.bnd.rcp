# GitHub Copilot Instructions — example.bnd.rcp

## Project Overview

Eclipse 4 RCP exemplar built with **bndtools** (v4.3.1) and **Gradle**. Demonstrates three Eclipse application types (headless, SWT dialog, E4 RCP product) with OSGi Declarative Services, CI/CD via GitHub Actions, and Docker-based VNC UI delivery.

- **Java**: 21 (source, target, compliance)
- **Eclipse Platform**: 2025-03 (R-4.35)
- **OSGi**: R7 APIs via enRoute
- **Build**: `biz.aQute.bnd.workspace` Gradle plugin
- **Group ID**: `klib.io`

## Repository Structure

| Directory | Purpose |
|-----------|---------|
| `cnf/` | bnd workspace configuration (`build.bnd`, repository plugins, shared settings) |
| `example.rcp.app.ui/` | Full Eclipse 4 RCP product — handlers, parts, application model (`.e4xmi`) |
| `example.rcp.ui/` | SWT progress-dialog application |
| `example.rcp.headless/` | Terminal-only Equinox application |
| `example.osgi.services/` | OSGi DS service patterns (sub-bundles via `-sub: *.bnd`) |
| `example.rcp.app.ui.swtbot.tests/` | SWTBot UI automation tests |
| `example.feature/` | P2 feature packaging |
| `io.klib.eclipse.snippets/` | SWT/Eclipse code reference snippets |

## Build & Test Commands

```bash
# Build all projects
./gradlew build

# Export platform-specific product
./gradlew export.app.ui_linux.gtk.x86-64
./gradlew export.app.ui_win32.win32.x86-64
./gradlew export.app.ui_macosx.cocoa.x86-64

# Export all three application types (Linux)
./gradlew export.app.ui_linux.gtk.x86-64 \
          export.12_equinoxapp_linux.gtk.x86-64 \
          export.ui_linux.gtk.x86-64

# Run tests
./gradlew test

# Docker build & run (port 5800 → web VNC)
docker build -t klibio/example.bnd.rcp .
docker run -d -p 5800:5800 klibio/example.bnd.rcp
```

## Bundle Naming Conventions

- Symbolic name matches the directory name: `example.rcp.app.ui`, `example.rcp.ui`, etc.
- Java packages mirror the BSN: `example.rcp.app.ui.handlers`, `example.rcp.app.ui.parts`
- All bundles use `singleton:=true` (required by Eclipse platform)
- Versions use timestamp qualifier: `0.1.0.${tstamp;yyyyMMdd-HHmmss}`
- Sub-bundles in `example.osgi.services/` use the pattern `<bsn>.<filename-stem>` (e.g. `11_immediate.bnd` → `example.osgi.services.immediate`)

## bnd.bnd Conventions

Every bundle's `bnd.bnd` follows this structure:

```bnd
-include: ${workspace}/cnf/fixedIndices/bnd_buildpath_Eclipse_Platform.bndrun

-buildpath: \
    ${fea_org.eclipse.rcp_4.35.0.v20250228-0640},\
    <other deps>

-includeresource: ./root/

Bundle-SymbolicName: ${project.name};singleton:=true
Bundle-Version: 0.1.0.${tstamp;yyyyMMdd-HHmmss}

Private-Package: \
    example.<bundle>.<package>
```

- Non-API packages go in `Private-Package`; only public API goes in `Export-Package`
- Static resources (icons, CSS, model files) live in `root/` and are included via `-includeresource: ./root/`
- Use `${project.name}` macro for `Bundle-SymbolicName` — never hard-code it

## .bndrun Files

Platform-specific launchers follow a strict naming convention:

```
<bundle>/<name>_<os>.<ws>.<arch>.bndrun
```

Examples: `app.ui_linux.gtk.x86-64.bndrun`, `app.ui_win32.win32.x86-64.bndrun`

- A `_shared.bndrun` contains cross-platform `runbundles` and `-runee` directives
- Platform-specific files add `-runfw`, `-runproperties`, and blacklist entries for other platforms
- Shared JVM args (G1GC, string dedup) are in `cnf/shared/`

## OSGi Declarative Services Patterns

```java
// Immediate component (activates at bundle start)
@Component(immediate = true, service = ImmediateService.class)
public class ImmediateService {
    @Activate void activate() { /* setup */ }
    @Deactivate void deactivate() { /* cleanup */ }
}

// Service consumer via ServiceTracker (15-second timeout)
ServiceTracker<ImmediateService, ImmediateService> tracker =
    new ServiceTracker<>(bundleContext, ImmediateService.class, null);
tracker.open(true);
ImmediateService svc = tracker.waitForService(15_000L);
```

## Eclipse 4 RCP Patterns

- **Application model**: `root/Application.e4xmi` — declarative UI (windows, parts, commands, menus, toolbars)
- **Handlers**: Annotated with `@Execute`; wired via `bundleclass://` URI in `.e4xmi`
- **Parts**: Use `@Inject` for Eclipse DI (`EPartService`, `EModelService`, `IEventBroker`, etc.)
- **Lifecycle**: `E4LifeCycle` with `@PostContextCreate`, `@PreSave`, `@ProcessAdditions`, `@ProcessRemovals`
- **Commands**: Defined in `.e4xmi` with keybindings (e.g. Ctrl+Q for quit)
- **CSS theming**: `root/css/default.css`
- **Product definition**: `plugin.xml` extension point `org.eclipse.core.runtime.products`

```java
// Typical E4 Handler
public class QuitHandler {
    @Execute
    public void execute(IWorkbench workbench) {
        workbench.close();
    }
}

// Typical E4 Part
public class SamplePart {
    @Inject
    private EPartService partService;

    @PostConstruct
    public void createComposite(Composite parent) { /* SWT widgets */ }

    @Focus
    public void onFocus() { /* focus logic */ }
}
```

## Testing

- **SWTBot tests** live in `example.rcp.app.ui.swtbot.tests/`; use `SWTBotJunit4ClassRunner`
- Tests run in a background thread during OSGi runtime startup (20-second timeout, 100ms playback delay)
- OSGi service tests use `ServiceTracker.waitForService()` with explicit timeouts
- Tests are declared in `swtbot_<platform>.bndrun` with `-tester: biz.aQute.junit`
- Currently only the `win32.win32.x86-64` platform has a SWTBot bndrun

## Platform Support

| Platform | OS/WS/Arch |
|----------|-----------|
| Linux | `linux.gtk.x86-64` |
| macOS Intel | `macosx.cocoa.x86-64` |
| macOS Apple Silicon | `macosx.cocoa.aarch64` |
| Windows | `win32.win32.x86-64` |

## Key Conventions

- **Never use PDE launch configurations** for development — use `.bndrun` files exclusively
- **Repository plugins** in `cnf/build.bnd` are numbered (`-plugin.1.` through `-plugin.9.`) — preserve ordering when adding new repos
- **Contracts enforcement**: `-contract: *` is always active; all used APIs must be from declared `buildpath`
- **Git metadata** is embedded in bundle manifests via `${git.describe}` and `${git.sha}` macros
- **Maven POM generation** is automatic (`-pom: true`) — do not add manual POM files
- The `cnf/` project is the bnd workspace root; all workspace-level settings belong there
