package sun.saq.security


import sun.saq.suntelecoms.RestfulController

class RoleController extends RestfulController<Role> {
	static responseFormats = ['json', 'xml']

    RoleController() {
        super(Role)
    }

}
