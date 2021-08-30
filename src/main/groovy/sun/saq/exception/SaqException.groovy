package sun.saq.exception

import sun.saq.constante.TypeException
import org.grails.datastore.gorm.GormEntity
import org.springframework.validation.Errors

class SaqException extends RuntimeException {
    Exception exception
    GormEntity entity
    Errors errors
    String code
    String message
    String debugMessage

    SaqException(String code, String message, String debugMessage) {
        super(message)
        this.code = code
        this.message = message
        this.debugMessage = debugMessage
    }

    SaqException(String code, String message) {
        super(message)
        this.code = code
        this.message = message
        this.debugMessage = message
    }

    SaqException(String message, Throwable cause) {
        super(message, cause)
        this.message = message
        this.debugMessage = message
    }

    SaqException(String code, Exception e) {
        super(e.message, e)
        this.code = code
    }

    SaqException(String code, Errors errors,GormEntity entity) {
        super(ExceptionUtils.errorsAsTabList(errors).isEmpty() ? code : ExceptionUtils.errorsAsTabList(errors).first())
        this.errors = errors
        this.code = code
        this.entity = entity
    }

    SaqException(TypeException typeException, Errors errors, GormEntity entity) {
        super(ExceptionUtils.errorsAsTabList(errors).isEmpty() ? typeException.defaultMessage : ExceptionUtils.errorsAsTabList(errors).first())
        this.errors = errors
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = typeException.defaultMessage
        this.entity = entity
    }

    SaqException(TypeException typeException, Errors errors, GormEntity entity, String customMessage) {
        super(ExceptionUtils.errorsAsTabList(errors).isEmpty() ? customMessage : ExceptionUtils.errorsAsTabList(errors).first())
        this.errors = errors
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = customMessage
        this.entity = entity
    }

    SaqException(TypeException typeException, Exception exception, GormEntity entity, String customMessage) {
        super(customMessage)
        this.exception = exception
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = customMessage
        this.entity = entity
    }

    SaqException(TypeException typeException, Exception exception, GormEntity entity) {
        super(exception)
        this.exception = exception
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = typeException.defaultMessage
        this.entity = entity
    }

    SaqException(TypeException typeException, GormEntity entity) {
        super(typeException.defaultMessage)
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = typeException.defaultMessage
        this.entity = entity
    }

    SaqException(TypeException typeException, GormEntity entity, String customMessage) {
        super(customMessage)
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = customMessage
        this.entity = entity
    }

    SaqException(TypeException typeException, String customMessage) {
        super(customMessage)
        this.code = typeException.code
        this.debugMessage = typeException.defaultMessage
        this.message = customMessage
    }


}
