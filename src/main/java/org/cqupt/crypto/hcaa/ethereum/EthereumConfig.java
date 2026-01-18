package org.cqupt.crypto.hcaa.ethereum;

import java.io.IOException;

import org.cqupt.crypto.hcaa.util.ConfigReader;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

public class EthereumConfig {
    //read address from ethereum.config
    public String readConfig(){
        ConfigReader configReader = new ConfigReader("ethereum.config");
        String ethereumAddress = configReader.getProperty("ethereum_address");
        return ethereumAddress;
    }
}
