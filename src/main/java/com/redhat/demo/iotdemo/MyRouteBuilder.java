package com.redhat.demo.iotdemo;

import com.redhat.demo.iotdemo.kura.Metric;
import com.redhat.demo.iotdemo.kura.Metrics;
import com.redhat.demo.iotdemo.kura.Payload;
import com.redhat.demo.iotdemo.kura.KuraPayload;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.Exchange.CONTENT_TYPE;

public class MyRouteBuilder extends RouteBuilder {

    public static String ECURL = "https4://api-sandbox.everyware-cloud.com/v2/messages/publish";

    public void configure() {

        from("timer://heartbeat?fixedRate=true&period=1000")
              .process(new Processor() {
                  @Override
                  public void process(Exchange exchange) throws Exception {
                      List<Metric> metricList = new ArrayList<>();
                      metricList.add(new Metric("Ambient", "double", "25.12345")); // type is one of string, double, int, float, long, boolean
                      metricList.add(new Metric("Humidity", "float", "32.287501"));
                      metricList.add(new Metric("Light", "double", "87.23"));

                      KuraPayload payload = new KuraPayload()
                            .withTopic("<AccountID>/<DeviceId>/<Topic>")
                            .withPayload(new Payload()
                                .withMetrics(new Metrics(metricList))
                            );
                     exchange.getIn().setBody(payload);
                  }
              })
              .to("direct:publish");


        from("direct:publish")
              .marshal().json(JsonLibrary.Gson)
              .setHeader(CONTENT_TYPE, constant("application/json"))
              .setHeader("Authorization", simple("Basic <echo 'userid:password'|base64>"))
              .to("log:Http4Post?showAll=true&multiline=true")
              .to(ECURL);
    }

}
