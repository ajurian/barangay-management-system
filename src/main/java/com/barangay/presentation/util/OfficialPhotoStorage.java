package com.barangay.presentation.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Utility helper for storing and retrieving barangay official photos.
 */
public final class OfficialPhotoStorage {

    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg");
    private static final Path STORAGE_DIRECTORY = Paths.get("assets", "profiles", "officials");

    private OfficialPhotoStorage() {
    }

    /**
     * Saves the given photo under the official's identifier, replacing any existing file.
     *
     * @param officialId official identifier used as file name stem
     * @param sourceFile chosen photo file
     * @throws IOException when the file cannot be copied
     */
    public static void savePhoto(String officialId, File sourceFile) throws IOException {
        if (officialId == null || officialId.isBlank()) {
            throw new IllegalArgumentException("Official ID is required");
        }
        if (sourceFile == null || !sourceFile.exists()) {
            throw new FileNotFoundException("Selected photo could not be found");
        }
        Files.createDirectories(STORAGE_DIRECTORY);
        String extension = getExtension(sourceFile.getName()).orElse(".png");
        Path destination = STORAGE_DIRECTORY.resolve(officialId + extension.toLowerCase(Locale.ENGLISH));
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Returns true if a stored photo exists for the given official ID.
     */
    public static boolean photoExists(String officialId) {
        return resolvePhoto(officialId).isPresent();
    }

    /**
     * Resolves the stored photo file for the official, if available.
     */
    public static Optional<File> resolvePhoto(String officialId) {
        if (officialId == null || officialId.isBlank()) {
            return Optional.empty();
        }
        for (String extension : SUPPORTED_EXTENSIONS) {
            Path candidate = STORAGE_DIRECTORY.resolve(officialId + extension);
            if (Files.exists(candidate)) {
                return Optional.of(candidate.toFile());
            }
        }
        return Optional.empty();
    }

    private static Optional<String> getExtension(String filename) {
        if (filename == null) {
            return Optional.empty();
        }
        int idx = filename.lastIndexOf('.') + 1;
        if (idx <= 0 || idx >= filename.length()) {
            return Optional.empty();
        }
        return Optional.of('.' + filename.substring(idx).toLowerCase(Locale.ENGLISH));
    }
}
