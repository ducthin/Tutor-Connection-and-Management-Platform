package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    
    List<Subject> findByCategory(String category);
    
    List<Subject> findByNameIn(List<String> names);
    
    @Query("SELECT DISTINCT s.category FROM Subject s ORDER BY s.category")
    List<String> findAllCategories();
    
    @Query("SELECT s FROM Subject s " +
           "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Subject> searchSubjects(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Subject s " +
           "WHERE EXISTS (SELECT 1 FROM TutorSubject ts WHERE ts.subject = s " +
           "AND ts.tutor.isVerified = true)")
    List<Subject> findSubjectsWithVerifiedTutors();
    
    @Query("SELECT s FROM Subject s " +
           "WHERE EXISTS (SELECT 1 FROM TutorSubject ts WHERE ts.subject = s " +
           "GROUP BY ts.subject " +
           "HAVING COUNT(ts) >= :minTutors)")
    List<Subject> findSubjectsWithMinimumTutors(@Param("minTutors") Long minTutors);
    
    @Query("SELECT s.name as subject, COUNT(ts) as tutorCount " +
           "FROM Subject s LEFT JOIN TutorSubject ts ON s = ts.subject " +
           "WHERE ts.tutor.isVerified = true " +
           "GROUP BY s.name")
    List<Object[]> getTutorCountsBySubject();
    
    @Query("SELECT s.name as subject, AVG(ts.hourlyRate) as avgRate " +
           "FROM Subject s LEFT JOIN TutorSubject ts ON s = ts.subject " +
           "WHERE ts.tutor.isVerified = true " +
           "GROUP BY s.name")
    List<Object[]> getAverageRatesBySubject();
    
    @Query("SELECT COUNT(DISTINCT ts.tutor) " +
           "FROM TutorSubject ts " +
           "WHERE ts.subject.id = :subjectId " +
           "AND ts.tutor.isVerified = true")
    Long countVerifiedTutorsBySubject(@Param("subjectId") Integer subjectId);
    
    boolean existsByNameAndCategory(String name, String category);
}