package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * @ApplicationPath sets the versioned base URI for all resources.
 * Combined with the web.xml servlet mapping (/api/v1/*), every endpoint
 * is reachable under /SmartCampusAPI/api/v1/...
 *
 * Lifecycle note: By default JAX-RS resource classes are REQUEST-SCOPED,
 * meaning a new instance is created for every incoming HTTP request.
 * This is why we use static, shared data stores in MockDatabase rather than
 * instance-level fields, ensuring all requests see the same in-memory state.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Jersey picks up all @Path and @Provider classes via package scanning
    // configured in web.xml, so no manual registration is needed here.
}
