package br.com.gustavo.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.net.ConnectException;
import java.util.List;

@Component
public class QueueRoute extends RouteBuilder {

    private static final String EXCEPTION_MESSAGE = "${header.CamelHttpResponseCode} - ${header.CamelHttpResponseText}";

    public static final String API_URI = "{{api.uri}}";
    public static final String API_DIRECT_ID = "api-route";
    public static final String API_DIRECT_URI = "direct:" + API_DIRECT_ID;

    @Value("#{'${api.no.redelivery.response.codes}'.split(',')}")
    private List<String> valuesApiNoRedeliveryResponseCodes;

    @Override
    public void configure() throws Exception {

        final String consumerId = "{{route.broker.consumer.id}}";
        final String consumerUri = "{{route.broker.consumer.uri}}";

        final String connectionErrorId = "{{route.broker.consumer.connection.error.id}}";
        final String connectionErrorUri = "{{route.broker.consumer.connection.error.uri}}";
        final String responseErrorId = "{{route.broker.consumer.response.error.id}}";
        final String responseErrorUri = "{{route.broker.consumer.response.error.uri}}";

        final String smtpUrl = "{{route.smtp.url}}";

        onException(ConnectException.class)
            .handled(false)
            .maximumRedeliveries(2)
            .to(connectionErrorUri)
            .end();

        from(consumerUri)
            .routeId(consumerId)
            .log("\nCONSUMED MESSAGE:\n${body}")
            .to(API_DIRECT_URI)
            .end()
            .log("Message sent!")
            .end();

        from(API_DIRECT_URI)
            .routeId(API_DIRECT_ID)
            .removeHeaders("CamelHttp*")
            .setHeader(Exchange.HTTP_METHOD, HttpMethods.POST)
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_UTF8))
            .setHeader("Accept", constant(MediaType.APPLICATION_JSON))
            .to(API_URI)
            .log(LoggingLevel.INFO, "\n\nresult: ${header.CamelHttpResponseCode}\n\n${body}\n")
            .choice()
                .when(isResponsePositive())
                    .log(LoggingLevel.INFO, "\nCreated with Success: ${header.CamelHttpResponseCode}")
                    .log(LoggingLevel.INFO, "\n${body}")
                .when(header(Exchange.HTTP_RESPONSE_CODE).in(valuesApiNoRedeliveryResponseCodes))
                    .log(LoggingLevel.INFO, "\n\nPUT failed: ${header.CamelHttpResponseCode}\n\n${body}\n")
                    .to(responseErrorUri)
                .otherwise()
                    .log(LoggingLevel.INFO, "\n\nfailed: ${header.CamelHttpResponseCode}\n\n${body}\n")
                    .to(responseErrorUri)
                    .throwException(new Exception("Error send message to Fake API."))
                .end()
            .end();

        from(connectionErrorUri)
            .routeId(connectionErrorId)
            .log(LoggingLevel.INFO, EXCEPTION_MESSAGE)
            .setHeader("subject", constant("Http Connection Error"))
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
            .setBody(simple(EXCEPTION_MESSAGE))
            .to(smtpUrl)
            .log(LoggingLevel.INFO, "Email sent.")
            .end();

        from(responseErrorUri)
            .routeId(responseErrorId)
            .log(EXCEPTION_MESSAGE)
            .setHeader("subject", simple(EXCEPTION_MESSAGE))
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
            .setBody(simple(EXCEPTION_MESSAGE + "\n\nMensagem:\n${exchangeProperty.originalRequest}"))
            .to(smtpUrl)
            .log(LoggingLevel.INFO, "Email sent.")
            .end();
    }

    private Predicate isResponsePositive() {
        return PredicateBuilder.or(
                header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(HttpServletResponse.SC_CREATED),
                header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(HttpServletResponse.SC_OK));
    }
}
