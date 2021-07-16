package react.auth;

import org.pac4j.play.PlayWebContext;
import play.mvc.Result;

import static play.mvc.Results.unauthorized;

public class KlarnaOidcResponseErrorHandler extends OidcResponseErrorHandler {

    @Override
    public Result handleError(final PlayWebContext context) {

        // Detailed Klarna specific information
        if (getError(context).equals("access_denied")) {
            return unauthorized(String.format("Access denied. " +
                            "It seems that you don't have access to this application yet. Please apply for access. \n\n" +
                            "If you already have been assigned this application, it may be so that your Okta request is still in action. " +
                            "Please try again in a while, maybe up to 12 hours. " +
                            "Please contact us if the error persists. \n\nError details: '%s':'%s'",
                    context.getRequestParameter("error"),
                    context.getRequestParameter("error_description")));
        }
        return super.handleError(context);
    }
}
