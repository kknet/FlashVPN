package nova.fast.free.vpn.network;

import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import nova.fast.free.vpn.NovaApp;
import nova.fast.free.vpn.core.LocalVpnService;
import nova.fast.free.vpn.utils.EventReporter;
import nova.fast.free.vpn.utils.MLogs;

public class PingNet {
    private static final String TAG = "PingNet";

    /**
     * @param pingNetEntity 检测网络实体类
     * @return 检测后的数据
     */
    public static PingNetEntity ping(PingNetEntity pingNetEntity) {
        String line = null;
        Process process = null;
        BufferedReader successReader = null;
        String command = "/system/bin/ping -c " + pingNetEntity.getPingCount() + " -w " + pingNetEntity.getPingWtime() + " " + pingNetEntity.getIp();
//        String command = "ping -c " + pingCount + " " + host;
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                MLogs.e(TAG, "ping fail:process is null.");
                append(pingNetEntity.getResultBuffer(), "ping fail:process is null.");

                pingNetEntity.setPingTime(null);
                pingNetEntity.setResult(false);
                EventReporter.reportNoPingBinary();
                return pingNetEntity;
            }
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = successReader.readLine()) != null) {
                MLogs.i(TAG, line);
                append(pingNetEntity.getResultBuffer(), line);
                String time;
                if ((time = getTime(line)) != null) {
                    pingNetEntity.setPingTime(time);
                }
            }
            int status = process.waitFor();
            if (status == 0) {
                MLogs.i(TAG, "exec cmd success:" + command);
                Locale locale = NovaApp.getApp().getResources().getConfiguration().locale;
                EventReporter.reportPingLevel(locale.toString(), LocalVpnService.IsRunning, pingNetEntity.getIp(), pingNetEntity.getPingTime());
                append(pingNetEntity.getResultBuffer(), "exec cmd success:" + command);
                pingNetEntity.setResult(true);
            } else {
                MLogs.e(TAG, "exec cmd fail." + status);
                EventReporter.reportPingFailed(status);
                append(pingNetEntity.getResultBuffer(), "exec cmd fail.");
                pingNetEntity.setPingTime(""+ServerInfo.LEVEL_1_PING);
                pingNetEntity.setResult(true);
            }
            MLogs.i(TAG, "exec finished.");
            append(pingNetEntity.getResultBuffer(), "exec finished.");
        } catch (IOException e) {
            MLogs.e(TAG, String.valueOf(e));
            EventReporter.reportPingFailed(-10);
        } catch (InterruptedException e) {
            MLogs.e(TAG, String.valueOf(e));
            EventReporter.reportPingFailed(-11);
        } finally {
            MLogs.i(TAG, "ping exit.");
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                    MLogs.e(TAG, String.valueOf(e));
                }
            }
        }
        MLogs.i(TAG, pingNetEntity.getResultBuffer().toString());
        return pingNetEntity;
    }

    private static void append(StringBuffer stringBuffer, String text) {
        if (stringBuffer != null) {
            stringBuffer.append(text + "\n");
        }
    }

    private static String getTime(String line) {
        String[] lines = line.split("\n");
        String time = null;
        for (String l : lines) {
            if (!l.contains("time="))
                continue;
            int index = l.indexOf("time=");
            time = l.substring(index + "time=".length());
            MLogs.i(TAG, time);
        }
        return time;
    }
}
