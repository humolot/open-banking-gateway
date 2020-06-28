package de.adorsys.opba.protocol.sandbox.hbci.protocol.authenticated.nonauthorized;

import de.adorsys.opba.protocol.sandbox.hbci.protocol.Operation;
import de.adorsys.opba.protocol.sandbox.hbci.protocol.TemplateBasedOperationHandler;
import de.adorsys.opba.protocol.sandbox.hbci.protocol.context.SandboxContext;
import de.adorsys.opba.protocol.sandbox.hbci.protocol.interpolation.JsonTemplateInterpolation;
import org.springframework.stereotype.Service;

@Service("authenticatedDialogEnd")
public class AuthenticatedDialogEnd extends TemplateBasedOperationHandler {

    public AuthenticatedDialogEnd(JsonTemplateInterpolation interpolation) {
        super(interpolation);
    }

    @Override
    protected String templatePath(SandboxContext context) {
        return "response-templates/authenticated/dialog-end.json";
    }

    @Override
    protected Operation handledRequestType() {
        return Operation.DIALOG_END;
    }
}
