package account.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SortNatural;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String lastname;
    @NotBlank
    @Email(regexp = "^[\\w-\\.]+@acme\\.com$")
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 12)
    private String password;
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Group> groups = new HashSet<>();
    @ElementCollection
    private Set<String> roles;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean active = true;

    @JsonIgnore
    @Column(name = "failed_attempt")
    private int failedAttempt;
    @JsonProperty("roles")
    @SortNatural
    public Set<String> getRoles() {
        return groups.stream()
                .map(group -> "ROLE_" + group.getGroupName())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
