package pm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pm.model.member.Member;
import pm.model.users.Users;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    List<Member> findByProdId(Integer id);

    boolean existsByMemberAndProdId(Users users, int id);

    List<Member> findByMember(Users users);

    @Query(value = "SELECT * FROM members WHERE member = :userId AND role = :role", nativeQuery = true)
    List<Member> findByMemberAndRole(@Param("userId") Integer userId, @Param("role") String role);

    List<Member> findByMember_IdAndRole(int userId, String role);

    List<Member> findByMemberAndProdId(Users users, int id);

    List<Member> findByProdIdAndRole(Integer id, String role);

    Long countByMemberAndRole(Users userId, String string);

    List<Member> findByMemberAndRole(Users users, String role);

    List<Member> findByProdIdAndBranch(Integer id, String branch);

    List<Member> findByProdIdAndBranchAndMember(Integer id, String branch, Users member);

    List<Member> findByProdIdAndBranchAndAssignedBy(Integer id, String branch, int id1);

    @Query(value = "SELECT * FROM members WHERE prod_id = :id AND branch = :branch AND assigned_by = :assign And member =:user_id", nativeQuery = true)
    List<Member> findByProdIdAndBranchAndAssignedBy1(Integer id, String branch, int assign, int user_id);

    @Query(value = "SELECT * FROM members WHERE prod_id = :id AND branch = :branch AND assigned_by = :assign And is_deleted =false", nativeQuery = true)
    List<Member> findByProdIdAndBranchAndAssignedByandisdeletedList(Integer id, String branch, int assign);

    @Query(value = "SELECT COUNT(*) FROM members WHERE member = :userId AND prod_id = :prodId", nativeQuery = true)
    int countByUserIdAndProdId(@Param("userId") int userId, @Param("prodId") int prodId);

    @Query(value = "SELECT u.name FROM members m JOIN users u ON u.id = m.assigned_by WHERE prod_id = :prodId AND member = :memberId", nativeQuery = true)
    String findByMemberAndProdIdAndGetAssignedBy(@Param("memberId") int memberId, @Param("prodId") int prodId);

}
