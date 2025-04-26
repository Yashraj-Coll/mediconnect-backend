package com.mediconnect.dto;

import java.util.List;

import lombok.Data;

@Data
public class TreatmentRecommendationDTO {
    
    private Long recommendationId;
    
    private String overview;
    
    private String personalizationFactors;
    
    private String lifestyleRecommendations;
    
    private String followUpRecommendations;
    
    public Long getRecommendationId() {
		return recommendationId;
	}

	public void setRecommendationId(Long recommendationId) {
		this.recommendationId = recommendationId;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public String getPersonalizationFactors() {
		return personalizationFactors;
	}

	public void setPersonalizationFactors(String personalizationFactors) {
		this.personalizationFactors = personalizationFactors;
	}

	public String getLifestyleRecommendations() {
		return lifestyleRecommendations;
	}

	public void setLifestyleRecommendations(String lifestyleRecommendations) {
		this.lifestyleRecommendations = lifestyleRecommendations;
	}

	public String getFollowUpRecommendations() {
		return followUpRecommendations;
	}

	public void setFollowUpRecommendations(String followUpRecommendations) {
		this.followUpRecommendations = followUpRecommendations;
	}

	public List<TreatmentOptionDTO> getTreatmentOptions() {
		return treatmentOptions;
	}

	public void setTreatmentOptions(List<TreatmentOptionDTO> treatmentOptions) {
		this.treatmentOptions = treatmentOptions;
	}

	private List<TreatmentOptionDTO> treatmentOptions;
}