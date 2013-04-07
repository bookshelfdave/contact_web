package com.metadave.contactweb;


import com.basho.contact.ConnectionInfo;
import com.basho.contact.ContactConnectionProvider;
import com.basho.contact.RuntimeContext;
import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebConnectionProvider implements ContactConnectionProvider{

    List<IRiakClient> clients = new ArrayList<IRiakClient>();

    public WebConnectionProvider(String hostsline) {
        System.out.println(hostsline);
        String hosts[] = hostsline.split(",");
        for(String host:hosts) {
            String chunks[] = host.split(":");
            String ip = chunks[0];
            String port = chunks[1];
            try {
                System.out.println("Connecting to Riak @ " + ip + ":" + port);
                IRiakClient riakClient = RiakFactory.pbcClient(ip, Integer.parseInt(port));
                clients.add(riakClient);
            } catch (RiakException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IRiakClient getDefaultClient(RuntimeContext runtimeContext) {
        return clients.get(0);
    }

    @Override
    public IRiakClient getClientByName(String s, RuntimeContext runtimeContext) {
        return clients.get(0);
    }

    @Override
    public IRiakClient createDefaultConnection(String s, int i, RuntimeContext runtimeContext) {
        return clients.get(0);
    }

    @Override
    public IRiakClient createNamedConnection(String s, int i, String s2, RuntimeContext runtimeContext) {
        return clients.get(0);
    }

    @Override
    public Map<String, ConnectionInfo> getAllConnections() {
        return new HashMap<String, ConnectionInfo>();
    }
}
