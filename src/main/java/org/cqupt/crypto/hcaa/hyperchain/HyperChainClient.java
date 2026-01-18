package org.cqupt.crypto.hcaa.hyperchain;

import cn.hyperchain.contract.BaseInvoke;
import cn.hyperchain.sdk.bvm.OperationResult;
import cn.hyperchain.sdk.bvm.Result;
import cn.hyperchain.sdk.bvm.operate.AccountOperation;
import cn.hyperchain.sdk.bvm.operate.BuiltinOperation;
import cn.hyperchain.sdk.bvm.operate.ProposalOperation;
import cn.hyperchain.sdk.common.utils.Decoder;
import cn.hyperchain.sdk.common.utils.FileUtil;
import cn.hyperchain.sdk.exception.RequestException;
import cn.hyperchain.sdk.provider.DefaultHttpProvider;
import cn.hyperchain.sdk.provider.HttpProvider;
import cn.hyperchain.sdk.provider.ProviderManager;
import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.account.Algo;
import cn.hyperchain.sdk.request.Request;
import cn.hyperchain.sdk.response.ReceiptResponse;
import cn.hyperchain.sdk.response.TxHashResponse;
import cn.hyperchain.sdk.response.account.StatusResponse;
import cn.hyperchain.sdk.response.block.BlockNumberResponse;
import cn.hyperchain.sdk.response.block.BlockResponse;
import cn.hyperchain.sdk.response.config.ProposalResponse;
import cn.hyperchain.sdk.response.contract.CompileContractResponse;
import cn.hyperchain.sdk.response.contract.StringResponse;
import cn.hyperchain.sdk.service.*;

import cn.hyperchain.sdk.transaction.Transaction;
import org.cqupt.crypto.hcaa.adapter.BlockchainClient;
import org.cqupt.crypto.hcaa.util.CommonUtils;
import org.cqupt.crypto.hcaa.util.ConfigReader;
import org.cqupt.crypto.hcaa.util.PasswordGenerator;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static cn.hyperchain.sdk.bvm.OperationResultCode.SuccessCode;
import static java.rmi.server.LogStream.log;

@Component("HyperChain")
public class HyperChainClient implements BlockchainClient {
    private ConfigReader configReader;
    private ContractService contractService;
    private static AccountService accountService;
//    private static Common common = new Common();
//    private static ProviderManager providerManager = Common.soloProviderManager;
    private static TxService sendTxService;
    private static BlockService blockService;
    private ConfigService configService;
    private Account account;

    @Override
    public void init() throws Exception {
        configReader = new ConfigReader("hpc.properties");
        String DEFAULT_URL = configReader.getProperty("nodeIp");
        InputStream tlsCa = FileUtil.readFileAsStream(configReader.getProperty("tlsCaPath"));
        InputStream tls_peer_cert = FileUtil.readFileAsStream(configReader.getProperty("tlsPeerCertPath"));
        InputStream tls_peer_priv = FileUtil.readFileAsStream(configReader.getProperty("tlsPeerPrivPath"));
        HttpProvider httpProvider = new DefaultHttpProvider.Builder()
                .setUrl(DEFAULT_URL)
                .https(tlsCa, tls_peer_cert, tls_peer_priv)
                .build();
        ProviderManager providerManager = ProviderManager.createManager(httpProvider);
        contractService = ServiceManager.getContractService(providerManager);
        accountService = ServiceManager.getAccountService(providerManager);
        sendTxService = ServiceManager.getTxService(providerManager);
        blockService = ServiceManager.getBlockService(providerManager);
        configService = ServiceManager.getConfigService(providerManager);
        AuthService authService = ServiceManager.getAuthService(providerManager);
        // create admin account and add admin role, this account will be used to create/delete/update contract
        account = accountService.genAccount(Algo.SMRAW);
        List<String> roles = new java.util.ArrayList<>();
        roles.add("admin");
        authService.addRoles(account.getAddress(), roles);
    }

    /**
     * deploy contract
     * contractName: jar file name without .jar suffix
     * contractCode: unused
     * */
    @Override
    public void createContract(String contractName, String contractCode) throws Exception {
        //获取hvm-jar路径下的jar包文件名
        //Path hvmJarPath = Paths.get("hvm-jar");
        //String fileName = Files.list(hvmJarPath).findFirst().get().getFileName().toString();
        //here we directly use the contractName as the file name, user should ensure the file exists
        InputStream payload = FileUtil.readFileAsStream("hvm-jar/"+contractName+".jar");
        Transaction transaction = new Transaction.HVMBuilder(account.getAddress()).deploy(payload).build();
        transaction.sign(account);
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

    /**
     * freeze contract
     * contractName: contract address
     * */
    @Override
    public void deleteContract(String contractName) throws Exception {
        Transaction transaction = new Transaction.HVMBuilder(account.getAddress()).freeze(contractName).build();
        transaction.sign(account);
        TxHashResponse txHashResponse = contractService.maintain(transaction).send();
        ReceiptResponse receiptResponse = txHashResponse.polling();
        String txHash = receiptResponse.getTxHash();
        String contractAddress = receiptResponse.getContractAddress();
        String addr = contractAddress;
        System.out.println("更新合约交易哈希: " + txHash);
        System.out.println("更新合约地址: " + addr);
        System.out.println("更新返回(未解码): " + receiptResponse.getRet());
        System.out.println("更新返回(解码)：" + Decoder.decodeHVM(receiptResponse.getRet(), String.class));
    }
    /**
     * update contract
     * contractName: contract address
     * newContractCode: new contract code
     * */
    @Override
    public void updateContract(String contractName, String newContractCode) throws Exception {
        CompileContractResponse compileContractResponse = contractService.compileContract(newContractCode).send();
        CompileContractResponse.CompileCode result = compileContractResponse.getResult();
        Transaction transaction = new Transaction.HVMBuilder(account.getAddress())
                .upgrade(contractName, result.toString())
                .build();
        TxHashResponse send = contractService.maintain(transaction).send();
        ReceiptResponse receiptResponse = send.polling();
        System.out.println("更新合约交易哈希: " + receiptResponse.getTxHash());
    }
    /**
     * query contract
     * contractName: contract address
     * */
    @Override
    public List<String> queryContract(String contractName) throws Exception {
        StringResponse stringResponse = contractService.getStatus(contractName).send();
        System.out.println("合约状态: " + stringResponse.getResult());
        return null;
    }
    /**
     *  create asset
     * */
    @Override
    public byte[] createAsset(String contract, String assetId) throws Exception {
        Account newAccount = accountService.genAccount(Algo.SMRAW);
        //cert should be generated by CA in real case
        String cert = "";
        Transaction transaction = new Transaction.
                BVMBuilder(account.getAddress()).
                invoke(new AccountOperation.AccountBuilder().register(newAccount.getAddress(), cert).build()).
                build();
        transaction.sign(account);
        ReceiptResponse receiptResponse = contractService.invoke(transaction).send().polling();
        Result result = Decoder.decodeBVM(receiptResponse.getRet());
        System.out.println("创建账户地址: " + account.getAddress());
        return null;
    }
    @Override
    public byte[] deleteAsset(String contract, String assetId) throws Exception {
        //get sdkCert from certs
        String sdkCert = CommonUtils.readCertFile("hyperchain/certs/sdkcert.cert");

        Transaction transaction = new Transaction.
                BVMBuilder(account.getAddress()).
                invoke(new AccountOperation.AccountBuilder().abandon(assetId, sdkCert).build()).
                build();
        transaction.sign(account);
        ReceiptResponse receiptResponse = contractService.invoke(transaction).send().polling();
        Result result = Decoder.decodeBVM(receiptResponse.getRet());
        return new byte[0];
    }
    @Override
    public byte[] updateAsset(String contract, String assetaddr, Integer amount) throws Exception {
        cn.hyperchain.sdk.transaction.Transaction transaction = new cn.hyperchain.sdk.transaction.Transaction.Builder(account.getAddress()).transfer(assetaddr, amount).build();
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

//    public String completeManageContractByVote(BuiltinOperation opt) throws RequestException {
//        Account acc = accountService.fromAccountJson(accountJsons[0]);
//        Transaction transaction = new Transaction.
//                BVMBuilder(acc.getAddress()).
//                invoke(opt).
//                build();
//        transaction.sign(acc);
//
//        ReceiptResponse receiptResponse = contractService.manageContractByVote(transaction).send().polling();
//        Result result = Decoder.decodeBVM(receiptResponse.getRet());
//        System.out.println(result);
//
//        Request<ProposalResponse> proposal = configService.getProposal();
//        ProposalResponse proposalResponse = proposal.send();
//        ProposalResponse.Proposal prop = proposalResponse.getProposal();
//
//        // vote
//        for (int i = 1; i < 6; i++) {
//            invokeBVMContract(new ProposalOperation.ProposalBuilder().vote(prop.getId(), true).build(), accountService.fromAccountJson(accountJsons[i]));
//        }
//
//        // execute
//        result = invokeBVMContract(new ProposalOperation.ProposalBuilder().execute(prop.getId()).build(), accountService.fromAccountJson(accountJsons[0]));
//
//        System.out.println(result.getRet());
//        List<OperationResult> resultList = Decoder.decodeBVMResult(result.getRet());
//
//        if (resultList.size() > 0) {
//            return resultList.get(0).getMsg();
//        }
//        return null;
//    }

//    public Result invokeBVMContract(BuiltinOperation opt, Account acc) throws RequestException {
//        Transaction transaction = new Transaction.
//                BVMBuilder(acc.getAddress()).
//                invoke(opt).
//                build();
//        transaction.sign(acc);
//
//        ReceiptResponse receiptResponse = contractService.invoke(transaction).send().polling();
//        Result result = Decoder.decodeBVM(receiptResponse.getRet());
//        System.out.println(result);
//        return result;
//    }
}
