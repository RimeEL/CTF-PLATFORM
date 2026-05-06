package com.ctf.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonUtil {
	private static final Gson gson = new GsonBuilder()
	        .setPrettyPrinting()
	        .registerTypeAdapter(java.time.LocalDateTime.class, 
	            (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) ->
	                new com.google.gson.JsonPrimitive(src.toString()))
	        .registerTypeAdapter(java.time.LocalDateTime.class,
	            (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) ->
	                java.time.LocalDateTime.parse(json.getAsString()))
	        .create();
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    // Lire le body JSON d'une requête HTTP
    public static <T> T parseBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) sb.append(line);
        return fromJson(sb.toString(), clazz);
    }

    // Envoyer une réponse JSON
    public static void sendJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(toJson(data));
    }
}