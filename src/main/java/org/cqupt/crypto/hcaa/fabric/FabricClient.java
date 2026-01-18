package org.cqupt.crypto.hcaa.fabric;

import org.cqupt.crypto.hcaa.HcamApplication;
import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.fabric.execution.Fabrics;
import org.cqupt.crypto.hcaa.util.AdapterConfig;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Network;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Component("Hyperledger-Fabric")
public class FabricClient implements BlockchainClient {

    private Gateway gateway;
    private String channelName;
    private String contractName; // 当前业务合约名
    private static Map<String, AdapterConfig.Operation> fun_operation;

    @Override
    public void init() throws Exception {
        Properties config = loadConfig("fabric.config");
        this.channelName = config.getProperty("channel_name");
        String userCertPath = config.getProperty("user_cert_path");
        System.out.println("✅ user_cert_path: " + userCertPath);
        String userKeyPath = config.getProperty("user_privatekey_path");
        System.out.println("✅ user_key_path: " + userKeyPath);
        String tlsCertPath = config.getProperty("peer_tls_cert_path");
        System.out.println("✅ peer_tls_cert_path: " + tlsCertPath);
        this.gateway = Fabrics.fabricGateway(tlsCertPath,userCertPath,userKeyPath, config.getProperty("msp_id"),config.getProperty("peer_endpoint_address"));
        fun_operation = HcamApplication.getFun_operation();
    }

    // ========== 合约管理方法 ==========

    /**
     * 部署合约（安装 + 批准 + 提交）
     */
    @Override
    public void createContract(String contractName, String chaincodePath) throws Exception {
        Network network = gateway.getNetwork(channelName);
        Contract lifecycle = network.getContract("_lifecycle");
        // 1. 打包链码为 .tar.gz（需外部生成或模拟）
        byte[] packageBytes = Files.readAllBytes(new File(chaincodePath).toPath());
        String encodedPackage = Base64.getEncoder().encodeToString(packageBytes);

        // 2. 安装链码
        lifecycle.submitTransaction("InstallChaincode", encodedPackage);

        // 3. 查询已安装的包 ID
        byte[] queryResult = lifecycle.evaluateTransaction("QueryInstalledChaincodes");
        String packageId = extractPackageId(queryResult); // 解析 JSON 获取 package_id

        // 4. 批准链码定义（单组织测试环境）
        lifecycle.submitTransaction("ApproveChaincodeDefinitionForMyOrg",
                contractName,
                "1.0", // version
                packageId,
                "1",   // sequence
                "true", // init required?
                "",   // collection config
                ""    // endorsement policy
        );

        // 5. 提交链码定义
        lifecycle.submitTransaction("CommitChaincodeDefinition",
                contractName,
                "1.0",
                "1",
                "true",
                "",
                ""
        );

        System.out.println("✅ Contract '" + contractName + "' deployed.");
    }

    /**
     * 删除合约（Fabric 不支持真正删除，仅逻辑弃用）
     */
    @Override
    public void deleteContract(String contractName) {
        throw new UnsupportedOperationException(
                "Fabric does not support deleting chaincode. Use upgrade or deprecate instead."
        );
    }

    /**
     * 更新合约（升级到新版本）
     */
    @Override
    public void updateContract(String newContractName, String newChaincodePath) throws Exception {
        Network network = gateway.getNetwork(channelName);
        Contract lifecycle = network.getContract("_lifecycle");

        // 1. 安装新包
        byte[] newPackageBytes = Files.readAllBytes(new File(newChaincodePath).toPath());
        String encodedNewPackage = Base64.getEncoder().encodeToString(newPackageBytes);
        lifecycle.submitTransaction("InstallChaincode", encodedNewPackage);

        // 2. 查询新 packageId
        byte[] result = lifecycle.evaluateTransaction("QueryInstalledChaincodes");
        String newPackageId = extractPackageId(result);

        // 3. 批准新版本（sequence+1）
        lifecycle.submitTransaction("ApproveChaincodeDefinitionForMyOrg",
                newContractName,
                "2.0",
                newPackageId,
                "2", // sequence
                "false",
                "",
                ""
        );

        // 4. 提交
        lifecycle.submitTransaction("CommitChaincodeDefinition",
                newContractName,
                "2.0",
                "2",
                "false",
                "",
                ""
        );

        System.out.println("✅ Contract upgraded to '" + newContractName + "'.");
    }

    /**
     * 查询已提交的合约名称列表
     */
    @Override
    public List<String> queryContract(String contractName) throws Exception {
        Network network = gateway.getNetwork(channelName);
        Contract lifecycle = network.getContract(contractName);

        byte[] result = lifecycle.evaluateTransaction("QueryChaincodeDefinitions");
        String json = new String(result, StandardCharsets.UTF_8);

        // 示例解析（实际应使用 Jackson）
        List<String> names = new ArrayList<>();
        if (json.contains("digital_asset_contract_v2")) {
            names.add("digital_asset_contract_v2");
        }
        return names;
    }

    // ========== 辅助方法 ==========

    private String extractPackageId(byte[] response) {
        // 示例：解析 {"installed_chaincodes":[{"package_id":"mycc_1.0:abc..."}]}
        String json = new String(response, StandardCharsets.UTF_8);
        // 使用正则或 Jackson 解析
        return "mycc_1.0:" + java.util.UUID.randomUUID().toString(); // 模拟返回
    }

    // ========== 业务方法（如资产操作） ==========

    @Override
    public byte[] createAsset(String contractName,String assetId) throws Exception {
        Contract contract = gateway.getNetwork(channelName).getContract(contractName);
        byte[] createAssets = contract.submitTransaction(fun_operation.get("create").getFunctionName(), assetId, "0");
        return createAssets;
    }

    @Override
    public byte[] deleteAsset(String contractName,String assetId) throws Exception {
        Contract contract = gateway.getNetwork(channelName).getContract(contractName);
        byte[] deleteAssets = contract.submitTransaction(fun_operation.get("delete").getFunctionName(), assetId);
        return deleteAssets;
    }

    @Override
    public byte[] updateAsset(String contractName,String assetId, Integer amount) throws Exception {
        Contract contract = gateway.getNetwork(channelName).getContract(contractName);
        byte[] updateAssets = contract.submitTransaction(fun_operation.get("update").getFunctionName(), assetId, amount.toString());
        return updateAssets;
    }

    @Override
    public byte[] queryAsset(String contractName,String assetId) throws GatewayException {
        Contract contract = gateway.getNetwork(channelName).getContract(contractName);
        byte[] result = contract.evaluateTransaction(fun_operation.get("query").getFunctionName(), assetId);
        return result;
    }

    @Override
    public byte[] getChainHeight() throws Exception {
        //unsupported in fabric sdk
        return new byte[0];
    }

    // ========== 加载配置文件 ==========

    private Properties loadConfig(String configFilePath) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(configFilePath);
        File configFile = resource.getFile();
        Properties properties = new Properties();
        if (configFile.exists()) {
            properties.load(Files.newBufferedReader(configFile.toPath()));
        } else {
            System.err.println("Configuration file not found: " + configFile.getAbsolutePath());
        }
        return properties;
    }



    @Override
    public void close() throws Exception {

    }

}
