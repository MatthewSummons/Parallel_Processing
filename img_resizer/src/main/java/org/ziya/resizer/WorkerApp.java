package org.ziya.resizer;

import java.io.*;
import java.util.List;

import com.amazonaws.regions.Regions;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.Message;

public class WorkerApp {
    private static String image_key;
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

        // Poll inbox queue for messages (every 10s)
        while (true) {
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                .withQueueUrl(Config.SQS_INBOX_URL)
                .withWaitTimeSeconds(10)
                .withMaxNumberOfMessages(1);

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();

            if (!messages.isEmpty()) {
                Message message = messages.get(0);
                System.out.println("[INFO] Received message: " + message.getBody());
                image_key = message.getBody();
                sqsClient.deleteMessage(Config.SQS_INBOX_URL, messages.get(0).getReceiptHandle());
                break;
            } else {
                System.out.println("No messages, polling again...");
            }
        } System.out.println("[INFO] Message received from SQS inbox");
        
        // Download image from S3
        S3Object fetchedFile = s3Client.getObject(Config.S3_BUCKET, image_key);
        System.out.println("[INFO] Image downloaded from S3 bucket: " + Config.S3_BUCKET);

        File inputFile = File.createTempFile("input-", ".png");
        try (InputStream in = fetchedFile.getObjectContent();
            FileOutputStream out = new FileOutputStream(inputFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        // Process image using ImageMagick (resize to 200x200)
        File outputFile = File.createTempFile("output-", ".png");
        ProcessBuilder pb = new ProcessBuilder(
                "convert", inputFile.getAbsolutePath(), "-resize", "200x200", outputFile.getAbsolutePath());
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("[INFO] Image resized successfully: " + outputFile.getAbsolutePath());
        } else {
            System.out.println("[ERROR] ImageMagick convert failed.");
        }

        // Upload processed image to S3
        s3Client.putObject(Config.S3_BUCKET, "processed-" + image_key, outputFile);
        System.out.println("[INFO] Processed image uploaded to S3 bucket: " + Config.S3_BUCKET);

        // Send SQS message to outbox with processed image key
        String messageBody = "processed-" + image_key;
        sqsClient.sendMessage(Config.SQS_OUTBOX_URL, messageBody);
        System.out.println("[INFO] Message sent to SQS outbox: " + Config.SQS_OUTBOX_URL);
        
        // Delete message from inbox queue
        System.out.println("[INFO] Message deleted from SQS inbox: " + Config.SQS_INBOX_URL);

        // Clean up; Close resources
        inputFile.delete();
        outputFile.delete();

        s3Client.shutdown();
        sqsClient.shutdown();

        System.out.println("[INFO] WorkerApp completed successfully.");
    }
}
