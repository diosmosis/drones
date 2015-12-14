package com.flarestar.drones.routing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * TODO
 * TODO: support sending all sorts of intents, not just intents to other activities
 */
@Singleton
public class ActivityRouter {

    public static class ActivityNotFoundException extends RuntimeException {
        public ActivityNotFoundException() {
        }

        public ActivityNotFoundException(String message) {
            super(message);
        }

        public ActivityNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public ActivityNotFoundException(Throwable cause) {
            super(cause);
        }

        public ActivityNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    private Activity activity;

    private Map<String, String> routes;
    private Map<String, Object> parameters;

    @Inject
    public ActivityRouter(Activity activity) {
        this.activity = activity;

        routes = findAvailableRoutes(activity);

        Map<String, Object> parameters = parseActivityIntent(activity.getIntent());
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public void set(String urlString) throws MalformedURLException {
        urlString = "http://dummy.com" + urlString; // necessary so URL won't throw

        URL url = new URL(urlString);

        String path = url.getPath().toLowerCase();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String activityClass = routes.get(path);
        if (activityClass == null) {
            throw new ActivityNotFoundException("Cannot find the activity class for path '" + path +"'.");
        }

        Intent intent = new Intent();
        intent.setClassName(activity, activityClass);

        Map<String, Object> params = parseQueryParameters(url.getQuery());
        addParsedParamsToIntent(intent, params);

        activity.startActivity(intent);
    }

    public String get(String paramName) {
        return (String)this.parameters.get(paramName);
    }

    public Map<String, Object> getAll() {
        return this.parameters;
    }

    public List<String> getMultiple(String paramName) {
        Object value = this.parameters.get(paramName + "[]");
        return (List<String>)value;
    }

    private List<String> convertArrayToList(Object value) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i != Array.getLength(value); ++i) {
            result.add(Array.get(value, i).toString());
        }
        return result;
    }

    private Map<String,Object> parseQueryParameters(String query) {
        Map<String, Object> params = new HashMap<>();
        if (query == null) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            String name = parts[0];
            String value = parts[1];

            if (name.endsWith("[]")) {
                name = name.substring(0, name.length() - 2);

                List<String> values = (List<String>)params.get(name);
                if (values == null) {
                    values = new ArrayList<>();
                    params.put(name, values);
                }

                values.add(value);
            } else {
                params.put(name, value);
            }
        }
        return params;
    }

    private void addParsedParamsToIntent(Intent intent, Map<String, Object> params) {
        for (Map.Entry<String, Object> param : params.entrySet()) {
            Object value = param.getValue();
            if (value instanceof List) {
                List<String> list = (List<String>)value;
                String[] array = list.toArray(new String[list.size()]);

                intent.putExtra(param.getKey(), array);
            } else {
                intent.putExtra(param.getKey(), (String)value);
            }
        }
    }

    private Map<String, String> findAvailableRoutes(Activity activity) {
        Map<String, String> result = new HashMap<>();

        ActivityInfo[] activities;
        try {
            activities = activity.getPackageManager().getPackageInfo(
                activity.getPackageName(), PackageManager.GET_ACTIVITIES).activities;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e); // unexpected
        }

        for (ActivityInfo info : activities) {
            result.put('/' + info.name.toLowerCase().replace('.', '/'), info.name);
        }

        return result;
    }

    private Map<String, Object> parseActivityIntent(Intent intent) {
        Map<String, Object> result = new HashMap<>();

        Bundle extraData = intent.getExtras();
        if (extraData == null) {
            return result;
        }

        for (String key : extraData.keySet()) {
            Object value = extraData.get(key);
            if (value == null) {
                continue;
            }

            if (value.getClass().isArray()) {
                result.put(key + "[]", convertArrayToList(value));
            } else {
                result.put(key, value.toString());
            }
        }
        return result;
    }
}
