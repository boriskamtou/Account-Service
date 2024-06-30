package account.adapters;


import account.entities.Group;
import account.entities.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class UserAdapter implements UserDetails {
    private final Users user;

    public UserAdapter(Users user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user != null) {
            Collection<GrantedAuthority> authorities = new ArrayList<>(user.getGroups().size());
            for (Group userGroup : user.getGroups()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + userGroup.getGroupName().toUpperCase()));
            }
            return authorities;
        }
        return null;
    }

    @Override
    public String getPassword() {
        if (user != null) {
            return user.getPassword();
        } else {
            return null; // Or throw an exception if needed
        }
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
