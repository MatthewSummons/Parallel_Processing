package org.ziya.resizer;

import java.io.*;
import java.util.List;

import com.amazonaws.regions.Regions;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class ClientApp {
    private static final String file_name = "1.png";
    private static final String file_path = "./1.png";

    public static void main(String[] args) throws Exception {
        // Initialize AWS S3 and SQS clients
        AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.AP_SOUTHEAST_2)
            .build();
        System.out.println("[INFO] Connection with S3 Client established");

        AmazonSQS sqsClient = AmazonSQSClientBuilder
            .standard()
            .withRegion(Regions.AP_SOUTHEAST_2)
            .build();
        System.out.println("[INFO] Connection with SQS Client established");

        // Upload image to S3
        s3Client.putObject(Config.S3_BUCKET, file_name, new File(file_path));
        System.out.println("[INFO] Image uploaded to S3 bucket: " + Config.S3_BUCKET);

        // Send SQS message to inbox with image key
        String messageBody = file_name;
        sqsClient.sendMessage(Config.SQS_INBOX_URL, messageBody);
        System.out.println("[INFO] Message sent to SQS inbox: " + Config.SQS_INBOX_URL);

        // Poll outbox queue for response
        String image_key = null;
        while (true) {
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                .withQueueUrl(Config.SQS_OUTBOX_URL)
                .withWaitTimeSeconds(10)
                .withMaxNumberOfMessages(1);

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();

            if (!messages.isEmpty()) {
                Message message = messages.get(0);
                System.out.println("[INFO] Received message: " + message.getBody());
                image_key = message.getBody();
                sqsClient.deleteMessage(Config.SQS_OUTBOX_URL, messages.get(0).getReceiptHandle());
                break;
            } else {
                System.out.println("No messages in outbox, polling again...");
            }
        } System.out.println("[INFO] Message received from SQS outboxbox");
        
        
        // Download processed image from S3
        S3Object processedImage = s3Client.getObject(Config.S3_BUCKET, image_key);
        System.out.println("[INFO] Processed image downloaded from S3 bucket: " + Config.S3_BUCKET);
        File outputFile = new File("processed_" + file_name);
        try (InputStream in = processedImage.getObjectContent();
             FileOutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } System.out.println("[INFO] Processed image saved to disk");


        s3Client.shutdown();
        sqsClient.shutdown();
    }
}
