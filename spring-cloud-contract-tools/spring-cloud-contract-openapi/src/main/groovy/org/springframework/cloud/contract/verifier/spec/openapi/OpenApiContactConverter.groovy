package org.springframework.cloud.contract.verifier.spec.openapi

import groovy.util.logging.Slf4j
import io.swagger.oas.models.PathItem
import io.swagger.parser.v3.OpenAPIV3Parser
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter

import static org.apache.commons.lang3.StringUtils.isNumeric

/**
 * Created by jt on 5/24/18.
 */
@Slf4j
class OpenApiContactConverter implements ContractConverter<Collection<PathItem>> {

    @Override
    boolean isAccepted(File file) {
        try {
            def spec = new OpenAPIV3Parser().read(file.path)

            if (spec == null){
                log.debug("Spec Not Found")
                throw new RuntimeException("Spec not found")
            }

            if (spec.paths.size() == 0){ // could toss NPE, which is also invalid spec
                log.debug("No Paths Found")
                throw new RuntimeException("No paths found")
            }

            def contractsFound = false
            //check spec for contracts
            spec.paths.each { k, v ->
                if(!contractsFound){
                    v.readOperations().each { operation ->
                        if (operation.extensions){
                            def contracts = operation.extensions."x-contracts"
                            if(contracts.size > 0){
                                contractsFound = true
                            }
                        }
                    }
                }
            }

            return contractsFound
        } catch (Exception e) {
            log.debug(e.message)
            return false
        }
    }

    @Override
    Collection<Contract> convertFrom(File file) {
        def sccContracts = []

        def spec = new OpenAPIV3Parser().read(file.path)

        spec?.paths?.each { path, pathItem ->
            pathItem.readOperations().each { operation ->
                if (operation?.extensions?."x-contracts"){
                    operation.extensions."x-contracts".each { openApiContract ->

                        def contractId = openApiContract.contractId

                        sccContracts.add(
                            Contract.make {
                                name = contractId + " - " + openApiContract.name

                                request {
                                    if (pathItem?.get?.is(operation)) {
                                        method("GET")
                                    } else if (pathItem?.put.is(operation)){
                                        method("PUT")
                                    } else if (pathItem?.post.is(operation)){
                                        method("POST")
                                    } else if (pathItem?.delete.is(operation)){
                                        method("DELETE")
                                    } else if (pathItem?.patch.is(operation)){
                                        method("PATCH")
                                    }
                                    url(path) {
                                        queryParameters {
                                            operation?.parameters?.each { openApiParam ->
                                                openApiParam?.extensions?."x-contracts".each { contractParam ->
                                                    if (contractParam.contractId == contractId){
                                                        if(openApiParam.in == 'path'){
                                                            parameter(openApiParam.name, contractParam.default)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    headers {
                                        if(openApiContract?.requestHeaders) {
                                            openApiContract?.requestHeaders?.each { xheader ->
                                                xheader.each {k, v ->
                                                    header(k, v)
                                                }
                                            }
                                        }
                                    }

                                    if(operation?.requestBody?.extensions?."x-contracts"){
                                        operation?.requestBody?.extensions?."x-contracts"?.each { contractBody ->
                                            if(contractBody.contractId == contractId){
                                                body(contractBody.body)
                                            }
                                        }
                                    }
                                    if(operation?.responses) {
                                        operation.responses.each { openApiResponse ->
                                            if (openApiResponse?.value?.extensions?.'x-contracts') {
                                                openApiResponse?.value?.extensions?.'x-contracts'?.each { responseContract ->
                                                    if (responseContract.contractId == contractId){

                                                        response {
                                                            if(isNumeric(openApiResponse.key)) {
                                                                status(Integer.valueOf(openApiResponse.key))
                                                            }

                                                            def contentTypeSB = new StringBuffer()

                                                            openApiResponse.getValue()?.content?.keySet()?.each { val ->
                                                                contentTypeSB.append(val)
                                                                contentTypeSB.append(';')
                                                            }

                                                            headers {
                                                                header('Content-Type', contentTypeSB.toString())
                                                                //todo add support for additional headers
                                                            }


                                                            if(responseContract.body) {
                                                                body{
                                                                    responseContract.body
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                    }
                }
            }
        }

        return sccContracts
    }


    @Override
    Collection<PathItem> convertTo(Collection<Contract> contract) {

        throw new RuntimeException("Not Implemented")

    }
}
