package sun.saq.security

class UserResetToken {
    String token
    User user
    Date expiryDate
    Boolean used = false

    static constraints = {
        token unique: ['user']
    }

    static mapping = {
        token type: 'text'

    }
}
