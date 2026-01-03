package org.cqupt.crypto.hcaa.chainmaker;

import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.Request;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.cqupt.crypto.hcaa.HcamApplication;
import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.util.AdapterConfig;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cqupt.crypto.hcaa.chainmaker.execution.InitClient.chainClient;
import static org.cqupt.crypto.hcaa.chainmaker.execution.InitClient.initChainClientForPk;

@Component("Chainmaker")
public class ChainmakerClient implements BlockchainClient {

    private static Map<String, AdapterConfig.Operation> fun_operation;

    @Override
    public void createContract(String contractName, String byteCodePath) throws Exception {

        byte[] byteCode = Files.readAllBytes(Paths.get(byteCodePath));

        // 准备参数
        Map<String, byte[]> params = new HashMap<>();

        // 创建 payload
        Request.Payload payload = chainClient.createContractCreatePayload(
                contractName, "1.0.0", byteCode, ContractOuterClass.RuntimeType.WASMER, params);

        // 发送交易
        ResultOuterClass.TxResponse response = chainClient.sendContractManageRequest(payload, null, 10000, 10000);
        System.out.println("✅ Contract '" + contractName + "' deployed.");
    }

    @Override
    public void deleteContract(String contractName) throws Exception {
        Request.Payload payload = chainClient.createContractRevokePayload(contractName);

        ResultOuterClass.TxResponse response = chainClient.sendContractManageRequest(payload, null, 10000, 10000);
        System.out.println("撤销合约结果: " + response.getMessage());
    }

    @Override
    public void updateContract(String contractName, String byteCodePath) throws Exception {
        byte[] byteCode = Files.readAllBytes(Paths.get(byteCodePath));

        Request.Payload payload = chainClient.createContractUpgradePayload(
                contractName, "1.0.1", byteCode, ContractOuterClass.RuntimeType.WASMER, null);

        ResultOuterClass.TxResponse response = chainClient.sendContractManageRequest(payload, null, 10000, 10000);
        System.out.println("更新合约结果: " + response.getMessage());
    }

    @Override
    public List<String> queryContract(String contractName) throws ChainClientException, ChainMakerCryptoSuiteException {
        ContractOuterClass.Contract contract = chainClient.getContractInfo(contractName, 10000);
        List<String> result = null;
        if (contract != null) {
            result = List.of(
                    contract.getName(),
                    contract.getVersion(),
                    contract.getStatus().toString()
            );
            System.out.println("合约名称: " + contract.getName());
            System.out.println("合约版本: " + contract.getVersion());
            System.out.println("合约状态: " + contract.getStatus());
        }
        return null;
    }
    //create new asset on chain
    @Override
    public byte[] createAsset(String contract,String assetId) throws ChainClientException, ChainMakerCryptoSuiteException {
        //理论上还需要校验配置的参数与这里传参类型、数量是否一致
        Map<String, byte[]> params = new HashMap<>();
        params.put(fun_operation.get("create").getParameterMapping()[0].getSource(), assetId.getBytes());
        params.put(fun_operation.get("create").getParameterMapping()[1].getSource(), "0".getBytes());

        // 使用 invokeContract 发起写操作
        ResultOuterClass.TxResponse response = chainClient.invokeContract(
                contract, fun_operation.get("create").getFunctionName(), null, params, 10000, 10000);

        System.out.println("资产上链交易ID: " + response.getTxId());
        return null;
    }

    @Override
    public byte[] deleteAsset(String contract,String assetId) throws ChainClientException, ChainMakerCryptoSuiteException {
        Map<String, byte[]> params = new HashMap<>();
        params.put(fun_operation.get("delete").getParameterMapping()[0].getSource(), assetId.getBytes());

        ResultOuterClass.TxResponse response = chainClient.invokeContract(
                contract, fun_operation.get("delete").getFunctionName(), null, params, 10000, 10000);

        System.out.println("删除结果: " + response.getMessage());
        return response.getMessage().getBytes();
    }

    @Override
    public byte[] updateAsset(String contract,String assetId, Integer amount) throws ChainClientException, ChainMakerCryptoSuiteException {
        Map<String, byte[]> params = new HashMap<>();
        params.put(fun_operation.get("update").getParameterMapping()[0].getSource(), assetId.getBytes());
        params.put(fun_operation.get("update").getParameterMapping()[1].getSource(), amount.toString().getBytes());

        ResultOuterClass.TxResponse response = chainClient.invokeContract(
                contract, fun_operation.get("update").getFunctionName(), null, params, 10000, 10000);

        System.out.println("修改结果: " + response.getCode());
        return null;
    }

    @Override
    public byte[] queryAsset(String contract,String assetId) throws ChainClientException, ChainMakerCryptoSuiteException {
        Map<String, byte[]> params = new HashMap<>();
        params.put(fun_operation.get("query").getParameterMapping()[0].getSource(), assetId.getBytes());

        // 使用 queryContract 发起只读查询，不产生交易ID
        ResultOuterClass.TxResponse response = chainClient.queryContract(
                contract, fun_operation.get("query").getFunctionName(), null, params, 10000);

        String result = response.getContractResult().getResult().toStringUtf8();
        System.out.println("查询结果: " + result);
        return result.getBytes();
    }

    @Override
    public byte[] getChainHeight() throws Exception {
        long currentBlockHeight = chainClient.getCurrentBlockHeight(100);
        System.out.println("当前区块高度: " + currentBlockHeight);
        return new byte[0];
    }

    @Override
    public void close() throws Exception {

    }

    public void init() {
        try {
            initChainClientForPk();
            fun_operation = HcamApplication.getFun_operation();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
