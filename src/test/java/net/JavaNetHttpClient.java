package net; /**
 * Created by kota.saito on 2016/03/09.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
//import play.api.libs.json.JsValue;
//import play.api.libs.json.Json;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JavaNetHttpClient {

  public enum ResKey {
    STATUS, CONTENTS
  }

  public static Map<ResKey, String> executeGet(String urlStr) throws IOException {
    URL url = new URL(urlStr);

    HttpURLConnection connection = null;

    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      return handleResponse(connection);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

  }

  public static Map<ResKey, String> executePost(String urlStr, String postData) throws IOException {

    URL url = new URL(urlStr);

    HttpURLConnection connection = null;

    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      connection.setRequestProperty("Content-Type", "application/json");

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),
          StandardCharsets.UTF_8));
      writer.write(postData);
      writer.flush();

      return handleResponse(connection);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

  }

  private static Map<ResKey, String> handleResponse(HttpURLConnection connection) throws IOException {
    StringBuilder sb = new StringBuilder("");
    Map<ResKey, String> ans = new HashMap<>();
    ans.put(ResKey.STATUS, Integer.toString(connection.getResponseCode()));

    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(),
          StandardCharsets.UTF_8);
           BufferedReader reader = new BufferedReader(isr)) {
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
      }
      ans.put(ResKey.CONTENTS, sb.toString());
      return ans;

    } else {
      InputStream is = connection.getErrorStream();
      if (is == null) {
        return ans;
      }
      try (InputStreamReader isr = new InputStreamReader(is,
          StandardCharsets.UTF_8);
           BufferedReader reader = new BufferedReader(isr)) {
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
      }
      ans.put(ResKey.CONTENTS, sb.toString());
      return ans;
    }
  }

}