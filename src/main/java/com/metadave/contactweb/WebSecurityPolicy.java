package com.metadave.contactweb;


import com.basho.contact.commands.*;
import com.basho.contact.security.AccessPolicy;

import java.util.HashMap;
import java.util.Map;

public class WebSecurityPolicy implements AccessPolicy{
    private static Map<Class<?>, Boolean> canAccess = new HashMap<Class<?>, Boolean>();
    static {
        canAccess.put(ConnectCommand.class, false);
        canAccess.put(ConnectionsCommand.class, false);
        canAccess.put(CountKeysCommand.class, true);
        canAccess.put(DeleteCommand.class, true);
        canAccess.put(FetchCommand.class, true);
        canAccess.put(GetBucketCommand.class, true);
        canAccess.put(GetBucketPropsCommand.class, true);
        canAccess.put(ListBucketsCommand.class, true);
        canAccess.put(ListKeysCommand.class, true);
        canAccess.put(MapredCommand.class, false);
        canAccess.put(Query2iCommand.class, true);
        canAccess.put(SetBucketPropsCommand.class, false);
        canAccess.put(StoreCommand.class, true);
        canAccess.put(UpdateCommand.class, false);
    }
    @Override
    public boolean canAccess(Class<?> aClass, String s) {
        if(!canAccess.containsKey(aClass)) {
            System.err.println("Unknown security operation:" + aClass.getName());
            return false;
        } else {
            return canAccess.get(aClass);
        }
    }
}
