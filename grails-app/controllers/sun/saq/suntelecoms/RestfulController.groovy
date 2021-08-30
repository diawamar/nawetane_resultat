package sun.saq.suntelecoms

import grails.artefact.Artefact
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.util.GrailsNameUtils
import grails.web.http.HttpHeaders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import sun.saq.helpers.Help
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.GenericValidator
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.http.HttpStatus

import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit

import static org.springframework.http.HttpStatus.*

@Artefact("Controller")
@ReadOnly
class RestfulController<T> {
    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: ["PUT", "POST"], patch: "PATCH", delete: "DELETE", search: "POST"]

    Class<T> resource
    String resourceName
    String resourceClassName
    GrailsApplication grailsApplication
    boolean readOnly

    RestfulController(Class<T> resource) {
        this(resource, false)
    }

    RestfulController(Class<T> resource, boolean readOnly) {
        this.resource = resource
        this.readOnly = readOnly
        resourceClassName = resource.simpleName
        resourceName = GrailsNameUtils.getPropertyName(resource)
    }

    /**
     * Lists all resources up to the given maximum
     *
     * @param max The maximum
     * @return A list of resources
     */
    def index(Integer max) {
        def search = params.search
        params.max = max ?: 10
        def isGlobal = true
        def searchQuery = null
        if (!StringUtils.isEmpty(params.searchQuery)) {
            params.isGlobal = params.isGlobal.equalsIgnoreCase('true')
            isGlobal = params.isGlobal
            searchQuery = params.searchQuery
        }
        if (params.searchQuery == null) {
            params.order = (params.order ?: 'desc')
            params.sort = (params.sort ?: 'createdAt')
            respond listAllResources(params), model: [total : countResources(),
                                                      max   : params.max,
                                                      offset: params.int("offset") ?: 0,
                                                      order : params.order,
                                                      sort  : params.sort,
                                                      search: searchQuery]

        } else {

            try {
                params.order = (params.order ?: 'desc')
                params.sort = (params.sort ?: 'createdAt')
                respond listSearchResources(params), model: [

                        search: searchQuery,
                        total : listSearchResources(params).size(),
                        max   : params.max,
                        offset: params.int("offset") ?: 0,
                        sort  : params.sort,
                        order : params.order
                ]
            } catch (Exception ex) {
                render model: [errors: [ex.message]], template: '/errors/custom'
            }
        }

    }

    /**
     * Lists query resources up to the given maximum
     *
     * @param max The maximum
     * @return A list of resources
     */

    /**
     * Shows a single resource
     * @param id The id of the resource
     * @return The rendered resource or a 404 if it doesn't exist
     */
    def show() {
        respond queryForResource(params.id)
    }

    /**
     * Displays a form to create a new resource
     */
    def create() {
        if (handleReadOnly()) {
            return
        }
        respond createResource()
    }

    /**
     * Saves a resource
     */
    @Transactional
    def save() {
        if (handleReadOnly()) {
            return
        }
        def instance = createResource()
        // instance.setDisplayMe()
        instance.validate()
        if (instance.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond instance.errors, view: 'create' // STATUS CODE 422
            return
        }

        saveResource instance

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [classMessageArg, instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                        grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: instance.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null))
                respond instance, [status: CREATED, view: 'show']
            }
        }
    }

    def edit() {
        if (handleReadOnly()) {
            return
        }
        respond queryForResource(params.id)
    }

    /**
     * Updates a resource for the given id
     * @param id
     */
    @Transactional
    def patch() {
        update()
    }

    /**
     * Updates a resource for the given id
     * @param id
     */
    @Transactional
    def update() {
        if (handleReadOnly()) {
            return
        }

        T instance = queryForResource(params.id)
        if (instance == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        instance.properties = getObjectToBind()
        instance.validate()
        if (instance.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond instance.errors, view: 'edit' // STATUS CODE 422
            return
        }

        updateResource instance
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [classMessageArg, instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                        grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: instance.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null))
                respond instance, [status: CREATED, view: 'show']

            }
        }
    }
    /**
     * Deletes a resource for the given id
     * @param id The id
     */
    @Transactional
    def delete() {
        if (handleReadOnly()) {
            return
        }

        def instance = queryForResource(params.id)
        if (instance == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        deleteResource instance

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [classMessageArg, instance.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT } // NO CONTENT STATUS CODE
        }
    }

    /**
     * handles the request for write methods (create, edit, update, save, delete) when controller is in read only mode
     *
     * @return true if controller is read only
     */
    protected boolean handleReadOnly() {
        if (readOnly) {
            render status: HttpStatus.METHOD_NOT_ALLOWED.value()
            return true
        } else {
            return false
        }
    }


    protected getObjectToBind() {
        request
    }

    /**
     * Queries for a resource for the given id
     *
     * @param id The id
     * @return The resource or null if it doesn't exist
     */
    protected T queryForResource(Serializable id) {
        resource.get(id)
    }

    /**
     * Creates a new instance of the resource for the given parameters
     *
     * @param params The parameters
     * @return The resource instance
     */
    protected T createResource(Map params) {
        resource.newInstance(params)
    }

    /**
     * Creates a new instance of the resource.  If the request
     * contains a body the body will be parsed and used to
     * initialize the new instance, otherwise request parameters
     * will be used to initialized the new instance.
     *
     * @return The resource instance
     */
    protected T createResource() {
        T instance = resource.newInstance()
        bindData instance, getObjectToBind()
        instance
    }

    /**
     * List all of resource based on parameters
     *
     * @return List of resources or empty if it doesn't exist
     */
    protected List<T> listAllResources(Map params) {
        params.remove('controller')
        params.remove('action')
        resource.list(params)
    }

    protected List<T> listAllResources(Map params, List aList) {
        if (aList != null && !aList.isEmpty()) {
            if (StringUtils.isEmpty(params.offset as String)) {
                params.offset = '0'
            }
            int listSize = aList.size()
            int offset = params.offset as Integer
            int max = params.max as Integer
            int start = offset == 0 ? 0 : (max * offset) > listSize ? listSize - max : (max * offset) - 1
            int end = (start + max) > listSize ? listSize : (start + max)
            return aList.subList(start, end)
        }
        params.remove('controller')
        params.remove('action')
        return resource.list(params)
    }

    /**
     * List all of columns to be querying
     *
     * @return List of columns or empty if it doesn't exist
     */
    protected List<String> listIndexedColumns() {
        resource.newInstance().listIndexedColumns()
    }

    protected String displayField(entity) {
        entity.newInstance().displayField()
    }

    protected Field getField(String name) {
        resource.getField(name)
    }

    /**
     * List query of resource based on parameters
     *
     * @return List of resources or empty if it doesn't exist
     */
    protected List<T> listSearchResources(Map params) throws Exception {
        params.remove('controller')
        params.remove('action')
        def fields = resource.newInstance().listIndexedColumns()
        def isGlobal = params.isGlobal
        //isGlobal = isGlobal as Boolean
        def searchQuery = params.searchQuery
        log.info('params >> ' + JsonOutput.toJson(params))

        //isGlobal = !(params.isGlobal as String).equalsIgnoreCase('false')
        searchQuery = params.searchQuery
        def criteria = resource.createCriteria()
        def liste = []
        try {


            if (isGlobal) {
                log.info(JsonOutput.toJson([method: "listSearchResources", message: "Global search >> ${searchQuery}"]))
                def allFields = resource.declaredFields.findAll { fields.contains(it.name) }
                def foreignFields = allFields.findAll { grailsApplication.isDomainClass(it.type) }
                liste = criteria.list(params) {
                    or {
                        for (field in allFields) {
                            if (grailsApplication.isDomainClass(field.type)) {
                                def targetColumn = displayField(field.type)
                                fetchMode("${field.name}", FetchMode.SELECT)
                                createAlias("${field.name}", "${field.name}", CriteriaSpecification.LEFT_JOIN)
                                or { ilike("${field.name}.${targetColumn}", "%${params.searchQuery}%") }
                            }
                            else if (field.type.equals(String.class)) {
                                or { ilike("${field.name}", "%${params.searchQuery}%") }
                            }
                            else if (field.type.getSuperclass().equals(Number.class)) {
                                if (Help.isNumeric(params.searchQuery as String)) {
                                    switch (field.type) {
                                        case Double:
                                            or { eq("${field.name}", Double.parseDouble(params.searchQuery as String)) }
                                            break
                                        case Long:
                                            if (StringUtils.isNumeric(params.searchQuery as String))
                                                or { eq("${field.name}", Long.parseLong(params.searchQuery as String)) }
                                            break
                                        case Integer:
                                            if (StringUtils.isNumeric(params.searchQuery as String))
                                                or { eq("${field.name}", Integer.parseInt(params.searchQuery as String)) }
                                            break
                                    }

                                }
                            }
                            else if (field.type.equals(Date.class)) {
                                SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM-dd')
                                if (GenericValidator.isDate(params.searchQuery as String, 'yyyy-MM-dd', true)) {
                                    def from = sdf.parse(params.searchQuery as String)
                                    def to = Date.from(from.toInstant().plus(1, ChronoUnit.DAYS))
                                    or { between("${field.name}", from, to) }
                                }
                            }
                        }
                    }
                }
            } else {
                log.info(JsonOutput.toJson([method: "listSearchResources", message: "Specific search >> ${searchQuery}"]));
                def search = searchQuery
                if (!(searchQuery instanceof Map))
                    search = Help.checkJsonValid(search as String) ? (new JsonSlurper().parseText(search as String) as Map) : null
                else
                    search = searchQuery
                if (search == null) throw new RuntimeException("un json est requis pour une recherche specifique")

                def allFields = resource.declaredFields.findAll { search.keySet().contains(it.name) }
                def foreignFields = allFields.findAll { grailsApplication.isDomainClass(it.type) }
                Map<Field, Object> finalMap = [:]
                for (entry in search) {
                    allFields.each {
                        if (it.name.equals(entry.key)) {
                            finalMap.put(it, entry.value)
                        }
                    }
                }
                liste = criteria.list(params) {
                    for (field in finalMap) {
                        or {
                            if (grailsApplication.isDomainClass(field.key.type)) {
                                def targetColumn = displayField(field.key.type)
                                fetchMode("${field.key.name}", FetchMode.SELECT)
                                createAlias("${field.key.name}", "${field.key.name}", CriteriaSpecification.LEFT_JOIN)
                                or { ilike("${field.key.name}.${targetColumn}", "%${field.value}%") }
                            } else if (field.key.type.equals(String.class)) {
                                or { ilike("${field.key.name}", "%${field.value}%") }
                            } else if (field.key.type.superclass.equals(Number.class)) {
                                if (Help.isNumeric(field.value as String)) {
                                    switch (field.key.type) {
                                        case Double:
                                            or { eq("${field.key.name}", Double.parseDouble(field.value as String)) }
                                            break
                                        case Long:
                                            if (StringUtils.isNumeric(field.value as String))
                                                or { eq("${field.key.name}", Long.parseLong(field.value as String)) }
                                            break
                                        case Integer:
                                            if (StringUtils.isNumeric(field.value as String))
                                                or { eq("${field.key.name}", Integer.parseInt(field.value as String)) }
                                            break
                                    }
                                }

                            } else if (field.key.type.equals(Date.class)) {
                                SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM-dd')
                                if (GenericValidator.isDate(field.value as String, 'yyyy-MM-dd', true)) {
                                    def from = sdf.parse(field.value as String)
                                    def to = Date.from(from.toInstant().plus(1, ChronoUnit.DAYS))
                                    or { between("${field.key.name}", from, to) }
                                }

                            }


                        }
                    }
                }
            }

            return liste as List<T>
        }
        catch (Exception ex) {
            log.error(grails.plugin.json.builder.JsonOutput.toJson([methode: "listSearchResources", error: ex?.message]))
            render model: [errors: [ex.message]], template: '/errors/custom'
        }
    }

    /**
     * Counts query of resources
     *
     * @return List of resources or empty if it doesn't exist
     */
    protected Integer countSearchResources(params) {
        listSearchResources(params.findAll { it.key != 'max' && it.key != 'offset' })?.size()
    }


    /**
     * Counts all of resources
     *
     * @return List of resources or empty if it doesn't exist
     */
    protected Integer countResources() {
        resource.count()
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [classMessageArg, params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    /**
     * Saves a resource
     *
     * @param resource The resource to be saved
     * @return The saved resource or null if can't save it
     */
    protected T saveResource(T resource) {
        resource.save flush: true
    }

    /**
     * Updates a resource
     *
     * @param resource The resource to be updated
     * @return The updated resource or null if can't save it
     */
    protected T updateResource(T resource) {
        saveResource resource
    }

    /**
     * Deletes a resource
     *
     * @param resource The resource to be deleted
     */
    protected void deleteResource(T resource) {
        resource.delete flush: true
    }

    protected String getClassMessageArg() {
        message(code: "${resourceName}.label".toString(), default: resourceClassName)
    }

    /**
     * Retourn une reponse generic sous format json
     * @param parametres
     * @param message
     * @return
     */
    def genericResponse(int httpCode, String message, def parametres) {
        String status = (httpCode == 200) ? "OK" : "NOK"

        def rep = [
                codeReponse: httpCode,
                dateServeur: new Date(),
                data       : parametres,
                message    : message,
                status     : status
        ]
        render rep as JSON, status: httpCode

    }

}

