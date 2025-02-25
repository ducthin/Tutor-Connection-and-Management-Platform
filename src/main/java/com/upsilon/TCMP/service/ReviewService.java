package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.ReviewDTO;
import java.util.List;
import java.util.Map;

public interface ReviewService {
    ReviewDTO createReview(Integer sessionId, Integer rating, String comment);
    ReviewDTO getReviewById(Integer id);
    List<ReviewDTO> getReviewsByTutorId(Integer tutorId);
    List<ReviewDTO> getReviewsByStudentId(Integer studentId);
    Double getAverageRatingForTutor(Integer tutorId);
    Long getTotalReviewsForTutor(Integer tutorId);
    void deleteReview(Integer id);
    
    /**
     * Get distribution of average ratings given by a student to their tutors
     * @param studentId the ID of the student
     * @return Map with tutor IDs as keys and their average ratings as values
     */
    Map<Integer, Double> getStudentTutorRatingsDistribution(Integer studentId);
}