package react.auth;

import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Result;

import static play.mvc.Results.internalServerError;


public class OidcResponseErrorHandler {
    private final Logger _logger = LoggerFactory.getLogger(getClass());

    private static final String ERROR_FIELD_NAME = "error";
    private static final String ERROR_DESCRIPTION_FIELD_NAME = "error_description";

    public Result handleError(final PlayWebContext context) {

        _logger.error("OIDC responded with an error: '{}'. Error description: '{}'",
                getError(context),
                getErrorDescription(context));
        return internalServerError(
                String.format("Internal server error. The OIDC service responded with an error: '%s'.\n" +
                                "Error description: '%s'",
                getError(context),
                getErrorDescription(context)));
    }

    public boolean isError(final PlayWebContext context) {
        return getError(context) != null && !getError(context).isEmpty();
    }

    public static String getError(final PlayWebContext context) {
        return context.getRequestParameter(ERROR_FIELD_NAME);
    }

    public static String getErrorDescription(final PlayWebContext context) {
        return context.getRequestParameter(ERROR_DESCRIPTION_FIELD_NAME);
    }
}
