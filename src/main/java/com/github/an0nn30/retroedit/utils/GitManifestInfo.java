package com.github.an0nn30.retroedit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class GitManifestInfo {

    private static Attributes getManifestAttributes() {
        try (InputStream inputStream = GitManifestInfo.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);
                return manifest.getMainAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getGitBranch() {
        Attributes attributes = getManifestAttributes();
        return attributes != null ? attributes.getValue("Git-Branch") : "unknown";
    }

    public static String getGitCommit() {
        Attributes attributes = getManifestAttributes();
        return attributes != null ? attributes.getValue("Git-Commit") : "unknown";
    }
}