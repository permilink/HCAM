package org.cqupt.crypto.hcaa.adapter;

import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.hyperledger.fabric.client.GatewayException;

import java.util.List;
import java.util.Map;

public interface BlockchainClient {
    //contract operations
    void createContract(String contractName, String contractCode) throws Exception;
    void deleteContract(String contractName) throws ChainMakerCryptoSuiteException, Exception;
    void updateContract(String contractName, String newContractCode) throws Exception;
    List<String> queryContract(String contractName) throws Exception;
    //asset operations
    byte[] createAsset(String contract,String assetId) throws Exception;
    byte[] deleteAsset(String contract,String assetId) throws Exception;
    byte[] updateAsset(String contract,String assetId, Integer amount) throws Exception;
    byte[] queryAsset(String contract,String assetId) throws Exception;
    byte[] getChainHeight() throws Exception;
    void close() throws Exception;

    void init() throws Exception;
}
