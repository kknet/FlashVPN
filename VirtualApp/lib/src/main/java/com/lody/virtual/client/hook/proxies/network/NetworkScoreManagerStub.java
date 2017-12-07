package com.lody.virtual.client.hook.proxies.network;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.lody.virtual.server.INetworkScoreManager;

import java.lang.reflect.Method;

public class
NetworkScoreManagerStub extends BinderInvocationProxy {

    public NetworkScoreManagerStub() {
        super(INetworkScoreManager.Stub.asInterface(ServiceManagerNative.getService(ServiceManagerNative.NETWORK_SCORE)), "network_score");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "setActiveScorer";
            }

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                return true;
            }
        });
    }
}
