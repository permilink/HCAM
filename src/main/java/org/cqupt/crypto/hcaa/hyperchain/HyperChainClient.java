package org.cqupt.crypto.hcaa.hyperchain;

import cn.hyperchain.contract.BaseInvoke;
import cn.hyperchain.sdk.common.utils.Decoder;
import cn.hyperchain.sdk.common.utils.FileUtil;
import cn.hyperchain.sdk.exception.RequestException;
import cn.hyperchain.sdk.provider.DefaultHttpProvider;
import cn.hyperchain.sdk.provider.ProviderManager;
import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.account.Algo;
import cn.hyperchain.sdk.request.Request;
import cn.hyperchain.sdk.response.ReceiptResponse;
import cn.hyperchain.sdk.response.TxHashResponse;
import cn.hyperchain.sdk.response.account.StatusResponse;
import cn.hyperchain.sdk.response.block.BlockNumberResponse;
import cn.hyperchain.sdk.response.block.BlockResponse;
import cn.hyperchain.sdk.response.contract.CompileContractResponse;
import cn.hyperchain.sdk.response.contract.StringResponse;
import cn.hyperchain.sdk.service.*;

import cn.hyperchain.sdk.transaction.Transaction;
import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.util.PasswordGenerator;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import static java.rmi.server.LogStream.log;

@Component("HyperChain")
public class HyperChainClient implements BlockchainClient {
    private ContractService contractService;
    private static AccountService accountService;
    private static Common common = new Common();
    private static ProviderManager providerManager = Common.soloProviderManager;
    private static TxService sendTxService = ServiceManager.getTxService(providerManager);
    private static BlockService blockService = ServiceManager.getBlockService(providerManager);

    @Override
    public void createContract(String contractName, String contractCode) throws Exception {
        Account account = accountService.genAccount(Algo.SMRAW);
        InputStream payload = FileUtil.readFileAsStream("hvm-jar/contractcollection-2.0-SNAPSHOT.jar");
        Transaction transaction = new Transaction.HVMBuilder(account.getAddress()).deploy(payload).build();
        transaction.sign(accountService.fromAccountJson(account.toJson()));
        // 4. get request
        TxHashResponse txHashResponse = contractService.deploy(transaction).send();
        ReceiptResponse receiptResponse = txHashResponse.polling();
        String txHash = receiptResponse.getTxHash();
        String contractAddress = receiptResponse.getContractAddress();
        String addr = contractAddress;
        System.out.println("部署合约交易哈希: " + txHash);
        System.out.println("部署合约地址: " + addr);
        System.out.println("部署返回(未解码): " + receiptResponse.getRet());
        System.out.println("部署返回(解码)：" + Decoder.decodeHVM(receiptResponse.getRet(), String.class));
    }

    @Override
    public void deleteContract(String contractName) throws Exception {
        //unsupported in litesdk
    }

    @Override
    public void updateContract(String contractName, String newContractCode) throws Exception {
        CompileContractResponse compileContractResponse = contractService.compileContract(newContractCode).send();
        CompileContractResponse.CompileCode result = compileContractResponse.getResult();
        //test, should use admin account
        Account account = accountService.genAccount(Algo.SMRAW);
        Transaction transaction = new Transaction.HVMBuilder(account.getAddress())
                .upgrade(contractName, result.toString())
                .build();
        TxHashResponse send = contractService.maintain(transaction).send();
        ReceiptResponse receiptResponse = send.polling();
        System.out.println("更新合约交易哈希: " + receiptResponse.getTxHash());
    }

    @Override
    public List<String> queryContract(String contractName) throws Exception {
//        Request<StringResponse> statusByCName = contractService.getStatusByCName(contractName);
        //get contract address by name, here we assume contractName is the address
        StringResponse stringResponse = contractService.getStatus(contractName).send();
        System.out.println("合约状态: " + stringResponse.getResult());
        return null;
    }

    public static Account genAccount(Algo algo, String password) {
        return accountService.genAccount(algo, password);
    }
    @Override
    public byte[] createAsset(String contract, String assetId) throws Exception {
        //随机生成密码
        String password = PasswordGenerator.generateRandomPassword();
        cn.hyperchain.sdk.account.Account account = accountService.genAccount(Algo.SMAES, password);
        System.out.println("创建账户地址: " + account.getAddress());
        common.deployEVM(account);
        return null;
    }
    @Override
    public byte[] deleteAsset(String contract, String assetId) throws Exception {
        //litesdk unsupported this function
        return new byte[0];
    }
    @Override
    public byte[] updateAsset(String contract, String assetaddr, Integer amount) throws Exception {
        //should be admin's address
        String address = "0x37a1100567bf7e0de2f5a0dc1917f0552aa43d88";
        cn.hyperchain.sdk.transaction.Transaction transaction = new cn.hyperchain.sdk.transaction.Transaction.Builder(address).transfer(assetaddr, amount).build();
        Request<TxHashResponse> request = sendTxService.sendTx(transaction);
        ReceiptResponse response = request.send().polling();
        System.out.println(response.getTxHash());
        return new byte[0];
    }
    @Override
    public byte[] queryAsset(String contract, String assetId) throws Exception {
        Request<StatusResponse> balance = accountService.getStatus(assetId);
        StatusResponse send = balance.send();
        System.out.println(send.getStatus());

        return new byte[0];
    }

    @Override
    public byte[] getChainHeight() throws Exception {
        Request<BlockNumberResponse> blockResponseBlockRequest = blockService.getChainHeight();
        BlockNumberResponse send = blockResponseBlockRequest.send();
        String result = send.getResult();
        System.out.println(result);
        return new byte[0];
    }

    @Override
    public void close() throws Exception {

    }

    public void init() throws Exception {
        String DEFAULT_URL = "localhost:8081";
        DefaultHttpProvider defaultHttpProvider = new DefaultHttpProvider.Builder().setUrl(DEFAULT_URL).build();
        ProviderManager providerManager = ProviderManager.createManager(defaultHttpProvider);
        contractService = ServiceManager.getContractService(providerManager);
        accountService = ServiceManager.getAccountService(providerManager);
    }


}
