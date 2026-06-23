# AGENTS.md — example.bnd.rcp

> AI agent guidelines for the `example.bnd.rcp` workspace.  
> This file applies to all agents working in this repository (GitHub Copilot, Claude Code, Cursor, etc.).

---

## Project Identity

| Property | Value |
|----------|-------|
| Purpose | Eclipse 4 RCP exemplar with bndtools — headless, SWT dialog, and full E4 product |
| Java | 21 (source / target / compliance) |
| Eclipse Platform | 2025-03 (R-4.35) |
| OSGi spec | R7 via enRoute |
| Build | Gradle + `biz.aQute.bnd.workspace` plugin (v4.3.1) |
| Group ID | `klib.io` |
| License | Eclipse Public License v1.0 |

---

## Directory Map

```
cnf/                          # bnd workspace root — ALL workspace settings live here
  build.bnd                   # compiler options, repositories (-plugin.1–9), groupId, contracts
  fixedIndices/               # shared -include fragments (Eclipse Platform buildpath)
  shared/                     # cross-platform JVM args, OSGi native capabilities
  central.maven / library.maven  # Maven Central index files

example.rcp.app.ui/           # Full Eclipse 4 RCP product
  bnd.bnd                     # bundle manifest
  root/                       # resources bundled via -includeresource (icons, CSS, model)
    Application.e4xmi         # declarative application model
    css/default.css
    plugin.xml                # product extension point
  src/                        # Java sources (handlers, parts, lifecycle)
  app.ui_<os>.<ws>.<arch>.bndrun   # platform launchers
  app.ui_shared.bndrun        # cross-platform base (runbundles, -runee)

example.rcp.ui/               # Lightweight SWT progress-dialog application
example.rcp.headless/         # Terminal-only Equinox application

example.osgi.services/        # OSGi DS service patterns
  bnd.bnd                     # workspace bundle (aggregates sub-bundles)
  11_immediate.bnd            # → example.osgi.services.immediate
  12_equinoxapp.bnd           # → example.osgi.services.equinoxapp

example.rcp.app.ui.swtbot.tests/   # SWTBot UI automation tests
example.feature/              # P2 feature packaging
io.klib.eclipse.snippets/     # SWT/Eclipse code reference snippets
```

---

## Build & Test Commands

```bash
# Full workspace build
./gradlew build

# Run all tests
./gradlew test

# Export platform-specific E4 product
./gradlew export.app.ui_linux.gtk.x86-64
./gradlew export.app.ui_win32.win32.x86-64
./gradlew export.app.ui_macosx.cocoa.x86-64
./gradlew export.app.ui_macosx.cocoa.aarch64

# Export all three app types for Linux (used by Docker/CI)
./gradlew export.app.ui_linux.gtk.x86-64 \
          export.12_equinoxapp_linux.gtk.x86-64 \
          export.ui_linux.gtk.x86-64

# Docker image (web VNC on port 5800)
docker build -t klibio/example.bnd.rcp .
docker run -d -p 5800:5800 klibio/example.bnd.rcp
# Access UI: http://localhost:5800
```

**CI/CD**: GitHub Actions (`.github/workflows/actions_build.yml`) builds on push to feature/bugfix branches and PRs to main; publishes to Docker Hub (`klibio/example.bnd.rcp`).

---

## OSGi Bundle Conventions

### Naming

- Bundle Symbolic Name **must** match the project directory name.
- Java packages **must** mirror the BSN: `example.rcp.app.ui` → packages `example.rcp.app.ui.*`
- All Eclipse bundles require `singleton:=true`
- Sub-bundles in `example.osgi.services/` use `-sub: *.bnd`; BSN = parent BSN + `.` + filename stem  
  e.g. `11_immediate.bnd` → `example.osgi.services.immediate`

### bnd.bnd Template

```bnd
-include: ${workspace}/cnf/fixedIndices/bnd_buildpath_Eclipse_Platform.bndrun

-buildpath: \
    ${fea_org.eclipse.rcp_4.35.0.v20250228-0640},\
    jakarta.inject.jakarta.inject-api,\
    jakarta.annotation-api

-includeresource: ./root/

Bundle-SymbolicName: ${project.name};singleton:=true
Bundle-Version:      0.1.0.${tstamp;yyyyMMdd-HHmmss}

Private-Package: \
    example.<bundle>.<package>
```

**Rules**:
- Always use `${project.name}` macro — never hard-code `Bundle-SymbolicName`
- Non-API packages → `Private-Package`; only published API → `Export-Package`
- Static resources (icons, CSS, model files) → `root/` directory, included via `-includeresource: ./root/`
- Maven POM generation is automatic (`-pom: true` in `cnf/build.bnd`) — do not add manual POM files

### Repository Plugins

Repositories in `cnf/build.bnd` are numbered (`-plugin.1.` … `-plugin.9.`). **Preserve ordering** when adding a new repo; insert at the appropriate slot.

| Plugin | Repo | Notes |
|--------|------|-------|
| `-plugin.1.R7.API` | OSGi R7 API | read-only, POM-based |
| `-plugin.2.Enterprise.API` | Enterprise Java APIs | read-only |
| `-plugin.3.R7.Impl` | OSGi R7 Reference Impl | read-only |
| `-plugin.4.Test` | Testing Bundles | read-only |
| `-plugin.5.Debug` | Debug Bundles | read-only |
| `-plugin.6.Central` | Maven Central | read-only, indexed |
| `-plugin.7.Local` | `cnf/local/` | writable |
| `-plugin.8.Templates` | `cnf/templates/` | writable |
| `-plugin.9.Release` | `cnf/release/` | release target |

---

## .bndrun File Conventions

Platform launchers follow this naming pattern:

```
<bundle>/<name>_<os>.<ws>.<arch>.bndrun
```

- `_shared.bndrun` — cross-platform base: `runbundles`, `-runee`, common system packages
- Platform-specific files add `-runfw`, `-runproperties`, and `-runblacklist` entries for native fragments of other platforms
- JVM args (G1GC, string deduplication) are shared via `cnf/shared/`

**Never use Eclipse PDE launch configurations** — only `.bndrun` files for run/debug/export.

---

## OSGi Declarative Services Patterns

```java
// Immediate component
@Component(immediate = true, service = ImmediateService.class)
public class ImmediateService {
    @Activate
    void activate() { /* startup logic */ }

    @Deactivate
    void deactivate() { /* cleanup */ }
}

// Service consumer — always use explicit timeout
ServiceTracker<ImmediateService, ImmediateService> tracker =
    new ServiceTracker<>(bundleContext, ImmediateService.class, null);
tracker.open(true);
ImmediateService svc = tracker.waitForService(15_000L); // 15 s timeout
```

---

## Eclipse 4 RCP Patterns

### Application Model
- Declarative UI in `root/Application.e4xmi` (windows, parts, commands, menus, toolbars, key bindings)
- Handlers wired via `bundleclass://` URIs; never use direct instantiation

### Handler Template
```java
public class QuitHandler {
    @Execute
    public void execute(IWorkbench workbench) {
        workbench.close();
    }
}
```

### Part Template
```java
public class SamplePart {
    @Inject
    private EPartService partService;

    @PostConstruct
    public void createComposite(Composite parent) {
        // build SWT widget tree
    }

    @Focus
    public void onFocus() { }
}
```

### Lifecycle Hooks
```java
public class E4LifeCycle {
    @PostContextCreate
    public void postContextCreate(IEclipseContext context) { }

    @PreSave
    public void preSave(IEclipseContext context) { }

    @ProcessAdditions
    public void processAdditions(IEclipseContext context) { }

    @ProcessRemovals
    public void processRemovals(IEclipseContext context) { }
}
```

- **CSS theming**: `root/css/default.css`
- **Product definition**: `plugin.xml` extension point `org.eclipse.core.runtime.products`
- **Key bindings** defined in `.e4xmi` (Ctrl+Q quit, Ctrl+O open, Ctrl+S save, Ctrl+A about)

---

## Testing

### SWTBot UI Tests (`example.rcp.app.ui.swtbot.tests/`)

- Runner: `SWTBotJunit4ClassRunner` (JUnit 4)
- Tests execute in a **background thread** during OSGi runtime startup
- Configuration: 20-second startup timeout, 100 ms playback delay
- Declared in `swtbot_<platform>.bndrun` with `-tester: biz.aQute.junit`
- Currently only `swtbot_win32.win32.x86-64.bndrun` exists

Typical test assertions:
```java
bot.waitUntil(Conditions.shellIsActive("Eclipse 4 RCP Application"));
assertThat(bot.table().rowCount()).isEqualTo(5);
bot.menu("File").menu("Quit").click();
```

### OSGi Service Tests
```java
ServiceTracker<…> tracker = new ServiceTracker<>(context, Service.class, null);
tracker.open(true);
Service svc = tracker.waitForService(15_000L);
assertNotNull("Service must be available within timeout", svc);
```

---

## Platform Support

| Platform | OS/WS/Arch Token |
|----------|-----------------|
| Linux x86-64 | `linux.gtk.x86-64` |
| macOS Intel | `macosx.cocoa.x86-64` |
| macOS Apple Silicon | `macosx.cocoa.aarch64` |
| Windows x86-64 | `win32.win32.x86-64` |

When adding a new bundle with native dependencies, add a corresponding `-runblacklist` entry in each platform's `.bndrun` to exclude the other platforms' native fragments.

---

## CI/CD & Docker

### GitHub Actions (`.github/workflows/actions_build.yml`)

- Triggers: push to `feature/**` / `bugfix/**` branches; PRs targeting `main`
- Matrix: Linux, macOS, Windows Gradle exports
- Publishes Docker image to `klibio/example.bnd.rcp` via secrets `DOCKERHUB_USERNAME` / `DOCKERHUB_TOKEN`

### Docker (Multi-stage `Dockerfile`)

1. **Build stage** — runs Gradle exports for all three Linux application types
2. **easy-novnc stage** — Go server for browser-based VNC access
3. **Runtime stage** — Debian with Supervisor, OpenBox WM, TigerVNC, gosu; exposes port `5800`

---

## Key Invariants (Do Not Violate)

1. `-contract: *` is always active — every API used must appear in `buildpath`
2. `Bundle-SymbolicName` must use `${project.name}` macro, never a literal string
3. `singleton:=true` on every Eclipse bundle
4. No PDE target definitions (`.target` files) for runtime launches — use `.bndrun` exclusively
5. `cnf/` is the single workspace root; workspace-wide settings belong only there
6. Repository plugin numbering in `cnf/build.bnd` must remain in order
7. All platform-specific `.bndrun` files must include a `-runblacklist` for the opposing platform's native fragments
