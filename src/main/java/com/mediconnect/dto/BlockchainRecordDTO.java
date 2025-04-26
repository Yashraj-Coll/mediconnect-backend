package com.mediconnect.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class BlockchainRecordDTO {
    
    public Long getMedicalRecordId() {
		return medicalRecordId;
	}

	public void setMedicalRecordId(Long medicalRecordId) {
		this.medicalRecordId = medicalRecordId;
	}

	public String getRecordHash() {
		return recordHash;
	}

	public void setRecordHash(String recordHash) {
		this.recordHash = recordHash;
	}

	public String getTransactionHash() {
		return transactionHash;
	}

	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}

	public Long getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(Long blockNumber) {
		this.blockNumber = blockNumber;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public Map<String, String> getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(Map<String, String> accessRights) {
		this.accessRights = accessRights;
	}

	private Long medicalRecordId;
    
    private String recordHash;
    
    private String transactionHash;
    
    private Long blockNumber;
    
    private LocalDateTime timestamp;
    
    private String ownerId;
    
    private String creatorId;
    
    private Map<String, String> accessRights;
}