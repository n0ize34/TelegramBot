package utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class DuckDuckHandler {

    private static String duckduckUrl = "https://api.duckduckgo.com/?q=%s&format=json&pretty=1";
    private static String answerPattern = "%s. [%s](%s)";
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public String search(String query) throws IOException {
        String preparedQuery = String.format(duckduckUrl, URLEncoder.encode(query.replace(' ', '+'), StandardCharsets.UTF_8.toString()));
        HttpGet request = new HttpGet(preparedQuery);
        CloseableHttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            return parseResult(entity);
        } else {
            return returnNoResults();
        }
    }

    private String parseResult(HttpEntity entity) {
        String result;
        try {
            result = EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
            return returnNoResults();
        }
        JSONObject resultJson = new JSONObject(result);
        JSONArray topics = resultJson.getJSONArray("RelatedTopics");

        StringJoiner answer = new StringJoiner("\n\n");
        answer.setEmptyValue(returnNoResults());
        int topicCounter = 1;
        for (int i = 0; i < topics.length(); i++) {
            JSONObject curTopic = topics.getJSONObject(i);
            if (curTopic.has("Topics")) {
                JSONArray innerTopics = curTopic.getJSONArray("Topics");
                for (int j = 0; j < innerTopics.length(); j++) {
                    JSONObject curInnerTopic = innerTopics.getJSONObject(j);
                    appendTopics(topicCounter++, curInnerTopic, answer);
                }
            } else {
                appendTopics(topicCounter++, curTopic, answer);
            }
        }
        return answer.toString();
    }

    private void appendTopics(int topicCounter, JSONObject topic, StringJoiner answer) {
        String url = topic.get("FirstURL").toString();
        url = url.replaceAll("\\)", "%29");
        answer.add(String.format(answerPattern, topicCounter, topic.get("Text").toString(), url));
    }

    private String returnNoResults() {
        return "No results. Nothing was found.";
    }

}