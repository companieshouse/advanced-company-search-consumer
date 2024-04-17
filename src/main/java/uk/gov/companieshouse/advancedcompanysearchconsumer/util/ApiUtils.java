package uk.gov.companieshouse.advancedcompanysearchconsumer.util;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.util.MessageKeys.COMPANY_NAME;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.util.MessageKeys.COMPANY_NUMBER;


public class ApiUtils {

    public static CompanyProfileApi mapMessageToRequest(ServiceParameters serviceParameters) {
        CompanyProfileApi requestBody = new CompanyProfileApi();

        requestBody.setCompanyName(COMPANY_NAME);
        requestBody.setCompanyNumber(COMPANY_NUMBER);

        return requestBody;
    }

}
