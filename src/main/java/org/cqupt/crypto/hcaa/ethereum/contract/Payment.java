package org.cqupt.crypto.hcaa.ethereum.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.14.0.
 */
@SuppressWarnings("rawtypes")
public class Payment extends Contract {
    public static final String BINARY = "0x608060405234801561000f575f80fd5b50335f806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506104a58061005c5f395ff3fe608060405234801561000f575f80fd5b506004361061004a575f3560e01c806327e235e31461004e5780638da5cb5b1461007e578063c40768761461009c578063f8b2cb4f146100b8575b5f80fd5b610068600480360381019061006391906102ab565b6100e8565b60405161007591906102ee565b60405180910390f35b6100866100fd565b6040516100939190610316565b60405180910390f35b6100b660048036038101906100b19190610359565b610120565b005b6100d260048036038101906100cd91906102ab565b610207565b6040516100df91906102ee565b60405180910390f35b6001602052805f5260405f205f915090505481565b5f8054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b5f8111610162576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610159906103f1565b60405180910390fd5b8060015f8473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f8282546101ae919061043c565b925050819055508173ffffffffffffffffffffffffffffffffffffffff167f737c69225d647e5994eab1a6c301bf6d9232beb2759ae1e27a8966b4732bc489826040516101fb91906102ee565b60405180910390a25050565b5f60015f8373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f20549050919050565b5f80fd5b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f61027a82610251565b9050919050565b61028a81610270565b8114610294575f80fd5b50565b5f813590506102a581610281565b92915050565b5f602082840312156102c0576102bf61024d565b5b5f6102cd84828501610297565b91505092915050565b5f819050919050565b6102e8816102d6565b82525050565b5f6020820190506103015f8301846102df565b92915050565b61031081610270565b82525050565b5f6020820190506103295f830184610307565b92915050565b610338816102d6565b8114610342575f80fd5b50565b5f813590506103538161032f565b92915050565b5f806040838503121561036f5761036e61024d565b5b5f61037c85828601610297565b925050602061038d85828601610345565b9150509250929050565b5f82825260208201905092915050565b7f416d6f756e74206d75737420626520706f7369746976650000000000000000005f82015250565b5f6103db601783610397565b91506103e6826103a7565b602082019050919050565b5f6020820190508181035f830152610408816103cf565b9050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f610446826102d6565b9150610451836102d6565b92508282019050808211156104695761046861040f565b5b9291505056fea26469706673582212202195068b773ef43d9da6de116e0b0f83a9dd56550c88ea8372a973406bb6e22b64736f6c63430008150033";

    public static final String FUNC_BALANCES = "balances";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PAY = "pay";

    public static final String FUNC_GETBALANCE = "getBalance";

    public static final Event PAID_EVENT = new Event("Paid", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<String, String>();
    }

    @Deprecated
    protected Payment(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Payment(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Payment(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Payment(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<PaidEventResponse> getPaidEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PAID_EVENT, transactionReceipt);
        ArrayList<PaidEventResponse> responses = new ArrayList<PaidEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PaidEventResponse typedResponse = new PaidEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.to = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PaidEventResponse getPaidEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PAID_EVENT, log);
        PaidEventResponse typedResponse = new PaidEventResponse();
        typedResponse.log = log;
        typedResponse.to = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<PaidEventResponse> paidEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPaidEventFromLog(log));
    }

    public Flowable<PaidEventResponse> paidEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAID_EVENT));
        return paidEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> balances(String param0) {
        final Function function = new Function(FUNC_BALANCES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> pay(String to, BigInteger amount) {
        final Function function = new Function(
                FUNC_PAY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(to), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getBalance(String user) {
        final Function function = new Function(FUNC_GETBALANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    @Deprecated
    public static Payment load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Payment(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Payment load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Payment(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Payment load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Payment(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Payment load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Payment(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Payment> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Payment.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Payment> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Payment.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Payment> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Payment.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Payment> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Payment.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class PaidEventResponse extends BaseEventResponse {
        public String to;

        public BigInteger amount;
    }
}
