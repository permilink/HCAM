package org.cqupt.crypto.hcaa.bcos;

import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;

public class BcosConfig {
    // 获取配置文件路径
    public final String configFile = "bcos.toml";
    public BcosSDK readConfig() {
        // 这里可以添加加载配置文件的逻辑
        BcosSDK bcosSDK = BcosSDK.build(configFile);
        System.out.println("Loading configuration from " + configFile);
        return bcosSDK;
    }
}
