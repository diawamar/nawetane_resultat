import io.swagger.models.SecurityRequirement
import io.swagger.models.Swagger
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.In
import sun.saq.security.AllUserDetailsService
import sun.saq.security.InfosUsersConnected
import sun.saq.security.UserPasswordEncoderListener
import sun.saq.security.UsersAuthentifications

// Place your Spring DSL code here
beans = {
    userPasswordEncoderListener(UserPasswordEncoderListener)
    restAuthenticationFailureHandler(UsersAuthentifications)
    accessTokenJsonRenderer(InfosUsersConnected)
    userDetailsService(AllUserDetailsService)
    swagger(Swagger) {
        securityDefinitions = ["apiKey": new ApiKeyAuthDefinition("authorisation", In.HEADER)]
        security = [new SecurityRequirement().requirement("authorisation")]
    }
}
