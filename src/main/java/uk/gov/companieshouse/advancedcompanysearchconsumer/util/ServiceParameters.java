package uk.gov.companieshouse.advancedcompanysearchconsumer.util;

import uk.gov.companieshouse.advancedcompanysearchconsumer.service.Service;

import java.util.Objects;
import uk.gov.companieshouse.stream.ResourceChangedData;

/**
 * Contains all parameters required by {@link Service service implementations}.
 */
public class ServiceParameters {

    private final ResourceChangedData data;

    public ServiceParameters(ResourceChangedData data) {
        this.data = data;
    }

    /**
     * Get data attached to the ServiceParameters object.
     *
     * @return A {@link ResourceChangedData} representing data that has been attached to the ServiceParameters object.
     */
    public ResourceChangedData getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceParameters)) {
            return false;
        }
        ServiceParameters that = (ServiceParameters) o;
        return Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData());
    }
}