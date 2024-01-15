package com.zyd.lib_log;

import java.lang.reflect.Type;
import java.util.Set;

public interface MessageListener<T> {

    void onNewMessage(T data);

    static <T> void postData(Set<MessageListener<T>> listeners, T data) {
        if (listeners != null && !listeners.isEmpty()) {
            for (MessageListener<T> listener : listeners) {
                String[] genericInterfacesClassName = getGenericInterfacesClassName(listener.getClass());
                if (genericInterfacesClassName != null && genericInterfacesClassName.length > 0) {
                    String className = genericInterfacesClassName[0];
                    if (data.getClass().getName().equals(className)) {
                        listener.onNewMessage(data);
                    }
                }
            }
        }
    }

    static String[] getGenericInterfacesClassName(Class<?> clz) {
        Type[] genericInterfaces = clz.getGenericInterfaces();
        if (genericInterfaces.length > 0) {
            Type type = getTypeByName(genericInterfaces, MessageListener.class.getName());
            if (type != null) {
                String typeName = type.toString();
                int i1 = typeName.indexOf("<");
                int i2 = typeName.indexOf(">");
                String allClassName = typeName.substring(i1 + 1, i2);
                return allClassName.split(",");
            }
        }
        return null;
    }

    static Type getTypeByName(Type[] types, String name) {
        for (Type type : types) {
            if (type.toString().contains(name)) {
                return type;
            }
        }
        return null;
    }

}
