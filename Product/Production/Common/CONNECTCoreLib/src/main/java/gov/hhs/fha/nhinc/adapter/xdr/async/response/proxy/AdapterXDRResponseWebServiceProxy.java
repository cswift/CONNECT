package gov.hhs.fha.nhinc.adapter.xdr.async.response.proxy;

import gov.hhs.fha.nhinc.adapterxdrresponsesecured.AdapterXDRResponseSecuredPortType;
import gov.hhs.fha.nhinc.adapterxdrresponsesecured.AdapterXDRResponseSecuredService;
import gov.hhs.fha.nhinc.async.AsyncMessageHandler;
import gov.hhs.fha.nhinc.async.AsyncMessageIdCreator;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenCreator;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.apache.commons.logging.Log;

/**
 *
 *
 * @author Visu Patlolla
 */
public class AdapterXDRResponseWebServiceProxy implements AdapterXDRResponseProxy {

    private static org.apache.commons.logging.Log logger = org.apache.commons.logging.LogFactory.getLog(AdapterXDRResponseWebServiceProxy.class);
    private static AdapterXDRResponseSecuredService securedAdapterService = null;

    /**
     *
     * @param body
     * @param assertion
     * @return
     */
    public XDRAcknowledgementType provideAndRegisterDocumentSetBResponse(oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType body, AssertionType assertion) {

        getLogger().debug("Entering provideAndRegisterDocumentSetBResponse");

        String adapterXDRResponseSecuredEndPointURL = null;

        XDRAcknowledgementType response = null;

        adapterXDRResponseSecuredEndPointURL = getAdapterXDRResponseSecuredUrl();

        if (NullChecker.isNotNullish(adapterXDRResponseSecuredEndPointURL)) {
            AdapterXDRResponseSecuredPortType port = getAdapterXDRResponseSecuredPort(adapterXDRResponseSecuredEndPointURL);

            setRequestContext(assertion, adapterXDRResponseSecuredEndPointURL, port);

            response = port.provideAndRegisterDocumentSetBResponse(body);

        } else {
            getLogger().error("The URL for service: " + NhincConstants.ADAPTER_XDR_RESPONSE_SECURED_SERVICE_NAME + " is null");
            response = new XDRAcknowledgementType();
            RegistryResponseType regResp = new RegistryResponseType();
            regResp.setStatus(NhincConstants.XDR_ACK_STATUS_MSG);
            response.setMessage(regResp);
        }

        getLogger().debug("Existing provideAndRegisterDocumentSetBResponse");

        return response;

    }

    protected void setRequestContext(AssertionType assertion, String adapterXDRResponseSecuredEndPointURL, AdapterXDRResponseSecuredPortType port) {
        SamlTokenCreator tokenCreator = new SamlTokenCreator();
        Map requestContext = tokenCreator.CreateRequestContext(assertion, adapterXDRResponseSecuredEndPointURL, NhincConstants.ADAPTER_XDRRESPONSE_SECURED_ACTION);

        ArrayList<Handler> handlerSetUp = new ArrayList<Handler>();
        AsyncMessageHandler msgHandler = new AsyncMessageHandler();
        handlerSetUp.add(msgHandler);
        ((javax.xml.ws.BindingProvider) port).getBinding().setHandlerChain(handlerSetUp);

        AsyncMessageIdCreator msgIdCreator = new AsyncMessageIdCreator();
        requestContext.putAll(msgIdCreator.CreateRequestContextForRelatesTo(assertion));

        ((BindingProvider) port).getRequestContext().putAll(requestContext);
    }

    /**
     *
     * @return
     */
    protected String getAdapterXDRResponseSecuredUrl() {
        String url = null;

        try {
            url = ConnectionManagerCache.getLocalEndpointURLByServiceName(NhincConstants.ADAPTER_XDR_RESPONSE_SECURED_SERVICE_NAME);

        } catch (ConnectionManagerException ex) {
            logger.error("Error: Failed to retrieve url for service: " + NhincConstants.ADAPTER_XDR_RESPONSE_SECURED_SERVICE_NAME, ex);
        }

        return url;
    }

    /**
     *
     * @param url
     * @return
     */
    protected AdapterXDRResponseSecuredPortType getAdapterXDRResponseSecuredPort(String url) {
        AdapterXDRResponseSecuredPortType port = getAdapterXDRResponseSecuredService().getAdapterXDRResponseSecuredPortSoap();

        logger.info("Setting endpoint address to Adapter XDR Secured Service to " + url);
        ((BindingProvider) port).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        return port;
    }

    protected Log getLogger() {
        return logger;
    }

    /**
     *
     * @return
     */
    protected AdapterXDRResponseSecuredService getAdapterXDRResponseSecuredService() {
        if (securedAdapterService == null) {
            return new AdapterXDRResponseSecuredService();
        } else {
            return securedAdapterService;
        }

    }
}