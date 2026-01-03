package org.cqupt.crypto.hcaa.http;

import org.cqupt.crypto.hcaa.adapter.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    @PostMapping("/createcontract")
    public String createContract(@RequestParam String contractName, @RequestParam String contractCode) {
        try {
            blockchainService.createContract(contractName, contractCode);
            return "Contract created successfully";
        } catch (Exception e) {
            return "Error creating contract: " + e.getMessage();
        }
    }

    @PostMapping("/deletecontract")
    public String deleteContract(@RequestParam String contractName) {
        try {
            blockchainService.deleteContract(contractName);
            return "Contract deleted successfully";
        } catch (Exception e) {
            return "Error deleting contract: " + e.getMessage();
        }
    }

    @PostMapping("/updatecontract")
    public String updateContract(@RequestParam String contractName, @RequestParam String newContractCode) {
        try {
            blockchainService.updateContract(contractName, newContractCode);
            return "Contract updated successfully";
        } catch (Exception e) {
            return "Error updating contract: " + e.getMessage();
        }
    }

    @GetMapping("/querycontract")
    public String queryContract(@RequestParam String contractName) {
        try {
            blockchainService.queryContract(contractName);
            return "Contract queried successfully";
        } catch (Exception e) {
            return "Error querying contract: " + e.getMessage();
        }
    }

    @PostMapping("/createasset")
    public String createAsset(@RequestParam String contract,@RequestParam String assetId) {
        try {
            blockchainService.createAsset(contract,assetId);
            return "Asset created successfully";
        } catch (Exception e) {
            return "Error creating asset: " + e.getMessage();
        }
    }

    @PostMapping("/deleteasset")
    public String deleteAsset(@RequestParam String contract, @RequestParam String assetId) {
        try {
            blockchainService.deleteAsset(contract, assetId);
            return "Asset deleted successfully";
        } catch (Exception e) {
            return "Error deleting asset: " + e.getMessage();
        }
    }

    @PostMapping("/updateasset")
    public String updateAsset(@RequestParam String contract, @RequestParam String assetId, @RequestParam Integer amount) {
        try {
            blockchainService.updateAsset(contract, assetId, amount);
            return "Asset updated successfully";
        } catch (Exception e) {
            return "Error updating asset: " + e.getMessage();
        }
    }

    @GetMapping("/queryasset")
    public String queryAsset(@RequestParam String contract, @RequestParam String assetId) {
        try {
            blockchainService.queryAsset(contract, assetId);
            return "Asset queried successfully";
        } catch (Exception e) {
            return "Error querying asset: " + e.getMessage();
        }
    }
}
