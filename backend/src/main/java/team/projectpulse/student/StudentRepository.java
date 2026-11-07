package team.projectpulse.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    // Find all students in a section
    List<Student> findBySectionSectionId(Integer sectionId);

    List<Student> findByTeamTeamId(Integer teamId);

    @Query("select s from Student s where s.id = :studentId and s.section.course.courseAdmin.id = :callerId")
    Optional<Student> findByIdAndOwningCourseAdmin(@Param("studentId") Integer studentId,
                                                    @Param("callerId") Integer callerId);

    @Query("select s from Student s join s.section.instructors instructor " +
           "where s.id = :studentId and instructor.id = :callerId")
    Optional<Student> findByIdAndAssignedInstructor(@Param("studentId") Integer studentId,
                                                      @Param("callerId") Integer callerId);
}
