package imooc.naga.server.jwt;

import imooc.naga.entity.system.User;

public class ContextUtil {
    private static ThreadLocal<User> local = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        local.set(user);
    }

    public static User getCurrentUser() {
        return local.get();
    }
}
