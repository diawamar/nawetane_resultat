package sun.saq.exception


import org.apache.commons.lang3.StringUtils
import org.grails.datastore.gorm.GormEntity
import sun.saq.constante.TypeException

import static sun.saq.constante.TypeException.*

/**
 * @author <a href="https://github.com/6ccattleya">Bamba CISSE</a>
 */
class SaqTryCatch {

/**
 * @param entity l'entité à vérifier
 * @param typeException le type d'exception à vérifier
 * @param customMessage le message personnalisé
 * @return l'objet à verifier ou L'exception
 */
    static def check(GormEntity entity, TypeException typeException, String customMessage = "") {
        if (StringUtils.isEmpty(customMessage)) {
            customMessage = typeException.defaultMessage
        }
        if (entity == null) {
            throw new SaqException(NULL, customMessage)
        }
        switch (typeException) {
            case SAVE:
                entity.validate()
                if (entity.hasErrors()) {
                    throw new SaqException(CONTRAINTE, entity.errors, entity, customMessage)
                } else {
                    try {
                        if (entity.save() == null) {
                            throw new SaqException(SAVE, customMessage)
                        }
                    } catch (Exception e) {
                        throw new SaqException(SAVE, e, entity, customMessage)
                    }
                }
                return entity
                break
            case UPDATE:
                entity.validate()
                if (entity.hasErrors()) {
                    throw new SaqException(CONTRAINTE, entity.errors, entity, customMessage)
                } else {
                    try {
                        if (entity.save() == null) {
                            throw new SaqException(UPDATE, customMessage)
                        }
                    } catch (Exception e) {
                        throw new SaqException(UPDATE, e, entity, customMessage)
                    }
                }
                return entity
                break
            case DELETE:
                entity.validate()
                if (entity.hasErrors()) {
                    throw new SaqException(CONTRAINTE, entity.errors, entity, customMessage)
                } else {
                    try {
                        entity.delete()
                    } catch (Exception e) {
                        throw new SaqException(DELETE, e, entity, customMessage)
                    }
                }
                break
            case FIND:
                entity.validate()
                if (entity.hasErrors()) {
                    throw new SaqException(NOTFOUND, entity.errors, entity, customMessage)
                } else {
                    if (entity == null) {
                        throw new SaqException(NOTFOUND, customMessage)
                    }
                    return entity
                }
                break
        }
    }
}
