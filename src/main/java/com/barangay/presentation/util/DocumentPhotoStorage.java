package com.barangay.presentation.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Utility class for persisting and retrieving issued document photos.
 */
public final class DocumentPhotoStorage {

    private DocumentPhotoStorage() {
    }

    /**
     * Returns the absolute path of the provided photo file so it can be stored with the document.
     */
    public static String savePhoto(File sourceFile) throws IOException {
        if (sourceFile == null) {
            return null;
        }
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Selected photo no longer exists");
        }
        return sourceFile.getAbsolutePath();
    }

    /**
     * Returns true if the stored photo path resolves to an existing file.
     */
    public static boolean photoExists(String storedPath) {
        Path path = resolvePath(storedPath);
        return path != null && Files.exists(path);
    }

    /**
     * Copies the stored photo to the destination path, creating parent directories if needed.
     */
    public static void copyPhotoTo(String storedPath, Path destination) throws IOException {
        Path source = resolvePath(storedPath);
        if (source == null || !Files.exists(source)) {
            throw new FileNotFoundException("Document photo not found");
        }
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Resolves the stored path to a File instance, or null if it cannot be resolved.
     */
    public static File resolvePhoto(String storedPath) {
        Path path = resolvePath(storedPath);
        return path != null ? path.toFile() : null;
    }

    public static Optional<String> getExtension(String fileName) {
        if (fileName == null) {
            return Optional.empty();
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(fileName.substring(idx));
    }

    private static Path resolvePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        return Paths.get(storedPath);
    }
}
