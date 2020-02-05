package org.gluu.oxd.server.op;

import com.google.inject.Injector;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxauth.model.uma.UmaMetadata;
import org.gluu.oxd.common.Command;
import org.gluu.oxd.common.ErrorResponseCode;
import org.gluu.oxd.common.params.RpGetClaimsGatheringUrlParams;
import org.gluu.oxd.common.response.IOpResponse;
import org.gluu.oxd.common.response.RpGetClaimsGatheringUrlResponse;
import org.gluu.oxd.server.HttpException;
import org.gluu.oxd.server.Utils;
import org.gluu.oxd.server.service.Rp;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 17/06/2016
 */

public class RpGetGetClaimsGatheringUrlOperation extends BaseOperation<RpGetClaimsGatheringUrlParams> {

//    private static final Logger LOG = LoggerFactory.getLogger(RpGetGetClaimsGatheringUrlOperation.class);

    protected RpGetGetClaimsGatheringUrlOperation(Command command, final Injector injector) {
        super(command, injector, RpGetClaimsGatheringUrlParams.class);
    }

    @Override
    public IOpResponse execute(RpGetClaimsGatheringUrlParams params) throws Exception {
        final Rp rp = getRp();
        validate(params, rp);

        final UmaMetadata metadata = getDiscoveryService().getUmaDiscoveryByOxdId(params.getOxdId());

        final String state = StringUtils.isNotBlank(params.getState()) ? getStateService().putState(Utils.encode(params.getState())) : getStateService().generateState();
        String redirectUri = StringUtils.isNotBlank(params.getRedirectUri()) ? params.getRedirectUri() : rp.getRedirectUri();

        String url = metadata.getClaimsInteractionEndpoint() +
                "?client_id=" + rp.getClientId() +
                "&ticket=" + params.getTicket() +
                "&claims_redirect_uri=" + params.getClaimsRedirectUri() +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        final RpGetClaimsGatheringUrlResponse r = new RpGetClaimsGatheringUrlResponse();
        r.setUrl(url);
        r.setState(state);
        return r;
    }

    private void validate(RpGetClaimsGatheringUrlParams params, Rp rp) {
        if (StringUtils.isBlank(params.getTicket())) {
            throw new HttpException(ErrorResponseCode.NO_UMA_TICKET_PARAMETER);
        }
        if (StringUtils.isBlank(params.getClaimsRedirectUri())) {
            throw new HttpException(ErrorResponseCode.NO_UMA_CLAIMS_REDIRECT_URI_PARAMETER);
        }
        if (!StringUtils.isBlank(params.getRedirectUri()) && !rp.getRedirectUris().contains(params.getRedirectUri())) {
            throw new HttpException(ErrorResponseCode.INVALID_REDIRECT_URI);
        }
    }
}