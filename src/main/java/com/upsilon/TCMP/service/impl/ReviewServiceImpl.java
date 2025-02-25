package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.ReviewDTO;
import com.upsilon.TCMP.entity.Review;
import com.upsilon.TCMP.entity.Session;
import com.upsilon.TCMP.repository.ReviewRepository;
import com.upsilon.TCMP.repository.SessionRepository;
import com.upsilon.TCMP.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, SessionRepository sessionRepository) {
        this.reviewRepository = reviewRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public ReviewDTO createReview(Integer sessionId, Integer rating, String comment) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

        Review review = new Review();
        review.setSession(session);
        review.setStudent(session.getStudent());
        review.setTutor(session.getTutor());
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    @Override
    public ReviewDTO getReviewById(Integer id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        return convertToDTO(review);
    }

    @Override
    public List<ReviewDTO> getReviewsByTutorId(Integer tutorId) {
        return reviewRepository.findByTutorId(tutorId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsByStudentId(Integer studentId) {
        return reviewRepository.getStudentReviews(studentId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRatingForTutor(Integer tutorId) {
        return reviewRepository.calculateAverageRating(tutorId);
    }

    @Override
    public Long getTotalReviewsForTutor(Integer tutorId) {
        return reviewRepository.countByTutorId(tutorId);
    }

    @Override
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public Map<Integer, Double> getStudentTutorRatingsDistribution(Integer studentId) {
        List<Object[]> results = reviewRepository.getStudentTutorRatingsDistribution(studentId);
        return results.stream()
            .collect(Collectors.toMap(
                result -> ((Number) result[0]).intValue(),
                result -> (Double) result[1]
            ));
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setSessionId(review.getSession().getId());
        dto.setStudentId(review.getStudent().getId());
        dto.setStudentName(review.getStudent().getUser().getFullName());
        dto.setTutorId(review.getTutor().getId());
        dto.setTutorName(review.getTutor().getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}