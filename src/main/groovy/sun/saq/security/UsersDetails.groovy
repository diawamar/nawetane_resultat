package sun.saq.security

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

class UsersDetails extends GrailsUser {
    String firstName
    String lastName
    String email
    Object  autorisations

    UsersDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<GrantedAuthority> authorities, long id, String firstName, String lastName, String email,Object autorisations) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.autorisations = autorisations
    }
}
