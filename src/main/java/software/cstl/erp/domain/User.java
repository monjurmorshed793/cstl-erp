package software.cstl.erp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.cstl.erp.config.Constants;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="cstl_user")
@Getter @Setter @EqualsAndHashCode @ToString
public class User extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min=1, max=50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash",length = 60, nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;
    private String imageUrl;

    @Size(max = 20)
    @Column(length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name="reset_key", length = 20)
    @JsonIgnore
    private String resetKey;
    private Instant resetDate = null;
    private Set<Authority> authorities = new HashSet<>();

}
