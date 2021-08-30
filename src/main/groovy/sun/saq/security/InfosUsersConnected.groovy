package sun.saq.security


import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import groovy.json.JsonBuilder
import org.springframework.security.core.GrantedAuthority
import sun.saq.security.Role
import sun.saq.suntelecoms.Privileges

class InfosUsersConnected implements AccessTokenJsonRenderer {
    @Override
    String generateJson(AccessToken accessToken) {
        List map = []
        Map<Role,List<Privileges>> source = accessToken.principal?.autorisations
        source.each {privileges->

            privileges.value.each {
                p2->
                    map.addAll(p2.toJson().privileges)
            }

        }
        Map response = [
                username     : accessToken.principal.username,
                firstName    : accessToken.principal?.firstName,
                lastName     : accessToken.principal?.lastName,
                email        : accessToken.principal?.email,
                access_token : accessToken.accessToken,
                token_type   : "Bearer",
                expires_in   : accessToken.expiration,
                refresh_token: accessToken.refreshToken,
                roles        : accessToken.authorities.collect { GrantedAuthority role -> role.authority },
                autorisations: [[privileges: map]]
        ]

        return new JsonBuilder(data: response).toPrettyString()
    }


}
