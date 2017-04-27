package org.springframework.samples.petclinic.system;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jan.plitschka on 26.04.2017.
 */
@Component
public class ApplicationListener implements InitializingBean, DisposableBean {

    private ExecutorService executorService;
    private boolean run = true;


    @Override
    public void destroy() throws Exception {
        run = false;
        System.out.println("Self destruct sequence initiated");
        sendNotification("Self destruct sequence initiated");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        sendNotification("I am alive again");

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            while (run) {
                //custom metric
                try (Socket conn = new Socket("a7a33c54.carbon.hostedgraphite.com", 2003)) {
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes("5cfc61bb-3f1b-4229-a569-26fce2181f64.test.testing 1\n");
                    conn.close();
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Stopping sending alive requests");
        });
        System.out.println("I am alive again");
    }

    private void sendNotification(String message) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();//Use this instead
        try {
            HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
            StringEntity params = new StringEntity("payload={\"text\":\"" + message + "\"}");
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
        } catch (Exception ex) {

            //handle exception here

        } finally {
            //Deprecated
            //httpClient.getConnectionManager().shutdown();
        }
    }
}
