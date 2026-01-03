package org.cqupt.crypto.hcaa.adapter;

import org.cqupt.crypto.hcaa.HcamApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlockchainService {
    @Autowired
    private HcamApplication hcamApplication;

    public void createContract(String contractName, String contractCode) throws Exception {
        hcamApplication.getCurrentClient().createContract(contractName, contractCode);
    }

    public void deleteContract(String contractName) throws Exception {
        hcamApplication.getCurrentClient().deleteContract(contractName);
    }

    public void updateContract(String contractName, String newContractCode) throws Exception {
        hcamApplication.getCurrentClient().updateContract(contractName, newContractCode);
    }

    public void queryContract(String contractName) throws Exception {
        hcamApplication.getCurrentClient().queryContract(contractName);
    }

    public void createAsset(String contractName,String assetId) throws Exception {
        hcamApplication.getCurrentClient().createAsset(contractName,assetId);
    }

    public void deleteAsset(String contractName, String assetId) throws Exception {
        hcamApplication.getCurrentClient().deleteAsset(contractName, assetId);
    }

    public void updateAsset(String contractName, String assetId, Integer amount) throws Exception {
        hcamApplication.getCurrentClient().updateAsset(contractName, assetId, amount);
    }

    public void queryAsset(String contractName, String assetId) throws Exception{
        hcamApplication.getCurrentClient().queryAsset(contractName, assetId);
    }

    public void getChainHeight() throws Exception {
        hcamApplication.getCurrentClient().getChainHeight();
    }
}
