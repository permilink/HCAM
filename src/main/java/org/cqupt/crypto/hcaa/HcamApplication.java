package org.cqupt.crypto.hcaa;

import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.util.AdapterConfig;
import org.cqupt.crypto.hcaa.util.ReadAdapterConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Map;

@SpringBootApplication
public class HcamApplication {
    @Autowired
    private Map<String, BlockchainClient> blockchainClients; // Spring 自动注入所有实现

    private static BlockchainClient currentClient;
    public static Map<String, AdapterConfig.Operation> fun_operation;

    private static Logger logger = LoggerFactory.getLogger(HcamApplication.class);



    @PostConstruct
    public void init() throws Exception {
        ReadAdapterConfigUtil readAdapterConfigUtil = new ReadAdapterConfigUtil();
        AdapterConfig adapterConfig = readAdapterConfigUtil.readAdapterConfig();
        if (adapterConfig != null) {
            String blockchainType = adapterConfig.getBlockchainType();
            currentClient = blockchainClients.get(blockchainType);
            if (currentClient == null && !blockchainType.equals("Chainmaker") && !blockchainType.equals("HyperLedger-Fabric")
                    && !blockchainType.equals("Ethereum") && !blockchainType.equals("FISCO BCOS") && !blockchainType.equals("Hyperchain")) {
                logger.error("Unsupported blockchain type: " + blockchainType);
                return;
            }
            currentClient.init(); // ✅ 初始化客户端
        } else {
            logger.error("server initialization failed");
        }
        fun_operation = adapterConfig.getOperations();
    }


    public static void main(String[] args) {
        SpringApplication.run(HcamApplication.class, args);
    }


    public BlockchainClient getCurrentClient() {
        return currentClient;
    }

    public static Map<String, AdapterConfig.Operation> getFun_operation() {
        return fun_operation;
    }


}
