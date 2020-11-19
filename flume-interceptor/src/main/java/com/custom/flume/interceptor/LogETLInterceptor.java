package com.custom.flume.interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ETL拦截器
 */
public class LogETLInterceptor implements Interceptor {
    @Override
    public void initialize() {

    }
    /*
    17: 58: 20.220[main] INFO com.atguigu.appclient.AppMain - 1604397500220 | {
	"cm": {
		"ln": "-55.4",
		"sv": "V2.2.6",
		"os": "8.2.3",
		"g": "IBCOJLJH@gmail.com",
		"mid": "999",
		"nw": "4G",
		"l": "pt",
		"vc": "19",
		"hw": "640*1136",
		"ar": "MX",
		"uid": "999",
		"t": "1604326500405",
		"la": "-41.1",
		"md": "sumsung-14",
		"vn": "1.0.4",
		"ba": "Sumsung",
		"sr": "X"
	},
	"ap": "app",
	"et": [{
		"ett": "1604324992323",
		"en": "display",
		"kv": {
			"goodsid": "247",
			"action": "2",
			"extend1": "2",
			"place": "3",
			"category": "30"
		}
	}, {
		"ett": "1604367675860",
		"en": "newsdetail",
		"kv": {
			"entry": "3",
			"goodsid": "248",
			"news_staytime": "3",
			"loading_time": "16",
			"action": "3",
			"showtype": "4",
			"category": "41",
			"type1": "325"
		}
	}, {
		"ett": "1604315002832",
		"en": "notification",
		"kv": {
			"ap_time": "1604348386746",
			"action": "2",
			"type": "3",
			"content": ""
		}
	}, {
		"ett": "1604364722308",
		"en": "error",
		"kv": {
			"errorDetail": "at cn.lift.dfdfdf.control.CommandUtil.getInfo(CommandUtil.java:67)\\n at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\n at java.lang.reflect.Method.invoke(Method.java:606)\\n",
			"errorBrief": "at cn.lift.dfdf.web.AbstractBaseController.validInbound(AbstractBaseController.java:72)"
		}
	}, {
		"ett": "1604374315870",
		"en": "favorites",
		"kv": {
			"course_id": 0,
			"id": 0,
			"add_time": "1604386385094",
			"userid": 0
		}
	}]
}
     */

    /**
     * 后面考虑到做用户画像系统,所以这个地方不要耽搁太长时间,做一个轻度清洗
     * @param event
     * @return
     */
    @Override
    public Event intercept(Event event) {
        //1.将Event->String类型,这样方便我们做一个大括号开头,大括号结尾的合法性效验
        byte[] body = event.getBody();
        String log = Arrays.toString(body);

        if (log.contains("start")){
            //清洗启动日志
            if (LogUtils.validateStart(log)){
                return event;
            }

        }else{
            //清洗事件日志
            if (LogUtils.validateEvent(log)){
                return event;
            }

        }

        return null;
    }

    @Override
    public List<Event> intercept(List<Event> Events) {
        ArrayList<Event> arrayList = new ArrayList<>();
        for (Event event : Events) {
            Event intercept1 = intercept(event);
            if (intercept1!=null){
                arrayList.add(intercept1);
            }

        }
        return arrayList;
    }

    @Override
    public void close() {

    }

    public static class Builder implements Interceptor.Builder{

        @Override
        public Interceptor build() {
            return new LogETLInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
