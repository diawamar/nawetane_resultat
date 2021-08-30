package sun.saq

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action: "delete")
        get "/$controller(.$format)?"(action: "index")
        get "/$controller/$id(.$format)?"(action: "show")
        post "/$controller(.$format)?"(action: "save")
        put "/$controller/$id(.$format)?"(action: "update")
        patch "/$controller/$id(.$format)?"(action: "patch")

        group "/api", {

            '/users'(resources: 'user')
            '/roles'(resources: 'role')
            '/users/role'(resources: 'userRole')
            '/users/change-password'(controller: 'user', action: 'changePassword')
            '/users/forget-password'(controller: 'user', action: 'forgetPassword')
            '/users/reset-password'(controller: 'user', action: 'resetPassword')
            "/users/get-username"(controller: 'user', action: 'getUserByUsername')
            "/users/bloquer-debloquer"(controller: 'user', action: 'bloquerDebloquerUtilisateur')

            //Gestion des privileges
            "/privileges/give-autorisations"(controller: 'autorisations', action: 'addAutorisations')
            "/privileges/get-roles"(controller: 'privileges', action: 'getPrivileges')
            '/privileges'(resources: 'privileges')
            "/privileges/liste-privileges"(controller: 'privileges', action: 'listePrivilegesV1')

        }
        "/public/test-contrat-jasper"(controller: 'test', action: 'testContratJasper')
        "/"(controller: 'application', action: 'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
