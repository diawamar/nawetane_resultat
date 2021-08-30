package sun.saq.security


import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import sun.saq.suntelecoms.RestfulController

import java.time.temporal.ChronoUnit

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.OK

class UserController extends RestfulController<User> {
    def springSecurityService
    def utilsService
    static responseFormats = ['json', 'xml']

    UserController() {
        super(User)
    }

    @org.springframework.transaction.annotation.Transactional
    def save(User user) {
        if (user == null) {
            render status: HttpStatus.NOT_FOUND
            return
        }
        try {
            User.findAll().each {
                if (it.username.equals(user.username)) {
                    throw new Exception("Le username doit être unique")
                }
                if (it.telephone.equals(user.telephone)) {
                    throw new Exception("Le numéro de téléphone doit être unique")

                }
                if (it.email.equals(user.email)) {
                    throw new Exception("L'email doit être unique")
                }
            }

            user.enabled = false
            User.withTransaction { status ->
                if (!user.save(flush: true)) {
                    status.setRollbackOnly()
                    throw new Exception("Une erreur est survenue lors de l'enregistrement.")
                }
            }
          //TODO: Envoyer un mail a l'utilisateur avec le mot de passe par defaut
            respond data: user, responseCode: OK.value()


        } catch (Exception e) {
            e.printStackTrace()
            log.error(JsonOutput.toJson([method: "saveUtilisateur", message: "${e.getMessage()}"]))
            render model: [errors: [e.message]], template: '/errors/custom'
        }
    }

    def forgetPassword() {
        def email = request.JSON.email as String
        def user = User.findByEmail(email)
        if (user == null) {
            render status: HttpStatus.NOT_FOUND
            return
        }

        String token = RandomStringUtils.randomNumeric(6)
        def now = new Date()
        def expiry = Date.from(now.toInstant().plus(1, ChronoUnit.DAYS))

        def userToken = new UserResetToken(user: user, token: token, expiryDate: expiry)
        userToken.save()
        if (userToken.hasErrors()) {
            render status: INTERNAL_SERVER_ERROR
        }

        //sending mail
        try {
       // TODO: Envoyer un mail pour reinitialisation de mot de passe
        } catch (Exception ex) {
            render model: [exception: ex], template: '/errors/exception', status: BAD_REQUEST
        }
        render model: ['message': "Veuillez consulter votre mail pour la réinitialisation de votre mot de passe !"], view: 'resetPassword'
    }

    @Transactional
    def resetPassword() {
        def errors = new ArrayList<String>()

        def token = request.JSON.token as String
        def newPassword = request.JSON.newPassword as String
        def confirmPassword = request.JSON.confirmPassword as String

        def passToken = UserResetToken.findByToken(token)

        if (passToken == null) {
            errors.add("Le jeton est invalide")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }

        if (passToken.used) {
            errors.add("Le jeton est déjà utilisé")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }

        Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate()
                .getTime() - cal.getTime()
                .getTime()) <= 0) {
            errors.add("Le jeton  est expire !")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }

        if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            errors.add("Le mot de passe et sa confirmation ne sont pas identiques ")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }
        try {
            def newUser = passToken.getUser()
            if (newUser.hasErrors()) {
                render status: INTERNAL_SERVER_ERROR
                return
            }
            newUser.password = "${newPassword}"
            newUser = newUser.save(true)
            passToken.used = true
            passToken.save(true)
            render model: ['message': "Mot de passe réinitialise avec succes!"], view: 'resetPassword'

        } catch (Exception exception) {
            render model: [errors: [exception.message]], template: '/errors/custom'
        }

    }

    @Transactional
    def changePassword() {
        def errors = new ArrayList<String>()
        def oldPassword = request.JSON.oldPassword as String
        def newPassword = request.JSON.newPassword as String
        def confirmPassword = request.JSON.confirmPassword as String
        def username = springSecurityService.getPrincipal()?.username
        User user = User.findByUsername(username)
        if (user == null) {
            errors.add("Vous devez vous connecter pour pour changer de mot de passe")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }
        if (newPassword == "" || confirmPassword == "" || oldPassword == "") {
            errors.add("Les arguments sont obligatoires")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }
        if (!springSecurityService.passwordEncoder.matches(oldPassword, user.getPassword())) {
            errors.add("Votre ancien mot de passe est incorrect")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }
        if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            errors.add("Le mot de passe et sa confirmation ne sont pas identiques! ")
            render model: ['errors': errors], template: "/errors/custom"
            return
        }

        if (!errors.isEmpty()) {
            render model: [errors: errors], template: '/errors/custom'
            return
        }

        try {
            if (user.hasErrors()) {
                render status: INTERNAL_SERVER_ERROR
                return
            }
            user.password = "${newPassword}"
            user = user.save(true)
            render model: ['message': "Mot de passe modifié avec succes!"], view: 'resetPassword'

        } catch (Exception exception) {
            render model: [errors: [exception.message]], template: '/errors/custom'
        }

    }

    def getUserByUsername() {
        def errors = new ArrayList<String>()
        def username = request.JSON.username as String
        if (StringUtils.isEmpty(username)) {
            errors.add("Veuilllez specifier nom de l'utilisateur")
        }
        if (!errors.isEmpty()) {
            render model: [errors: errors], template: '/errors/errors'
            return
        }
        try {
            def user = utilsService.getUserByUsername(username)
            respond data: user, responseCode: OK.value()

        }
        catch (Exception ex) {
            render model: [errors: [ex.message]], template: '/errors/errors'
        }
    }

    //   :Bloquer ou Debloquer un Utilisateur
    def bloquerDebloquerUtilisateur() {
        def errors = new ArrayList<String>()
        def userId = request.JSON.userId
        if (userId == null) {
            errors.add("Veuilllez spécifier l'utilisateur")
        }
        if (!errors.isEmpty()) {
            render model: [errors: errors], template: '/errors/custom'
            return
        }
        try {
            def utilisateur = utilsService.bloquerDebloquerUser(userId as Long)
            respond data: utilisateur, responseCode: OK.value()

        }
        catch (Exception ex) {
            render model: [errors: [ex.message]], template: '/errors/errors'
        }
    }

}
