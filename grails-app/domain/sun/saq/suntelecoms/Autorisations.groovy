package sun.saq.suntelecoms

import grails.plugins.orm.auditable.Auditable
import sun.saq.security.Role

class Autorisations implements Auditable{

    Date createdAt = new Date()

    //audit-trail fields
    String userCreate
    Date dateCreated
    String userUpdate
    Date lastUpdated

    static belongsTo = [privilege: Privileges, role: Role]

    static constraints = {
    }
}
