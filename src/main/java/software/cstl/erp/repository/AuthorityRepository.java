package software.cstl.erp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import software.cstl.erp.domain.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
