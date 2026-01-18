package org.cqupt.crypto.hcaa.util;

import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CommonUtils {
    public static CryptoSuite getCryptoSuite() {
        return new CryptoSuite(CryptoType.ECDSA_TYPE);
    }

    public static ContractABIDefinition getContractABIDefinition(String abiDesc) {
        CryptoSuite cryptoSuite = getCryptoSuite();
        ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
        return abiDefinitionFactory.loadABI(abiDesc);
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String bytesToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] stringToBytes(String string) {
        int len = string.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
                    + Character.digit(string.charAt(i + 1), 16));
        }
        return data;
    }

    public static String readCertFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (InputStream is = CommonUtils.class.getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                throw new IllegalArgumentException("证书文件未找到: " + filename);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

        } catch (IOException e) {
            throw new RuntimeException("读取证书文件失败: " + filename, e);
        }
        return content.toString();
    }
}
