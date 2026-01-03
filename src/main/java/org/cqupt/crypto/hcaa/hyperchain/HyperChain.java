package org.cqupt.crypto.hcaa.hyperchain;

import cn.hyperchain.sdk.exception.RequestException;
import cn.hyperchain.sdk.provider.DefaultHttpProvider;
import cn.hyperchain.sdk.provider.ProviderManager;
import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.account.Algo;
import cn.hyperchain.sdk.request.Request;
import cn.hyperchain.sdk.response.ReceiptResponse;
import cn.hyperchain.sdk.response.TxHashResponse;
import cn.hyperchain.sdk.response.account.StatusResponse;
import cn.hyperchain.sdk.service.AccountService;
import cn.hyperchain.sdk.service.ContractService;
import cn.hyperchain.sdk.service.ServiceManager;

import cn.hyperchain.sdk.service.TxService;
import cn.hyperchain.sdk.transaction.Transaction;
import org.cqupt.crypto.hcaa.util.PasswordGenerator;

public class HyperChain {
    private ContractService contractService;
    private static AccountService accountService;
    private static Common common = new Common();
    private static ProviderManager providerManager = Common.soloProviderManager;
    private static TxService sendTxService = ServiceManager.getTxService(providerManager);

    public void init() {
        String DEFAULT_URL = "localhost:8081";
        DefaultHttpProvider defaultHttpProvider = new DefaultHttpProvider.Builder().setUrl(DEFAULT_URL).build();
        ProviderManager providerManager = ProviderManager.createManager(defaultHttpProvider);
        contractService = ServiceManager.getContractService(providerManager);
        accountService = ServiceManager.getAccountService(providerManager);
    }

    public static Account genAccount(Algo algo, String password) {
        return accountService.genAccount(algo, password);
    }

    public byte[] createAsset(String contract, String assetId) throws Exception {
        //随机生成密码
        String password = PasswordGenerator.generateRandomPassword();
        cn.hyperchain.sdk.account.Account account = accountService.genAccount(Algo.SMAES, password);
        System.out.println("创建账户地址: " + account.getAddress());
        common.deployEVM(account);
        return null;
    }

    public byte[] deleteAsset(String contract, String assetId) throws Exception {
        //litesdk unsupported this function
        return new byte[0];
    }

    public byte[] updateAsset(String contract, String assetaddr, Integer amount) throws Exception {
        //should be admin's address
        String address = "0x37a1100567bf7e0de2f5a0dc1917f0552aa43d88";
        Transaction transaction = new Transaction.Builder(address).transfer(assetaddr, amount).build();
        Request<TxHashResponse> request = sendTxService.sendTx(transaction);
        ReceiptResponse response = request.send().polling();
        System.out.println(response.getTxHash());
        return new byte[0];
    }

    public byte[] queryAsset(String contract, String assetId) throws RequestException {
        Request<StatusResponse> balance = accountService.getStatus(assetId);
        StatusResponse send = balance.send();
        System.out.println(send.getStatus());

        return new byte[0];
    }
}
