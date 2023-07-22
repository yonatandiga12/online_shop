package BusinessLayer.ExternalSystems;

import initialize.ConfigReader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public abstract class URLRequest {


    public StringBuilder setQuery(Map<String, String> postContent) throws Exception {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : postContent.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            query.append("=");
            query.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            query.append("&");
        }
        return query;
    }

    public String getUrlResponse(StringBuilder query) throws IOException {
        try {
            //String urlString = "https://php-server-try.000webhostapp.com/";
            String urlString = new ConfigReader().getExternalSystemsURL();
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(query.toString());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200)
                return "-1";

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseMessage = response.toString();

            connection.disconnect();

            return responseMessage;
        } catch (java.net.SocketTimeoutException e) {
            throw new SocketException("External System is not responding");
        }
        catch (Exception e1) {
            return "-1";
        }
    }




}
