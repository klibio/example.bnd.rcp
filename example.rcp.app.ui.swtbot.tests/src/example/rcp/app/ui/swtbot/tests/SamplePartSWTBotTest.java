package example.rcp.app.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * SWTBot test case for the example.rcp.app.ui application.
 *
 * <p>Verifies that:
 * <ol>
 *   <li>The main application window ("Eclipse 4 RCP Application") is visible.</li>
 *   <li>The "Sample Part" view is present and shows exactly 5 sample items.</li>
 *   <li>Choosing File → Quit (and confirming) shuts the application down cleanly
 *       without any platform-level errors.</li>
 * </ol>
 *
 * <p>To run this test from bndtools IDE:
 * <ol>
 *   <li>Open {@code swtbot_win32.win32.x86-64.bndrun} and click <em>Resolve</em>
 *       to populate the SWTBot run-bundles.</li>
 *   <li>Right-click the bndrun file → <em>Debug As → Bnd OSGi Test Launchers</em>.</li>
 * </ol>
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class SamplePartSWTBotTest {

    private static final String MAIN_WINDOW_TITLE   = "Eclipse 4 RCP Application";
    private static final String SAMPLE_PART_TITLE   = "Sample Part";
    private static final String QUIT_DIALOG_TITLE   = "Confirmation";
    // 60 s: a cold Eclipse 4 start from a fresh Equinox config (osgi.clean=true,
    // new storage dir) takes significantly longer than a warm IDE re-launch.
    private static final long   TIMEOUT_MS          = 60_000;

    private static SWTBot                 bot;
    private static List<IStatus>          platformErrors;
    private static ILogListener           logListener;

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        SWTBotPreferences.TIMEOUT        = TIMEOUT_MS;
        SWTBotPreferences.PLAYBACK_DELAY = 100;

        Bundle self = FrameworkUtil.getBundle(SamplePartSWTBotTest.class);

        // Derive the project directory from the bundle jar location
        // (<project>/generated/<name>.jar → two parent levels up).
        // Fall back to a sub-folder of java.io.tmpdir which is always writable
        // and survives bnd's post-test cleanup of the launch storage dir.
        File screenshotsDir;
        try {
            String loc = self.getLocation().replaceFirst("^(?:initial@)?reference:", "");
            // On Windows the URI may look like "file:/C:/path/..." (single slash);
            // normalize to three slashes so new URI() + new File() parse correctly.
            if (loc.matches("file:/[^/].*")) {
                loc = "file://" + loc.substring("file:".length());
            }
            File jarFile = new File(new URI(loc));
            screenshotsDir = new File(jarFile.getParentFile().getParentFile(), "screenshots");
        } catch (Exception ex) {
            screenshotsDir = new File(System.getProperty("java.io.tmpdir"),
                    "swtbot-screenshots");
        }
        screenshotsDir.mkdirs();
        SWTBotPreferences.SCREENSHOTS_DIR = screenshotsDir.getAbsolutePath();
        System.out.println("[SWTBot] screenshots dir: " + screenshotsDir.getAbsolutePath());

        // When running via bnd CLI with aQute.junit.separatethread=true, the
        // test thread starts in BundleActivator.start() BEFORE the bnd launcher
        // has finished starting all bundles and published launcher.ready=true.
        // bndtools.runtime.applaunch.eclipse4 tracks that service: it only
        // registers the ApplicationLauncher OSGi service (which triggers the
        // Eclipse 4 app and SWT Display creation) AFTER launcher.ready fires.
        // Polling Display.getDefault() alone is insufficient because it is null
        // until after ApplicationLauncher.launch() hands the main thread to the
        // E4 app.  Wait for the ApplicationLauncher service first so we are sure
        // the E4 startup sequence has actually been triggered.
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;

        // 1. Probe for ApplicationLauncher service (10 s max).
        //    In bnd CLI ("bnd test"), bndtools.runtime.applaunch.eclipse4 registers
        //    this AFTER launcher.ready=true fires — proves E4 has the main thread.
        //    In Eclipse IDE's bndtools launcher, aQute.launcher.Launcher may not
        //    be registered at all, so this service never appears; proceed anyway
        //    and let the Display wait below do the sequencing.
        ServiceReference<ApplicationLauncher> appLauncherRef = null;
        long probeDeadline = Math.min(deadline, System.currentTimeMillis() + 10_000);
        while (appLauncherRef == null && System.currentTimeMillis() < probeDeadline) {
            appLauncherRef = self.getBundleContext()
                                 .getServiceReference(ApplicationLauncher.class);
            if (appLauncherRef == null) Thread.sleep(200);
        }
        System.out.println("[SWTBot] ApplicationLauncher "
            + (appLauncherRef != null ? "found" : "not found (IDE mode — Display wait follows)"));

        // 2. Wait for the SWT Display — created by the E4 app on the main thread.
        //    Works in both bnd CLI and Eclipse IDE.
        while (Display.getDefault() == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(200);
        }
        if (Display.getDefault() == null) {
            throw new IllegalStateException(
                "SWT Display not created within " + TIMEOUT_MS + " ms. "
                + "Check that bndtools.runtime.applaunch.eclipse4 is in -runbundles.");
        }

        platformErrors = Collections.synchronizedList(new ArrayList<>());
        logListener = (status, plugin) -> {
            if (status.getSeverity() >= IStatus.ERROR) {
                platformErrors.add(status);
            }
        };
        Platform.addLogListener(logListener);

        bot = new SWTBot();
    }

    @AfterClass
    public static void afterClass() {
        if (logListener != null && Platform.isRunning()) {
            Platform.removeLogListener(logListener);
        }
    }

    /**
     * Single test that covers the full lifecycle:
     * open → verify items → quit → verify clean shutdown.
     */
    @Test
    public void testSamplePartShowsFiveItemsThenQuitCleanly() throws Exception {

        // ── Step 1: Wait for and activate the main application window ─────────
        // Use shell existence (not shellIsActive) so the test passes even when
        // the Eclipse window opens behind another window and never receives focus.
        // shellIsActive(title) checks Display.getActiveShell() — it fails when
        // the window exists but is not the OS-focused window.
        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() {
                for (SWTBotShell s : bot.shells()) {
                    if (MAIN_WINDOW_TITLE.equals(s.getText())) return true;
                }
                return false;
            }
            @Override
            public String getFailureMessage() {
                StringBuilder sb = new StringBuilder(
                    "Shell '" + MAIN_WINDOW_TITLE + "' not found. Available shells: [");
                for (SWTBotShell s : bot.shells()) sb.append('"').append(s.getText()).append("\", ");
                sb.append("]");
                return sb.toString();
            }
        }, TIMEOUT_MS);
        SWTBotShell mainShell = bot.shell(MAIN_WINDOW_TITLE);
        mainShell.activate();
        SWTUtils.captureScreenshot(SWTBotPreferences.SCREENSHOTS_DIR + File.separator + "01_main_window_open.png");

        // ── Step 2: Verify the "Sample Part" tab is visible ───────────────────
        bot.cTabItem(SAMPLE_PART_TITLE).activate();

        // ── Step 3: Verify the table contains exactly 5 items ─────────────────
        SWTBotTable table = bot.table(0);
        assertEquals("Sample Part table must contain exactly 5 items",
                5, table.rowCount());
        assertEquals("Sample item 1", table.getTableItem(0).getText());
        assertEquals("Sample item 2", table.getTableItem(1).getText());
        assertEquals("Sample item 3", table.getTableItem(2).getText());
        assertEquals("Sample item 4", table.getTableItem(3).getText());
        assertEquals("Sample item 5", table.getTableItem(4).getText());
        SWTUtils.captureScreenshot(SWTBotPreferences.SCREENSHOTS_DIR + File.separator + "02_table_verified.png");

        // ── Step 4: Invoke File → Quit and confirm ───────────────────────────
        // QuitHandler.execute() calls MessageDialog.openConfirm() which enters a
        // nested SWT event loop on the main thread.  If we wait for the dialog
        // from the background test thread and then call bot.button("OK").click(),
        // the click's syncExec can race with the nested-loop event processing and
        // arrive AFTER the Display has already been disposed (SWTException).
        //
        // Fix: start a dedicated watchdog thread that uses display.syncExec to
        // poll for the confirmation dialog and click OK from the UI thread side.
        // This completely avoids the background-thread race.
        final Display display = Display.getDefault();
        final Thread quitWatchdog = new Thread(() -> {
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;
            while (System.currentTimeMillis() < deadline) {
                if (display.isDisposed()) break;
                try { Thread.sleep(100); } catch (InterruptedException ex) { break; }
                if (display.isDisposed()) break;
                final boolean[] clicked = { false };
                display.syncExec(() -> {
                    if (display.isDisposed()) return;
                    for (Shell s : display.getShells()) {
                        if (QUIT_DIALOG_TITLE.equals(s.getText())) {
                            clicked[0] = clickButtonInShell(s, "OK");
                            return;
                        }
                    }
                });
                if (clicked[0]) break;
            }
        }, "quit-dialog-watchdog");
        quitWatchdog.setDaemon(true);
        quitWatchdog.start();
        SWTUtils.captureScreenshot(SWTBotPreferences.SCREENSHOTS_DIR + File.separator + "03_before_quit.png");

        bot.menu("File").menu("Quit").click();
        quitWatchdog.join(TIMEOUT_MS);

        // ── Step 5: Wait for the main window to close ─────────────────────────
        // Use a simple Thread.sleep loop (not bot.waitUntil) because the Display
        // may already be disposed by this point; widget.isDisposed() is always
        // safe to call from any thread.
        long shutdownDeadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!mainShell.widget.isDisposed() && System.currentTimeMillis() < shutdownDeadline) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // biz.aQute.junit interrupts the test thread during framework
                // shutdown — which only happens after the E4 app has exited.
                // Restore the flag and break; the assertTrue below will confirm
                // whether the window was actually disposed.
                Thread.currentThread().interrupt();
                break;
            }
        }
        assertTrue("Main application window did not close after confirming Quit",
                mainShell.widget.isDisposed());

        // ── Step 6: Assert no platform-level errors were logged ───────────────
        assertTrue(
                "Platform errors were recorded during the test session: " + platformErrors,
                platformErrors.isEmpty());
    }

    /**
     * Recursively searches {@code shell} for a visible, enabled {@link Button}
     * whose text matches {@code label} (after stripping SWT mnemonic '&' chars)
     * and fires a {@code SWT.Selection} event on it.
     *
     * <p>Must be called from the SWT UI thread (e.g., inside a syncExec).
     *
     * @return {@code true} if a matching button was found and clicked
     */
    private static boolean clickButtonInShell(Shell shell, String label) {
        return walkAndClick(shell, label);
    }

    private static boolean walkAndClick(Control c, String label) {
        if (c instanceof Button) {
            String text = ((Button) c).getText().replace("&", "");
            if (label.equals(text) && c.isVisible() && c.isEnabled()) {
                Event e = new Event();
                e.widget = c;
                c.notifyListeners(SWT.Selection, e);
                return true;
            }
        }
        if (c instanceof Composite) {
            for (Control child : ((Composite) c).getChildren()) {
                if (walkAndClick(child, label)) return true;
            }
        }
        return false;
    }
}
