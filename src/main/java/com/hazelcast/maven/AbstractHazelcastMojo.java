package com.hazelcast.maven;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

abstract class AbstractHazelcastMojo extends AbstractMojo {

    static final String AVRO_EXTENSION = ".avsc";

    static final String SCHEMA_REPLICATED_MAP = "hz:schemas";

    @Parameter
    String addresses;

    @Parameter
    String groupName;

    protected HazelcastInstance newClient() {
        return HazelcastClient.newHazelcastClient(createConfig());
    }

    protected ClientConfig createConfig() {
        ClientConfig config = new ClientConfig();
        if (addresses != null) {
            config.getNetworkConfig().addAddress(addresses);
        }
        if (groupName != null) {
            config.getGroupConfig().setName(groupName);
        }
        return config;
    }
}
