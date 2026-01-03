package org.cqupt.crypto.hcaa.chainmaker.execution;

import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.NodeConfig;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.utils.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InitClient {

    static String SDK_CONFIG = "chainmaker.yml";

    public static ChainClient chainClient;
    static ChainManager chainManager;
    static User user;

    public static void initChainClientForPk() throws Exception {
        //通过配置文件创建
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(representer);
        InputStream in = InitClient.class.getClassLoader().getResourceAsStream(SDK_CONFIG);

        SdkConfig sdkConfig;
        sdkConfig = yaml.loadAs(in, SdkConfig.class);
        assert in != null;
        in.close();

        for (NodeConfig nodeConfig : sdkConfig.getChainClient().getNodes()) {
            List<byte[]> tlsCaCertList = new ArrayList<>();
            if (nodeConfig.getTrustRootPaths() != null) {
                for (String rootPath : nodeConfig.getTrustRootPaths()) {
                    List<String> filePathList = FileUtils.getFilesByPath(rootPath);
                    for (String filePath : filePathList) {
                        tlsCaCertList.add(FileUtils.getFileBytes(filePath));
                    }
                }
            }
            byte[][] tlsCaCerts = new byte[tlsCaCertList.size()][];
            tlsCaCertList.toArray(tlsCaCerts);
            nodeConfig.setTrustRootBytes(tlsCaCerts);
        }

        chainManager = ChainManager.getInstance();
        chainClient = chainManager.getChainClient(sdkConfig.getChainClient().getChainId());

        if (chainClient == null) {
            chainClient = chainManager.createChainClient(sdkConfig);
        }

        //公钥模式下，多签用户
//        CryptoSuite generatedCryptoSuite = ChainmakerX509CryptoSuite.newInstance(false);
//        adminUser1 = new User(ORG_ID1);
//        adminUser1.setAuthType(AuthType.Public.getMsg());
//        adminUser1.setPriBytes(FileUtils.getResourceFileBytes(ADMIN1_PK_PATH));
//        adminUser1.setTlsPrivateKey(CryptoUtils.getPrivateKeyFromBytes(FileUtils.getResourceFileBytes(ADMIN1_PK_TLS_PRI_KEY_PATH)));
//        adminUser1.setTlsCertificate(generatedCryptoSuite.getCertificateFromBytes(FileUtils.getResourceFileBytes(ADMIN1_PK_TLS_CERT_PATH)));
        System.out.println("init ChainMaker client success.");
    }

}
