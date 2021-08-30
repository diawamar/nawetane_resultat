package sun.saq.exception

import grails.converters.JSON

class ErreurController {
    def index() {
        if (request.exception == null) {
            render([
                    responseCode: 500,
                    cause       : [
                            code   : 5000,
                            message: "Une erreur inconnue est survenue lors du traitement"
                    ]
            ] as JSON)
            return
        }
        def exception = request.exception.cause?.cause
        if (exception instanceof SaqException) {
            render model: [saqException: exception], view: 'index'
        } else {
            render([
                    responseCode: 500,
                    cause       : [
                            code     : 5000,
                            message  : "Une erreur inconnue est survenue lors du traitement",
                            exception: exception
                    ]
            ] as JSON)
        }
    }
}
