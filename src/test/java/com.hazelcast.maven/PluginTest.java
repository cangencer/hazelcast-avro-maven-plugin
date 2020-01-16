package com.hazelcast.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class PluginTest {

    @Test
    @Ignore
    public void testUpload() throws MojoExecutionException {
        UploadSchemas uploadSchemas = new UploadSchemas();
        uploadSchemas.inputDirectory = new File("src/test/avro");

        uploadSchemas.execute();
    }

    @Test
    @Ignore
    public void testDownload() throws MojoExecutionException {
        DownloadSchemas downloadSchemas = new DownloadSchemas();

        downloadSchemas.outputDirectory = new File("src/test/avro");
        downloadSchemas.patterns.add("user_*");

        downloadSchemas.execute();
    }
}
