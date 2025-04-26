package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;


import com.mediconnect.blockchain.MedicalRecordContract;
import com.mediconnect.dto.BlockchainRecordDTO;
import com.mediconnect.exception.BlockchainException;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.MedicalRecordBlockchain;
import com.mediconnect.repository.MedicalRecordBlockchainRepository;
import com.mediconnect.repository.MedicalRecordRepository;

@Service
public class BlockchainMedicalRecordService {

    @Value("${blockchain.node-url}")
    private String blockchainNodeUrl;
    
    public String getBlockchainNodeUrl() {
		return blockchainNodeUrl;
	}

	public void setBlockchainNodeUrl(String blockchainNodeUrl) {
		this.blockchainNodeUrl = blockchainNodeUrl;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public MedicalRecordRepository getMedicalRecordRepository() {
		return medicalRecordRepository;
	}

	public void setMedicalRecordRepository(MedicalRecordRepository medicalRecordRepository) {
		this.medicalRecordRepository = medicalRecordRepository;
	}

	public MedicalRecordBlockchainRepository getBlockchainRepository() {
		return blockchainRepository;
	}

	public void setBlockchainRepository(MedicalRecordBlockchainRepository blockchainRepository) {
		this.blockchainRepository = blockchainRepository;
	}

	public Web3j getWeb3j() {
		return web3j;
	}

	public void setWeb3j(Web3j web3j) {
		this.web3j = web3j;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public MedicalRecordContract getContract() {
		return contract;
	}

	public void setContract(MedicalRecordContract contract) {
		this.contract = contract;
	}

	@Value("${blockchain.contract-address}")
    private String contractAddress;
    
    @Value("${blockchain.private-key}")
    private String privateKey;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private MedicalRecordBlockchainRepository blockchainRepository;
    
    private Web3j web3j;
    private Credentials credentials;
    private MedicalRecordContract contract;
    
    /**
     * Initialize blockchain connection
     */
    @PostConstruct
    public void init() {
        try {
            web3j = Web3j.build(new HttpService(blockchainNodeUrl));
            credentials = Credentials.create(privateKey);
            contract = MedicalRecordContract.load(
                    contractAddress, web3j, credentials, 
                    MedicalRecordContract.GAS_PRICE, MedicalRecordContract.GAS_LIMIT);
        } catch (Exception e) {
            throw new BlockchainException("Failed to initialize blockchain connection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Store medical record hash on blockchain
     */
    @Transactional
    public MedicalRecordBlockchain storeRecordOnBlockchain(Long medicalRecordId) {
        try {
            // Retrieve medical record
            MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                    .orElseThrow(() -> new BlockchainException("Medical record not found"));
            
            // Check if record already exists on blockchain
            Optional<MedicalRecordBlockchain> existingRecord = 
                    blockchainRepository.findByMedicalRecordId(medicalRecordId);
            
            if (existingRecord.isPresent()) {
                throw new BlockchainException("Record already exists on blockchain");
            }
            
            // Generate record hash - in real implementation, this would be more sophisticated
            String recordHash = generateRecordHash(medicalRecord);
            
            // Store hash on blockchain
            String transactionHash = contract.storeRecordHash(
                    medicalRecordId.toString(), 
                    recordHash, 
                    medicalRecord.getPatient().getId().toString(),
                    medicalRecord.getDoctor().getId().toString()
            ).send().getTransactionHash();
            
            // Get block number
            long blockNumber = web3j.ethGetTransactionReceipt(transactionHash)
                    .send().getTransactionReceipt().get().getBlockNumber().longValue();
            
            // Save blockchain record reference in database
            MedicalRecordBlockchain blockchainRecord = new MedicalRecordBlockchain();
            blockchainRecord.setMedicalRecord(medicalRecord);
            blockchainRecord.setRecordHash(recordHash);
            blockchainRecord.setTransactionHash(transactionHash);
            blockchainRecord.setBlockNumber(blockNumber);
            blockchainRecord.setTimestamp(LocalDateTime.now());
            
            return blockchainRepository.save(blockchainRecord);
            
        } catch (Exception e) {
            throw new BlockchainException("Failed to store record on blockchain: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify medical record integrity against blockchain
     */
    public boolean verifyRecordIntegrity(Long medicalRecordId) {
        try {
            // Retrieve medical record
            MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                    .orElseThrow(() -> new BlockchainException("Medical record not found"));
            
            // Check if record exists on blockchain
            Optional<MedicalRecordBlockchain> blockchainRecord = 
                    blockchainRepository.findByMedicalRecordId(medicalRecordId);
            
            if (!blockchainRecord.isPresent()) {
                throw new BlockchainException("Record not found on blockchain");
            }
            
            // Generate current hash of record
            String currentHash = generateRecordHash(medicalRecord);
            
            // Verify against stored hash
            String storedHash = blockchainRecord.get().getRecordHash();
            
            return currentHash.equals(storedHash);
            
        } catch (Exception e) {
            throw new BlockchainException("Failed to verify record integrity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Share access to a medical record with a new entity (doctor, facility, etc.)
     */
    @SuppressWarnings("unused")
	@Transactional
    public void shareRecordAccess(Long medicalRecordId, String recipientId, String accessLevel) {
        try {
            // Check if record exists on blockchain
            Optional<MedicalRecordBlockchain> blockchainRecord = 
                    blockchainRepository.findByMedicalRecordId(medicalRecordId);
            
            if (!blockchainRecord.isPresent()) {
                throw new BlockchainException("Record not found on blockchain");
            }
            
            // Grant access on blockchain
            String transactionHash = contract.grantAccess(
                    medicalRecordId.toString(), 
                    recipientId,
                    accessLevel
            ).send().getTransactionHash();
            
            // Update blockchain record with new access info
            MedicalRecordBlockchain record = blockchainRecord.get();
            Map<String, String> accessRights = record.getAccessRights();
            if (accessRights == null) {
                accessRights = new HashMap<>();
            }
            
            accessRights.put(recipientId, accessLevel);
            record.setAccessRights(accessRights);
            
            blockchainRepository.save(record);
            
        } catch (Exception e) {
            throw new BlockchainException("Failed to share record access: " + e.getMessage(), e);
        }
    }
    
    /**
     * Revoke access to a medical record
     */
    @Transactional
    public void revokeRecordAccess(Long medicalRecordId, String recipientId) {
        try {
            // Check if record exists on blockchain
            Optional<MedicalRecordBlockchain> blockchainRecord = 
                    blockchainRepository.findByMedicalRecordId(medicalRecordId);
            
            if (!blockchainRecord.isPresent()) {
                throw new BlockchainException("Record not found on blockchain");
            }
            
            // Revoke access on blockchain
            @SuppressWarnings("unused")
			String transactionHash = contract.revokeAccess(
                    medicalRecordId.toString(), 
                    recipientId
            ).send().getTransactionHash();
            
            // Update blockchain record
            MedicalRecordBlockchain record = blockchainRecord.get();
            Map<String, String> accessRights = record.getAccessRights();
            if (accessRights != null) {
                accessRights.remove(recipientId);
                record.setAccessRights(accessRights);
            }
            
            blockchainRepository.save(record);
            
        } catch (Exception e) {
            throw new BlockchainException("Failed to revoke record access: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get blockchain record info
     */
    public BlockchainRecordDTO getBlockchainRecordInfo(Long medicalRecordId) {
        try {
            // Check if record exists on blockchain
            MedicalRecordBlockchain blockchainRecord = blockchainRepository.findByMedicalRecordId(medicalRecordId)
                    .orElseThrow(() -> new BlockchainException("Record not found on blockchain"));
            
            // Get record details from blockchain
            MedicalRecordContract.RecordInfo recordInfo = contract.getRecordInfo(
                    medicalRecordId.toString()
            ).send();
            
            BlockchainRecordDTO dto = new BlockchainRecordDTO();
            dto.setMedicalRecordId(medicalRecordId);
            dto.setRecordHash(blockchainRecord.getRecordHash());
            dto.setTransactionHash(blockchainRecord.getTransactionHash());
            dto.setBlockNumber(blockchainRecord.getBlockNumber());
            dto.setTimestamp(blockchainRecord.getTimestamp());
            dto.setOwnerId(recordInfo.ownerId);
            dto.setCreatorId(recordInfo.creatorId);
            dto.setAccessRights(blockchainRecord.getAccessRights());
            
            return dto;
            
        } catch (Exception e) {
            throw new BlockchainException("Failed to get blockchain record info: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate hash of medical record
     */
    private String generateRecordHash(MedicalRecord record) {
        // In a real implementation, this would use a proper cryptographic hash function
        // with all relevant record fields included in the hash
        StringBuilder data = new StringBuilder();
        data.append(record.getId())
            .append(record.getPatient().getId())
            .append(record.getDoctor().getId())
            .append(record.getRecordDate())
            .append(record.getDiagnosis())
            .append(record.getTreatment())
            .append(record.getSymptoms())
            .append(record.getNotes())
            .append(record.getUpdatedAt());
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("Hash algorithm not available", e);
        }
    }
    
    /**
     * Convert byte array to hex string
     */
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}