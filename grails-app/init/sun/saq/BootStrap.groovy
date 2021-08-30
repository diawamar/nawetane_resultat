package sun.saq

import grails.converters.JSON
import sun.saq.exception.SaqException
import sun.saq.security.Role
import sun.saq.security.User
import sun.saq.security.UserRole
import sun.saq.suntelecoms.Helper

import static sun.saq.exception.ExceptionUtils.errorsAsTabList
import static sun.saq.lib.utilities.Utilities.*

class BootStrap {

    def init = { servletContext ->
        registrerSaqException()
        testLib()
        //bootstrap()
    }
    def destroy = {
    }

    def bootstrap() {
        if (Helper.count == 0) {
            def roleAdmin = new Role(authority: 'ROLE_ADMIN').save()
            def roleUser = new Role(authority: 'ROLE_USER').save()

            def userAdmin = new User(username: 'admin', password: 'admin', enabled: true, email: 'admin-airtimes@yopmail.com', firstName: 'SunTelecoms', lastName: 'SunTelecoms', telephone: '337689878')
            userAdmin.save()
            UserRole.create(userAdmin, roleAdmin)

            new Helper(hasBootstrapped: true).save()
        }
    }

    /**
     * method for register SaqException domain for JSON rendering
     * @return the marshaller object
     */
    def registrerSaqException() {
        JSON.registerObjectMarshaller(SaqException) { SaqException it ->
            def output = [:]
            output['code'] = it.code
            output['message'] = it.message
            output['debugMessage'] = it.debugMessage

            if (it.errors != null) {
                if (!it.errors.allErrors.isEmpty()) {
                    output['erreurs'] = errorsAsTabList(it.errors)
                }
            }
            if (it.exception != null) {
                output['exception'] = it.exception.message
            }
            return output
        }
    }

    def testLib() {
        println sendSms("61dd0d143189ced60e2202c9f36cc5294a1415b52f61ea60c4dca1eeb498f1ac", "https://dev.afripayway.com/api/common/v1/sendSMS", "221776210191", "APGSA", "Salut Bamba")
    }
}
