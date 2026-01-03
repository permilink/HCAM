package org.cqupt.crypto.hcaa.util;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadAdapterConfigUtil {
    private static final String filePath = "adapter.yaml";
    public AdapterConfig readAdapterConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ReadAdapterConfigUtil.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            return yaml.loadAs(inputStream, AdapterConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
