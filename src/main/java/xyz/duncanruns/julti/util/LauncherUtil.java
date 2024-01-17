package xyz.duncanruns.julti.util;

import org.apache.logging.log4j.Level;
import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.JultiOptions;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class LauncherUtil {
    private LauncherUtil() {
    } 

    public static void launchPrograms() {
        JultiOptions options = JultiOptions.getJultiOptions();
        List<Path> paths = options.launchingProgramPaths;
        if (!Desktop.isDesktopSupported()) {
            Julti.log(Level.ERROR, "Could not launch programs: Java Desktop not supported");
            return;
        }
        for (Path path : paths) {
            Desktop desktop = Desktop.getDesktop();
            try {
                // FIXME: Launching programs like this wont launch them from their directory
                // OBS won't launch properly bcs of this it will do like can't find en_us
                desktop.open(path.toFile());
                Julti.log(Level.INFO, "Launched \"" + path.toString() + "\"");
            } catch (IllegalArgumentException | IOException e) {
                Julti.log(Level.ERROR, "Could not launch \"" + path + "\":\n" + e.toString());
            }
        }
    }
}
