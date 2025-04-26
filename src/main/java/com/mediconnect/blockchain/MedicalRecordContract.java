package com.mediconnect.blockchain;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.DynamicStruct;  // Changed import from Struct to DynamicStruct
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;  // Added import for StaticGasProvider

/**
 * This class is an auto-generated wrapper for the MedicalRecord Solidity smart contract.
 * It provides Java bindings for interacting with the blockchain.
 */
@SuppressWarnings("rawtypes")
public class MedicalRecordContract extends Contract {
    
    public static final String BINARY = "0x60806040..."; // Contract bytecode would go here
    
    public static final String FUNC_STORERECORDHASH = "storeRecordHash";
    public static final String FUNC_GETRECORDINFO = "getRecordInfo";
    public static final String FUNC_GRANTACCESS = "grantAccess";
    public static final String FUNC_REVOKEACCESS = "revokeAccess";
    
    public static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_500_000L);
    
    /**
     * Record info struct returned by getRecordInfo
     */
    public static class RecordInfo extends DynamicStruct {  // Changed from Struct to DynamicStruct
        public String recordId;
        public String recordHash;
        public String ownerId;
        public String creatorId;
        public BigInteger timestamp;
        
        public RecordInfo(String recordId, String recordHash, String ownerId, String creatorId, BigInteger timestamp) {
            super(
                new Utf8String(recordId),
                new Utf8String(recordHash),
                new Utf8String(ownerId),
                new Utf8String(creatorId),
                new org.web3j.abi.datatypes.generated.Uint256(timestamp)
            );
            this.recordId = recordId;
            this.recordHash = recordHash;
            this.ownerId = ownerId;
            this.creatorId = creatorId;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Constructor for deploying the contract
     */
    protected MedicalRecordContract(String contractAddress, Web3j web3j, Credentials credentials, 
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
    }
    
    protected MedicalRecordContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, 
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, new StaticGasProvider(gasPrice, gasLimit));
    }
    
    protected MedicalRecordContract(String contractAddress, Web3j web3j, Credentials credentials, 
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }
    
    protected MedicalRecordContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, 
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }
    
    /**
     * Factory method for loading/deploying contract
     */
    public static MedicalRecordContract load(String contractAddress, Web3j web3j, Credentials credentials, 
            BigInteger gasPrice, BigInteger gasLimit) {
        return new MedicalRecordContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }
    
    public static MedicalRecordContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, 
            BigInteger gasPrice, BigInteger gasLimit) {
        return new MedicalRecordContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
    
    /**
     * Store a medical record hash on the blockchain
     */
    public RemoteFunctionCall<TransactionReceipt> storeRecordHash(
            String recordId, String recordHash, String ownerId, String creatorId) {
        Function function = new Function(
                FUNC_STORERECORDHASH, 
                Arrays.<Type>asList(
                        new Utf8String(recordId),
                        new Utf8String(recordHash),
                        new Utf8String(ownerId),
                        new Utf8String(creatorId)), 
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
    
    /**
     * Get information about a medical record
     */
    public RemoteFunctionCall<RecordInfo> getRecordInfo(String recordId) {
        Function function = new Function(
                FUNC_GETRECORDINFO, 
                Arrays.<Type>asList(new Utf8String(recordId)),
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Utf8String>() {},
                        new TypeReference<Utf8String>() {},
                        new TypeReference<Utf8String>() {},
                        new TypeReference<Utf8String>() {},
                        new TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, RecordInfo.class);
    }
    
    /**
     * Grant access to a medical record
     */
    public RemoteFunctionCall<TransactionReceipt> grantAccess(String recordId, String recipientId, String accessLevel) {
        Function function = new Function(
                FUNC_GRANTACCESS, 
                Arrays.<Type>asList(
                        new Utf8String(recordId),
                        new Utf8String(recipientId),
                        new Utf8String(accessLevel)), 
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
    
    /**
     * Revoke access to a medical record
     */
    public RemoteFunctionCall<TransactionReceipt> revokeAccess(String recordId, String recipientId) {
        Function function = new Function(
                FUNC_REVOKEACCESS, 
                Arrays.<Type>asList(
                        new Utf8String(recordId),
                        new Utf8String(recipientId)), 
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
}