package sun.saq.security


import sun.saq.suntelecoms.RestfulController

class UserRoleController extends RestfulController<UserRole> {
	static responseFormats = ['json', 'xml']

    UserRoleController() {
        super(UserRole)
    }

    def index() { }
}
