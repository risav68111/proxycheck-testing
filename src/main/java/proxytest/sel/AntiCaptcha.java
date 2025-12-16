package proxytest.sel;
// package com.fios.fiosspringbootapi.utlis;

import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class AntiCaptcha {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String KEY = dotenv.get("ANTICPTCHA_KEY");

    public static String CreateTask(String filePath) {
        HttpClient httpClient = HttpClients.createDefault();
        String url = "https://api.anti-captcha.com/createTask";
        HttpPost httpPost = new HttpPost(url);

        try {
            // Set headers
            byte[] imageBytes = Files.readAllBytes(Paths.get(filePath));

            // Step 2: Encode the byte array to Base64
            String base64Encoded = Base64.getEncoder().encodeToString(imageBytes);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            JSONObject postData = new JSONObject();
            postData.put("type", "ImageToTextTask");
            postData.put("body", base64Encoded.replace("\r", "").replace("\n", ""));
            postData.put("phrase", false);
            postData.put("case", false);
            postData.put("numeric", 0);
            postData.put("math", true);
            postData.put("minLength", 0);
            postData.put("maxLength", 0);
            JSONObject jsonPostData = new JSONObject();
            jsonPostData.put("clientKey", KEY);
            jsonPostData.put("softId", 0);
            jsonPostData.put("task", postData);

            // Set request body

            StringEntity entity = new StringEntity(jsonPostData.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // Execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // Get the response body as a String
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            // Handle the response as needed
            System.out.println("Response Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            JSONObject responseJson = new JSONObject(responseBody);
            if (responseJson.get("taskId").toString() != null) {
                Thread.sleep(3000);
               return checkTaskStatusWithinTimeLimit(Integer.parseInt(responseJson.get("taskId").toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    public static JSONObject getTaskResult(Integer TaskId) {
        HttpClient httpClient = HttpClients.createDefault();
        String url = "https://api.anti-captcha.com/getTaskResult";
        HttpPost httpPost = new HttpPost(url);

        try {
            // Set headers

            // Step 2: Encode the byte array to Base64

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            JSONObject jsonPostData = new JSONObject();
            jsonPostData.put("clientKey", KEY);
            jsonPostData.put("taskId", TaskId);

            // Set request body

            StringEntity entity = new StringEntity(jsonPostData.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // Execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // Get the response body as a String
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            // Handle the response as needed
            System.out.println("Response Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            JSONObject responseJson = new JSONObject(responseBody);
            return responseJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static String checkTaskStatusWithinTimeLimit(Integer TaskId) {
//        long startTime = System.currentTimeMillis();
//        long timeout = 120 * 1000; // Convert 120 seconds to milliseconds
//
//        // Simulate the task progress with a loop
//        JSONObject taskStatus = new JSONObject();
//        taskStatus.put("status", "processing");
//        String captcha = "";
//        while (taskStatus == null || !taskStatus.has("status") || !"ready".equals(taskStatus.getString("status"))) {
//            System.out.println(taskStatus.get("status"));
//            // Check if the time limit has been exceeded
//            if (System.currentTimeMillis() - startTime > timeout) {
//                return "";
//            }
//
//            // Simulate task progress - Replace this with your actual task status check
//            // logic
//            taskStatus = getTaskResult(TaskId); // Assuming this method returns the current task status
//
//            try {
//                if(!"ready".equals(taskStatus.getString("status"))){
//                    Thread.sleep(3000);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        if (taskStatus.getJSONObject("solution").getString("text") != null) {
//            captcha = taskStatus.getJSONObject("solution").getString("text");
//            System.out.println("this is captcha->>>>>>" + captcha);
//        }
//        return captcha;
//    }

    public static String checkTaskStatusWithinTimeLimit(Integer taskId) {
        long startTime = System.currentTimeMillis();
        long timeout = 120 * 1000; // 120 seconds in milliseconds

        try {
            while (System.currentTimeMillis() - startTime < timeout) {
                JSONObject taskStatus = getTaskResult(taskId);

                if (taskStatus != null) {
                    // Handle errors silently
                    if (taskStatus.has("errorCode")) {
                        return ""; // Return empty result for any error
                    }

                    // Process task status
                    if ("ready".equals(taskStatus.optString("status"))) {
                        JSONObject solution = taskStatus.optJSONObject("solution");
                        if (solution != null && solution.has("text")) {
                            return solution.getString("text"); // Return solved captcha text
                        }
                    } else if ("processing".equals(taskStatus.optString("status"))) {
                        Thread.sleep(3000); // Wait for 3 seconds before the next check
                    }
                }
            }
        } catch (Exception e) {
            // Handle exception silently
        }

        return ""; // Return empty if no result within the time limit
    }


}
