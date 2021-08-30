package sun.saq

import grails.core.GrailsApplication
import grails.plugins.GrailsPluginManager
import grails.plugins.PluginManagerAware
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

@Api(value = "/", tags = ["Application"])
class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    @ApiOperation(value = "App Infos",nickname='/')
    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }
}
