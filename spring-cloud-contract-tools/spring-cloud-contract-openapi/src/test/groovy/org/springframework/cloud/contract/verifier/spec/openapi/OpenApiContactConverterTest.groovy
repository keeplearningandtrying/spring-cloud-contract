package org.springframework.cloud.contract.verifier.spec.openapi

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Ignore
import spock.lang.Specification


/**
 * Created by jt on 5/24/18.
 */
class OpenApiContactConverterTest extends Specification {

    OpenApiContractConverter contactConverter

    void setup() {
        contactConverter = new OpenApiContractConverter()
    }

    def "IsAccepted True"() {
        given:
        File file = new File('src/test/resources/openapi/openapi.yaml')
        when:

        def result = contactConverter.isAccepted(file)

        then:
        result

    }

    def "IsAccepted False"() {
        given:
        File file = new File('foo')
        when:

        def result = contactConverter.isAccepted(file)

        then:
        !result

    }

    def "ConvertFrom - should not go boom"() {
        given:
        File file = new File('src/test/resources/openapi/openapi.yaml')
        when:

        def result = contactConverter.convertFrom(file)

        println result

        then:
        result != null


    }

    @Ignore // not working yet
    def "Test Should Mark Client As Fraud"() {
        given:
        Contract dslContract = DslContracts.shouldMarkClientAsFraud()

        File file = new File('src/test/resources/openapi/fraudservice.yaml')
        when:

        def result = contactConverter.convertFrom(file)

        Contract openApiContract = result.find {it.name.equalsIgnoreCase("Should Mark Client As Fraud")}

        then:
        result != null
        dslContract == openApiContract

    }
}
