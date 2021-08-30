package sun.saq

import grails.gorm.transactions.Transactional
import sun.saq.security.Role
import sun.saq.security.User
import sun.saq.suntelecoms.Privileges

@Transactional
class UtilsService {

    def getUserByUsername(String username) {
        User utilisateur = User.findByUsername(username)
        if (utilisateur != null) {
            return utilisateur
        } else
            throw new Exception("cet utilisateur n'existe pas!")
    }

    //  :Bloquer ou Debloquer un utilisateur
    def bloquerDebloquerUser(Long userId) {
        def user = User.get(userId)
        if (user == null)
            throw new Exception("Cet utilisateur n'existe pas")
        if (user.enabled) {
            user.enabled = false
            return [message: "Utilisateur  déseactivé avec success", status: user.enabled]
        } else {
            user.enabled = true
            return [message: "Utilisateur activé avec success  ", status: user.enabled]
        }
    }

    def getPrivileges(def data) {
        Role role = Role.get(data?.idRole?.toLong())
        if (!role)
            throw new Exception("Le role est  introuvable")
        if (role != null) {
            def privileges = Privileges.findAll().findAll {
                it.autorisations.findAll {
                    it.role == role
                }
            }
            return privileges

        } else
            throw new Exception("Le role  n'existe pas")
    }

}
