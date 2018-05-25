package org.springframework.cloud.contract.verifier.spec.openapi

import spock.lang.Specification


/**
 * Created by jt on 5/24/18.
 */
class OpenApiContactConverterTest extends Specification {

    OpenApiContactConverter contactConverter

    void setup() {
        contactConverter = new OpenApiContactConverter()
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

    def "ConvertFrom"() {
        given:
        File file = new File('src/test/resources/openapi/openapi.yaml')
        when:

        def result = contactConverter.convertFrom(file)

        println result

        then:
        result != null


    }

    def "ConvertTo"() {
    }
}
