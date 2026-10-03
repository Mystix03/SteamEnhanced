package com.custom.steamenhanced;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HLTBManager {

    private static final String HLTB_URL =
            "https://howlongtobeat.com/api/search/site";

    public interface Callback {
        void onSuccess(String gameName, double hours, String url);
        void onError(String error);
    }

    public static void lookup(
            String gameName,
            Callback callback) {

        new Thread(() -> {

            try {

                // ---------------------------------------------------------
                // 1. Normalize game name exactly for the HLTB search
                // ---------------------------------------------------------

                String cleanName = gameName
                        .replace("’", "'")
                        .replaceAll("[^\\p{L}\\p{N}\\s`~!@#$%^&*()_=+|\\\\\\]}\\[{;:',<.>/?-]", "")
                        .replaceAll("\\s+", " ")
                        .trim();

                if (cleanName.isEmpty()) {
                    throw new Exception("Game name is empty");
                }

                String lowerName =
                        cleanName.toLowerCase();

                /*
                 * The original userscript creates:
                 *
                 * "elden","ring"
                 *
                 * rather than sending a normal string.
                 */
                JSONArray searchTerms =
                        new JSONArray();

                for (String word :
                        lowerName.split("\\s+")) {

                    if (!word.isEmpty()) {
                        searchTerms.put(word);
                    }
                }


                // ---------------------------------------------------------
                // 2. Get HLTB authentication token
                //
                // Based directly on the fork:
                //
                // GET /api/search/site/init?t=
                //
                // Headers:
                // Accept: */*
                // User-Agent: browser UA
                // Referer: https://howlongtobeat.com/?q=
                // ---------------------------------------------------------

                String initUrl =
                        HLTB_URL + "/init?t=";

                JSONObject initHeaders =
                        new JSONObject();

                initHeaders.put(
                        "Accept",
                        "*/*"
                );

                initHeaders.put(
                        "User-Agent",
                        getUserAgent()
                );

                initHeaders.put(
                        "Referer",
                        "https://howlongtobeat.com/?q="
                );

                String initResponse =
                        makeRequest(
                                initUrl,
                                "GET",
                                null,
                                initHeaders
                        );

                JSONObject initJson =
                        new JSONObject(initResponse);

                String token =
                        initJson.optString(
                                "token",
                                ""
                        );

                /*
                 * The fork uses hpKey/hpVal from the init response.
                 */
                String hpKey =
                        initJson.optString(
                                "hpKey",
                                ""
                        );

                String hpVal =
                        initJson.optString(
                                "hpVal",
                                ""
                        );

                if (token.isEmpty()) {
                    throw new Exception(
                            "HLTB token not received"
                    );
                }


                // ---------------------------------------------------------
                // 3. Build the SAME searchOptions structure used by
                //    the fork.
                // ---------------------------------------------------------

                JSONObject games =
                        new JSONObject();

                games.put(
                        "userId",
                        0
                );

                games.put(
                        "platform",
                        ""
                );

                games.put(
                        "sortCategory",
                        "popular"
                );

                games.put(
                        "rangeCategory",
                        "main"
                );


                JSONObject rangeTime =
                        new JSONObject();

                rangeTime.put(
                        "min",
                        JSONObject.NULL
                );

                rangeTime.put(
                        "max",
                        JSONObject.NULL
                );

                games.put(
                        "rangeTime",
                        rangeTime
                );


                JSONObject gameplay =
                        new JSONObject();

                gameplay.put(
                        "perspective",
                        ""
                );

                gameplay.put(
                        "flow",
                        ""
                );

                gameplay.put(
                        "genre",
                        ""
                );

                gameplay.put(
                        "difficulty",
                        ""
                );

                games.put(
                        "gameplay",
                        gameplay
                );


                JSONObject rangeYear =
                        new JSONObject();

                rangeYear.put(
                        "min",
                        ""
                );

                rangeYear.put(
                        "max",
                        ""
                );

                games.put(
                        "rangeYear",
                        rangeYear
                );

                games.put(
                        "modifier",
                        ""
                );


                JSONObject users =
                        new JSONObject();

                users.put(
                        "sortCategory",
                        "postcount"
                );


                JSONObject lists =
                        new JSONObject();

                lists.put(
                        "sortCategory",
                        "follows"
                );


                JSONObject searchOptions =
                        new JSONObject();

                searchOptions.put(
                        "games",
                        games
                );

                searchOptions.put(
                        "users",
                        users
                );

                searchOptions.put(
                        "lists",
                        lists
                );

                searchOptions.put(
                        "filter",
                        ""
                );

                searchOptions.put(
                        "sort",
                        0
                );

                searchOptions.put(
                        "randomizer",
                        0
                );


                // ---------------------------------------------------------
                // 4. Build POST body.
                //
                // Important difference:
                // fork uses useCache=false
                // and also puts hpKey/hpVal in the body.
                // ---------------------------------------------------------

                JSONObject requestBody =
                        new JSONObject();

                requestBody.put(
                        "searchType",
                        "games"
                );

                requestBody.put(
                        "searchTerms",
                        searchTerms
                );

                requestBody.put(
                        "searchPage",
                        1
                );

                requestBody.put(
                        "size",
                        20
                );

                requestBody.put(
                        "searchOptions",
                        searchOptions
                );

                requestBody.put(
                        "useCache",
                        false
                );

                /*
                 * The original script dynamically adds:
                 *
                 * "hpKey": "hpVal"
                 *
                 * to the JSON body.
                 */
                if (!hpKey.isEmpty()) {

                    requestBody.put(
                            hpKey,
                            hpVal
                    );
                }


                // ---------------------------------------------------------
                // 5. POST headers.
                //
                // These are the important headers from the fork.
                // ---------------------------------------------------------

                JSONObject postHeaders =
                        new JSONObject();

                postHeaders.put(
                        "User-Agent",
                        getUserAgent()
                );

                postHeaders.put(
                        "Content-Type",
                        "application/json"
                );

                postHeaders.put(
                        "Origin",
                        "https://howlongtobeat.com"
                );

                postHeaders.put(
                        "Referer",
                        "https://howlongtobeat.com/"
                );

                postHeaders.put(
                        "x-auth-token",
                        token
                );

                if (!hpKey.isEmpty()) {

                    postHeaders.put(
                            "x-hp-key",
                            hpKey
                    );
                }

                if (!hpVal.isEmpty()) {

                    postHeaders.put(
                            "x-hp-val",
                            hpVal
                    );
                }


                // ---------------------------------------------------------
                // 6. Perform HLTB search
                // ---------------------------------------------------------

                String response =
                        makeRequest(
                                HLTB_URL,
                                "POST",
                                requestBody.toString(),
                                postHeaders
                        );


                JSONObject result =
                        new JSONObject(response);

                JSONArray data =
                        result.optJSONArray("data");


                if (data == null ||
                        data.length() == 0) {

                    throw new Exception(
                            "No HLTB results found"
                    );
                }


                // ---------------------------------------------------------
                // 7. Find exact game name
                // ---------------------------------------------------------

                JSONObject matchedGame =
                        null;

                for (int i = 0;
                     i < data.length();
                     i++) {

                    JSONObject game =
                            data.getJSONObject(i);

                    String resultName =
                            game.optString(
                                            "game_name",
                                            ""
                                    )
                                    .toLowerCase()
                                    .trim();

                    if (resultName.equals(
                            lowerName)) {

                        matchedGame = game;
                        break;
                    }
                }


                /*
                 * Same fallback behavior:
                 * if exact match isn't found,
                 * use the first result.
                 */
                if (matchedGame == null) {

                    matchedGame =
                            data.getJSONObject(0);
                }


                // ---------------------------------------------------------
                // 8. Get main-story completion time
                // ---------------------------------------------------------

                long seconds =
                        matchedGame.optLong(
                                "comp_main",
                                0
                        );

                if (seconds <= 0) {

                    throw new Exception(
                            "Main story time unavailable"
                    );
                }


                double hours =
                        Math.round(
                                (seconds / 3600.0) * 2
                        ) / 2.0;


                String resultGameName =
                        matchedGame.optString(
                                "game_name",
                                gameName
                        );


                String hltbUrl =
                        "https://howlongtobeat.com/?q="
                                + URLEncoder.encode(
                                resultGameName,
                                StandardCharsets.UTF_8
                        );


                // ---------------------------------------------------------
                // 9. Return result
                // ---------------------------------------------------------

                callback.onSuccess(
                        resultGameName,
                        hours,
                        hltbUrl
                );


            } catch (Exception e) {

                callback.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown HLTB error"
                );
            }

        }).start();
    }


    // =============================================================
    // HTTP
    // =============================================================

    private static String makeRequest(
            String urlString,
            String method,
            String body,
            JSONObject headers
    ) throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                method
        );

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                15000
        );

        connection.setUseCaches(
                false
        );

        connection.setRequestProperty(
                "Connection",
                "keep-alive"
        );


        // Apply headers.

        if (headers != null) {

            JSONArray names =
                    headers.names();

            if (names != null) {

                for (int i = 0;
                     i < names.length();
                     i++) {

                    String name =
                            names.getString(i);

                    String value =
                            headers.getString(name);

                    connection.setRequestProperty(
                            name,
                            value
                    );
                }
            }
        }


        // POST body.

        if (body != null) {

            connection.setDoOutput(
                    true
            );

            byte[] bytes =
                    body.getBytes(
                            StandardCharsets.UTF_8
                    );

            connection.setFixedLengthStreamingMode(
                    bytes.length
            );

            try (OutputStream output =
                         connection.getOutputStream()) {

                output.write(bytes);
                output.flush();
            }
        }


        int responseCode =
                connection.getResponseCode();


        InputStream stream;

        if (responseCode >= 200 &&
                responseCode < 300) {

            stream =
                    connection.getInputStream();

        } else {

            stream =
                    connection.getErrorStream();

            if (stream == null) {

                connection.disconnect();

                throw new Exception(
                        "HLTB HTTP "
                                + responseCode
                );
            }
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                );


        StringBuilder response =
                new StringBuilder();

        String line;

        while ((line =
                reader.readLine()) != null) {

            response.append(line);
        }

        reader.close();

        connection.disconnect();


        if (responseCode < 200 ||
                responseCode >= 300) {

            /*
             * Include a small part of HLTB's
             * actual error response. This will
             * make the next Logcat much more useful.
             */
            String errorBody =
                    response.toString();

            if (errorBody.length() > 500) {
                errorBody =
                        errorBody.substring(
                                0,
                                500
                        );
            }

            throw new Exception(
                    "HLTB HTTP "
                            + responseCode
                            + " | "
                            + errorBody
            );
        }


        return response.toString();
    }


    // =============================================================
    // Browser-like User-Agent
    // =============================================================

    private static String getUserAgent() {

        return "Mozilla/5.0 " +
                "(Linux; Android 15; Mobile) " +
                "AppleWebKit/537.36 " +
                "(KHTML, like Gecko) " +
                "Chrome/153.0.0.0 " +
                "Mobile Safari/537.36";
    }
}