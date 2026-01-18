package org.cqupt.crypto.hcaa.bcos;

import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.cqupt.crypto.hcaa.HcamApplication;
import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.util.AdapterConfig;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.response.Code;
import org.fisco.bcos.sdk.v3.client.protocol.response.ConsensusStatus;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BcosClient implements BlockchainClient {
    private Client client;
    private CryptoKeyPair keyPair;
    private static Map<String, AdapterConfig.Operation> bcosOperation;
    private AssembleTransactionProcessor assembleTransactionProcessor;
    private String abi;

    @Override
    public void init() throws Exception {
        bcosOperation = HcamApplication.getFun_operation();
        // 1. 初始化 SDK
        BcosSDK bcosSDK = new BcosConfig().readConfig();
        if (bcosSDK == null) {
            throw new IllegalStateException("BcosSDK is null after readConfig()");
        }

        // 2. 获取 client
        client = bcosSDK.getClient();
        if (client == null) {
            throw new IllegalStateException("Client is null from BcosSDK");
        }

        // 3. 获取 CryptoSuite 和 KeyPair
        CryptoSuite cryptoSuite = client.getCryptoSuite();
        if (cryptoSuite == null) {
            throw new IllegalStateException("CryptoSuite is null from client");
        }

        keyPair = cryptoSuite.getCryptoKeyPair();
        if (keyPair == null) {
            throw new IllegalStateException("CryptoKeyPair is null from CryptoSuite");
        }
        assembleTransactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair, "src/main/resources/bcos/abi/", "src/main/resources/bcos/bin/");
        try (Stream<Path> paths = Files.list(Paths.get("src/main/resources/bcos/abi/"))) {
            abi = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(file -> file.endsWith(".abi"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No ABI file found in the directory"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ABI directory", e);
        }
    }

    @Override
    public void createContract(String contractName, String contractCode) throws Exception {
        AssembleTransactionProcessor assembleTransactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair, "src/main/resources/bcos/abi/", "src/main/resources/bcos/bin/");
        //生成随机数
        String randomStr = Long.toString(System.currentTimeMillis());
        TransactionResponse transactionResponse = assembleTransactionProcessor.deployByContractLoader(contractName+randomStr, new ArrayList<>());
        if (transactionResponse.getReturnCode()==0){
            System.out.println("Contract deployed successfully. Address: " + transactionResponse.getContractAddress());
        }else{
            System.out.println("Contract deployment failed. Error: " + transactionResponse.getReturnMessage());
        }
    }

    @Override
    public void deleteContract(String contractName) throws ChainMakerCryptoSuiteException, Exception {
        //BCOS does not support delete contract
    }

    @Override
    public void updateContract(String contractName, String newContractCode) throws Exception {
        //BCOS does not support update contract by code
    }

    @Override
    public List<String> queryContract(String contractName) throws Exception {
        String chainId = client.getChainId();
        String group = client.getGroup();
        EnumNodeVersion.Version chainCompatibilityVersion = client.getChainCompatibilityVersion();
        System.out.println("Connected to BCOS chain. Chain ID: " + chainId + ", Group: " + group);
        System.out.println("Contract version: " + chainCompatibilityVersion.toString());
        return List.of();
    }

    @Override
    public byte[] createAsset(String contract, String assetId) throws Exception {
        // 创建非国密类型的CryptoSuite
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        // 随机生成非国密公私钥对
        CryptoKeyPair cryptoKeyPair = cryptoSuite.generateRandomKeyPair();
        // 获取账户地址
        String accountAddress = cryptoKeyPair.getAddress();
        System.out.println("Generated account address: " + accountAddress);
        return new byte[0];
    }

    @Override
    public byte[] deleteAsset(String contract, String assetId) throws Exception {
        //BCOS does not support delete account
        return new byte[0];
    }

    @Override
    public byte[] updateAsset(String contract, String assetId, Integer amount) throws Exception {
        List<Object> params = new ArrayList<>();
        params.add(amount);
        String founctionName = bcosOperation.get("query").getFunctionName();
        TransactionResponse response = assembleTransactionProcessor.sendTransactionAndGetResponseByContractLoader(contract, "src/main/resources/bcos/abi/" + abi + ".abi", founctionName, params);

        return new byte[0];
    }

    @Override
    public byte[] queryAsset(String contract, String assetId) throws Exception {

        String founctionName = bcosOperation.get("query").getFunctionName();

        CallResponse callResponse = assembleTransactionProcessor.sendCallByContractLoader(contract,"src/main/resources/bcos/abi/"+abi+".abi",founctionName,new ArrayList<>());
        System.out.println("Query result: " + callResponse.toString());
        return new byte[0];
    }

    @Override
    public byte[] getChainHeight() throws Exception {
        BigInteger blockLimit = client.getBlockLimit();
        System.out.println("Cached chain height: " + blockLimit);
        return new byte[0];
    }

    @Override
    public void close() throws Exception {

    }

}
