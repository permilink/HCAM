package org.cqupt.crypto.hcaa.util;

import java.util.Map;

public class AdapterConfig {
    private String blockchainType;
    private String chaincodeId;
    private String contract;
    private Map<String, Operation> operations;

    public static class Operation {
        private String functionName;
        private ParameterMapping[] parameterMapping;

        public static class ParameterMapping {
            private String source;
            private String type;

            // Getters and setters
            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }

        // Getters and setters
        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }

        public ParameterMapping[] getParameterMapping() {
            return parameterMapping;
        }

        public void setParameterMapping(ParameterMapping[] parameterMapping) {
            this.parameterMapping = parameterMapping;
        }
    }

    // Getters and setters
    public String getBlockchainType() {
        return blockchainType;
    }

    public void setBlockchainType(String blockchainType) {
        this.blockchainType = blockchainType;
    }

    public String getChaincodeId() {
        return chaincodeId;
    }

    public void setChaincodeId(String chaincodeId) {
        this.chaincodeId = chaincodeId;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public Map<String, Operation> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, Operation> operations) {
        this.operations = operations;
    }

}
