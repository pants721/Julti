package xyz.duncanruns.julti.util;

import org.apache.logging.log4j.Level;
import xyz.duncanruns.julti.JultiOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static xyz.duncanruns.julti.Julti.log;

public final class ResourceUtil {
    private ResourceUtil() {

    }

    public static BufferedImage getImageResource(String name) throws IOException {
        return ImageIO.read(getResource(name));
    }

    public static URL getResource(String name) {
        return ResourceUtil.class.getResource(name);
    }

    public static void copyResourceToFile(String resourceName, Path destination) throws IOException {
        // Answer to https://stackoverflow.com/questions/10308221/how-to-copy-file-inside-jar-to-outside-the-jar
        InputStream inStream = getResourceAsStream(resourceName);
        OutputStream outStream = Files.newOutputStream(destination);
        int readBytes;
        byte[] buffer = new byte[4096];
        while ((readBytes = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, readBytes);
        }
        inStream.close();
        outStream.close();
    }

    public static InputStream getResourceAsStream(String name) {
        return ResourceUtil.class.getResourceAsStream(name);
    }

    public static void makeResources() {
        JultiOptions.ensureJultiDir();
        JultiOptions.getJultiDir().resolve("sounds").toFile().mkdirs();

        String[] filesToCopy = {
                "dirtcover.png",
                "lock.png",
                "blacksmith_example.png",
                "beach_example.png",
                "sounds/click.wav",
                "sounds/plop.wav"
        };

        for (String name : filesToCopy) {
            try {
                Path dest = JultiOptions.getJultiDir().resolve(name);
                if (dest.toFile().exists()) {
                    continue;
                }
                ResourceUtil.copyResourceToFile("/" + name, dest);
                log(Level.INFO, "Generated .Julti file " + name);
            } catch (Exception e) {
                log(Level.ERROR, "Failed to copy resource (" + e.getClass().getSimpleName() + "):\n" + e);
            }
        }

        Path[] scriptLocations = {
                JultiOptions.getJultiDir().resolve("julti-obs-link.lua"),
                Paths.get(System.getProperty("user.home")).resolve("Documents").resolve("julti-obs-link.lua")
        };

        for (Path dest : scriptLocations) {
            try {
                String name = "julti-obs-link.lua";
                ResourceUtil.copyResourceToFile("/" + name, dest);
                log(Level.INFO, "Generated " + name + " file in " + dest.getName(dest.getNameCount() - 2));
            } catch (Exception e) {
                log(Level.ERROR, "Failed to copy resource (" + e.getClass().getSimpleName() + "):\n" + e);
            }
        }
    }

    public static List<String> getResourcesFromFolder(String folder)
            throws URISyntaxException, IOException {

        List<String> result;

        try {
            result = getResourcesFromFolderJAR(folder);
        } catch (Exception ignored) {
            result = getResourcesFromFolderDev(folder);
        }

        return result;

    }

    private static List<String> getResourcesFromFolderDev(String folder) {
        if (!folder.startsWith("/")) {
            folder = "/" + folder;
        }
        return Arrays.stream(new File(ResourceUtil.class.getResource(folder).getPath()).list()).collect(Collectors.toList());
    }

    private static List<String> getResourcesFromFolderJAR(String folder) throws URISyntaxException, IOException {
        List<String> result;
        // get path of the current running JAR
        String jarPath = ResourceUtil.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();

        // Encode the jarPath to handle spaces
        jarPath = new File(jarPath).toURI().toString();

        // file walks JAR
        URI uri = URI.create("jar:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            result = Files.walk(fs.getPath(folder))
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }
        return result;
    }
}
