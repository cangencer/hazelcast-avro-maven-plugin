/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.maven;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Mojo(name = "upload")
public class UploadSchemas extends AbstractHazelcastMojo {

    @Parameter
    String addresses;

    @Parameter
    String groupName;

    @Parameter(required = true, property = "inputDirectory")
    File inputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        HazelcastInstance client = this.newClient();
        try {
            ReplicatedMap<String, String> map = client.getReplicatedMap(SCHEMA_REPLICATED_MAP);

            if (!inputDirectory.isDirectory()) {
                throw new MojoExecutionException(inputDirectory + " is not a valid directory");
            }
            try {
                getLog().info("Uploading schemas");
                Files.walk(inputDirectory.toPath()).forEach(p -> {
                    getLog().info(p.toString());
                    String name = p.getFileName().toString();
                    if (name.endsWith(AVRO_EXTENSION)) {
                        String key = name.substring(0, name.length() - AVRO_EXTENSION.length());
                        try {
                            getLog().info("Uploading schema file " + p);
                            String value = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                            map.put(key, value);
                        } catch (IOException e) {
                            throw new RuntimeException("Error reading " + p);
                        }
                    }
                });
            } catch (IOException e) {
                throw new MojoExecutionException(inputDirectory + " is not a valid directory");
            }
        } finally {
            client.shutdown();
        }
    }
}
