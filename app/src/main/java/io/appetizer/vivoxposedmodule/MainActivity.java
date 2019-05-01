package io.appetizer.vivoxposedmodule;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainActivity implements IXposedHookLoadPackage {
    public void handleLoadPackage(final LoadPackageParam lpp) throws Throwable {
        logRequest(lpp);
        logResponse(lpp);
    }

    public void logRequest(final LoadPackageParam lpp){
        try {
            Class<?> tVal = XposedHelpers.findClass("com.vivo.g.t",lpp.classLoader);
            XposedHelpers.findAndHookMethod("com.vivo.appstore.a", lpp.classLoader, "a", tVal, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String requestUrl = (String) param.getResult();
                    logtofile(requestUrl);
                    Log.d("VivoAppmarket", requestUrl);
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Throwable e) {
            Log.e("VivoAppmarket", "logRequest" + e.getMessage());
        }

    }

    public void logResponse(final LoadPackageParam lpp){
        try {
            XposedHelpers.findAndHookMethod("com.vivo.appstore.a", lpp.classLoader, "a", byte[].class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String responsebody = (String) param.getResult();
                    logtofile(responsebody);
                    Log.d("VivoAppmarket", responsebody);
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Throwable e) {
            Log.e("VivoAppmarket", "logResponse" + e.getMessage());
        }

    }


    public void logRequestAndResponse(final LoadPackageParam lpp){
        try {
            final Class<?> tVal = XposedHelpers.findClass("com.vivo.g.t",lpp.classLoader);
            final Class<?> Request = XposedHelpers.findClass("com.vivo.network.okhttp3.Request",lpp.classLoader);
            final Class<?> Response = XposedHelpers.findClass("com.vivo.network.okhttp3.Response",lpp.classLoader);
            final Class<?> ResponseBody = XposedHelpers.findClass("com.vivo.network.okhttp3.ResponseBody",lpp.classLoader);
            final Class<?> RealResponseBody = XposedHelpers.findClass("com.vivo.network.okhttp3.internal.http.RealResponseBody",lpp.classLoader);
            XposedHelpers.findAndHookMethod("com.vivo.g.n", lpp.classLoader, "a", Response, tVal, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object response = param.args[0];
                    Method body = Response.cast(response).getClass().getDeclaredMethod("body");
                    body.setAccessible(true);
                    Object responsebody = body.invoke(Response.cast(response));

                    Method[] ms1 = ResponseBody.cast(responsebody).getClass().getMethods();
                    for(Method m : ms1){
                        m.setAccessible(true);
                        Log.e("1VivoAppmarket", m.getName());
                        if(m.getName().equalsIgnoreCase("string")){
                            String responsestr = (String)m.invoke(ResponseBody.cast(responsebody));
                            Log.e("1VivoAppmarket", responsestr);
                        }

                    }

                    /*Method[] ms1 = RealResponseBody.cast(responsebody).getClass().getMethods();
                    for(Method m : ms1){
                        m.setAccessible(true);
                        if(m.getName().equalsIgnoreCase("string")){
                            String responsestr = (String)m.invoke(RealResponseBody.cast(responsebody));
                            Log.e("VivoAppmarket", responsestr);
                        }

                    }*/
                    //if(bodystr!=null)
                    //else Log.e("VivoAppmarket", "AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    Log.d("VivoAppmarket", responsebody.toString());
                    Object tval = param.args[1];
                    Log.d("VivoAppmarket", tVal.cast(tval).toString());
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Throwable e) {
            Log.e("VivoAppmarket", "logRequest" + e.getMessage());
        }

    }

    public void hookCallAndLog(final LoadPackageParam lpp){
        try {
            final Class<?> Request = XposedHelpers.findClass("com.vivo.network.okhttp3.Request",lpp.classLoader);
            final Class<?> Response = XposedHelpers.findClass("com.vivo.network.okhttp3.Response",lpp.classLoader);
            final Class<?> RealCall = XposedHelpers.findClass("com.vivo.network.okhttp3.RealCall",lpp.classLoader);
            final Class<?> ResponseBody = XposedHelpers.findClass("com.vivo.network.okhttp3.ResponseBody",lpp.classLoader);

            XposedHelpers.findAndHookMethod("com.vivo.network.okhttp3.OkHttpClient", lpp.classLoader, "newCall", Request, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object request = param.args[0];
                    Log.d("VivoAppmarket", Request.cast(request).toString());
                    Object call = param.getResult();
                    Method getResponseWithInterceptorChain = RealCall.cast(call).getClass().getDeclaredMethod("getResponseWithInterceptorChain",boolean.class);
                    getResponseWithInterceptorChain.setAccessible(true);
                    Object response = getResponseWithInterceptorChain.invoke(RealCall.cast(call),false);
                    Method body = Response.cast(response).getClass().getDeclaredMethod("body");
                    body.setAccessible(true);
                    Object responsebody = body.invoke(Response.cast(response));
                    Method string = ResponseBody.cast(responsebody).getClass().getDeclaredMethod("string");
                    string.setAccessible(true);
                    String responsestr = (String)string.invoke(ResponseBody.cast(responsebody));
                    Log.d("VivoAppmarket", responsestr);
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Throwable e) {
            Log.e("VivoAppmarket", "logRequest" + e.getMessage());
        }

    }
    public void logtofile(String data){
        File file = new File("sdcard/vivosession.json");
        String path = file.getAbsolutePath();
        Log.d("DApp",path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file,true);
            fos.write((data+"\n").getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
