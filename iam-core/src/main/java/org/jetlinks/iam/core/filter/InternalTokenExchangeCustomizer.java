package org.jetlinks.iam.core.filter;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.iam.core.entity.Parameter;
import org.jetlinks.iam.core.token.ParsedToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * 应用的请求过滤器，透传token.
 *
 * @author zhangji 2023/8/4
 */
@AllArgsConstructor
public class InternalTokenExchangeCustomizer implements ClientHttpRequestInterceptor {

    private final List<Parameter> headers;

    private final List<Parameter> parameters;


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (CollectionUtils.isNotEmpty(headers)) {
            for (Parameter header : this.headers) {
                if (header.getValue() != null) {
                    request.getHeaders().add(header.getKey(), header.getValue());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(parameters)) {
            UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromHttpRequest(request);
            for (Parameter parameter : parameters) {
                if (parameter.getKey() != null) {
                    componentsBuilder.queryParam(parameter.getKey(), parameter.getValue());
                }
            }

            request = new HttpRequestWrapper(request) {
                @Override
                public URI getURI() {
                    return componentsBuilder.build().toUri();
                }
            };
        }

        return execution.execute(request, body);
    }

    public static void applyToken(ParsedToken token, HttpHeaders headers) {
        if ("default".equals(token.getType())) {
            headers.add("X-Access-Token", token.getToken());
        } else if ("basic".equals(token.getType())) {
            headers.setBasicAuth(token.getToken());
        } else {
            headers.setBearerAuth(token.getToken());
        }
    }


}
