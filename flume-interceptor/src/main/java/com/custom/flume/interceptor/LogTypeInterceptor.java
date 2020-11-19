package com.custom.flume.interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Flume日志类型区分拦截器
 */
public class LogTypeInterceptor implements Interceptor {
    @Override
    public void initialize() {

    }

    /**
     * 将数据分成两类
     * @param event
     * @return
     */
    @Override
    public Event intercept(Event event) {
        //取出body数据
        byte[] body = event.getBody();
        String log = Arrays.toString(body);
        //取出header
        Map<String, String> headers = event.getHeaders();

        if (log.contains("start")){
            headers.put("topic","topic_start");

        }else headers.put("topic","topic_event");

        return null;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        ArrayList<Event> arrayList = new ArrayList<>();
        for (Event event : events) {
            Event intercept1 = intercept(event);
            arrayList.add(intercept1);
        }
        return arrayList;
    }

    @Override
    public void close() {

    }
    public static class Builder implements Interceptor.Builder{

        @Override
        public Interceptor build() {
            return new LogTypeInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
