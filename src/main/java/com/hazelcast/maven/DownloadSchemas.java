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

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.matcher.WildcardConfigPatternMatcher;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "download")
public class DownloadSchemas extends AbstractHazelcastMojo {

    @Parameter(required = true)
    List<String> patterns = new ArrayList<>();

    @Parameter(required = true)
    File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        HazelcastInstance client = newClient();
        try {
            ReplicatedMap<String, String> map = client.getReplicatedMap(SCHEMA_REPLICATED_MAP);

            WildcardConfigPatternMatcher matcher = new WildcardConfigPatternMatcher();
            Set<Entry<String, String>> schemas = map.entrySet()
                                                    .stream()
                                                    .filter(e -> patterns.stream().anyMatch(p -> matcher.matches(p, e.getKey())))
                                                    .collect(Collectors.toSet());

            if (!outputDirectory.isDirectory()) {
                if (!outputDirectory.mkdirs()) {
                    throw new IllegalStateException("Could not create directory " + outputDirectory);
                }
            }

            for (Entry<String, String> schema : schemas) {
                getLog().info("Downloading schema " + schema.getKey());
                String name = schema.getKey();
                String text = schema.getValue();
                File f = new File(outputDirectory, name + AVRO_EXTENSION);
                try (OutputStreamWriter o = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
                    o.write(text);
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to write file " + f);
                }
            }
        } finally {
            client.shutdown();
        }
    }

}
