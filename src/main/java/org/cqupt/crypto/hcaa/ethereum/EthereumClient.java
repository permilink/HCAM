package org.cqupt.crypto.hcaa.ethereum;

import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.ethereum.contract.Payment;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Component("Ethereum")
public class EthereumClient implements BlockchainClient {
    private Web3j web3;
    private String pk = ""; //private key

    @Override
    public void createContract(String contractName, String contractCode) throws Exception {
        //ethereum deploy contract can not be dynamic, need to compile first and generate java class file
        //here we use the pre-generated DocumentRegistry class to deploy, pre-generated way can refer to web3j official doc:https://kauri.io/#article/84475132317d4d6a84a2c42eb9348e4b
        Credentials credentials = Credentials.create(pk);
        BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
        BigInteger gasLimit = BigInteger.valueOf(6_721_975L);
        StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
        System.out.println("Deploying contract...");
        Payment contract = Payment.deploy(
                web3,
                credentials,
                gasProvider
        ).send();
        String contractAddress = contract.getContractAddress();
        System.out.println("Contract deployed at address: " + contractAddress);
    }

    @Override
    public void deleteContract(String contractName) throws ChainMakerCryptoSuiteException, Exception {
        //ethereum does not support delete contract
    }

    @Override
    public void updateContract(String contractName, String newContractCode) throws Exception {
        //ethereum does not support update contract by code
    }

    @Override
    public List<String> queryContract(String contractName) throws Exception {
        //测试是否连接成功
        String web3ClientVersion = web3.web3ClientVersion().send().getWeb3ClientVersion();
        System.out.println("version=" + web3ClientVersion);
        return List.of();
    }

    @Override
    public byte[] createAsset(String contract, String assetId) throws Exception {
        // 指定密钥文件并验证账户和密码
        // String coinBaseFile = "E:\\code\\tf\\web3jdemo-master\\UTC--2023-03-18T03-53-26.594744041Z--c8f2eab07f7aa4c37a67afdcd6ccb50cb7f1b14f";
        String coinBaseFile = "UTC--2023-03-22T03-55-59.205896981Z--ea56facd3d6abdcc700643278dc73e458b17e65e";
        String myPWD = "";
        Credentials credentials = WalletUtils.loadCredentials(myPWD, coinBaseFile);
        System.out.println("Get the account address"+credentials.getAddress());
        String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
        System.out.println("Get the unencrypted private key into hexadecimal"+privateKey);
        return new byte[0];
    }

    @Override
    public byte[] deleteAsset(String contract, String assetId) throws Exception {
        //web3j does not support delete account
        return new byte[0];
    }

    @Override
    public byte[] updateAsset(String contract, String assetAddr, Integer amount) throws Exception {
        // Decrypt and open the wallet into a Credential object
        try {

            // Decrypt and open the wallet into a Credential object
        Credentials credentials = Credentials.create(pk);
        System.out.println("Account address: " + credentials.getAddress());
        System.out.println("Balance: " + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));

        // Get the latest nonce
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce =  ethGetTransactionCount.getTransactionCount();

        // Recipient address

        // Value to transfer (in wei)
        BigInteger value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger();

        // Gas Parameters
        BigInteger gasLimit = BigInteger.valueOf(21000);
        BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();

        // Prepare the rawTransaction
        RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                assetAddr,
                value);

        // Sign the transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        // Send transaction
        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
        String transactionHash = ethSendTransaction.getTransactionHash();
        System.out.println("transactionHash: " + transactionHash);

        // Wait for transaction to be mined
        Optional<TransactionReceipt> transactionReceipt = null;
        do {
            System.out.println("checking if transaction " + transactionHash + " is mined....");
            EthGetTransactionReceipt ethGetTransactionReceiptResp = web3.ethGetTransactionReceipt(transactionHash).send();
            transactionReceipt = ethGetTransactionReceiptResp.getTransactionReceipt();
            Thread.sleep(3000); // Wait 3 sec
        } while(!transactionReceipt.isPresent());

        System.out.println("Transaction " + transactionHash + " was mined in block # " + transactionReceipt.get().getBlockNumber());
        System.out.println("Balance: " + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));


    } catch (IOException | InterruptedException ex) {
        throw new RuntimeException(ex);
    }

        return new byte[0];
    }

    @Override
    public byte[] queryAsset(String contract, String assetId) throws Exception {
        //assetId is the user address
        EthGetBalance balanceWei = web3.ethGetBalance(assetId, DefaultBlockParameterName.LATEST).send();
        BigDecimal balanceInEther = Convert.fromWei(balanceWei.getBalance().toString(), Convert.Unit.ETHER);
        System.out.println("user:"+assetId+", balance:"+balanceInEther);
        return new byte[0];
    }

    @Override
    public byte[] getChainHeight() throws Exception {
        return new byte[0];
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void init() throws Exception {
        EthereumConfig ethereumConfig = new EthereumConfig();
        web3 = Web3j.build(new org.web3j.protocol.http.HttpService(ethereumConfig.readConfig()));
        //测试是否连接成功
        String web3ClientVersion = web3.web3ClientVersion().send().getWeb3ClientVersion();
        System.out.println("version=" + web3ClientVersion);
    }
}
