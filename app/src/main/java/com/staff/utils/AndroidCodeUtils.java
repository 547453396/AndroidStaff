package com.staff.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipFile;

/**
 * Created by liheng on 17/4/6.
 * 此类主要总结一些开发中常用的方法，未归类随意，持续补充
 */
public class AndroidCodeUtils {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * px(像素) 的单位 转成为 sp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 拨打电话
     * 需要添加android.permission.CALL_PHONE权限
     * @param context
     * @param phoneNumber
     */
    public static void call(Context context, String phoneNumber) {
        Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        context.startActivity(call);
    }

    /**
     * 跳转至拨号界面
     * @param context
     * @param phoneNumber
     */
    public static void callDial(Context context, String phoneNumber) {
        context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
    }

    /**
     * 发送短信
     * @param context
     * @param phoneNumber
     * @param content
     */
    public static void sendSms(Context context, String phoneNumber,String content) {
        Uri uri = Uri.parse("smsto:"+ (TextUtils.isEmpty(phoneNumber) ? "" : phoneNumber));
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", TextUtils.isEmpty(content) ? "" : content);
        context.startActivity(intent);
    }

    /**
     * 唤醒屏幕并解锁
     * 需要 android.permission.DISABLE_KEYGUARD
     * android.permission.WAKE_LOCK
     * @param context
     */
    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    /**
     * 判断当前App处于前台还是后台状态
     * 需要权限
     * @param context
     * @return
     */
    public static boolean isApplicationBackground(final Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有可用网络
     * @param ctx
     * @return
     */
    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * 判断wifi是否可用
     * @param context
     * @return
     */
    public static boolean isWiFiActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getTypeName().equals("WIFI")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取手机的imei
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        TelephonyManager telephoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephoneManager.getDeviceId();
    }

    /**
     * 获取手机的imsi
     * @param context
     * @return
     */
    public static String getImsi(Context context) {
        TelephonyManager telephoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephoneManager.getSubscriberId();
    }

    /**
     * 获取mac地址
     * 需要权限 android.permission.ACCESS_WIFI_STATE
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    /**
     * 获取状态栏高度
     * @param ctx
     * @return
     */
    public static int getStatusBarHeight(Context ctx) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = ctx.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null)
            return null;
        String table = "0123456789abcdef";
        StringBuilder ret = new StringBuilder(2 * bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            int b;
            b = 0x0f & (bytes[i] >> 4);
            ret.append(table.charAt(b));
            b = 0x0f & bytes[i];
            ret.append(table.charAt(b));
        }
        return ret.toString();
    }

    /**
     * 计算给定 byte [] 串的 MD5
     */
    private static byte[] MD5(byte[] input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(input);
            return md.digest();
        } else
            return null;
    }

    /**
     * 获取md5字符串
     * @param input
     * @return
     */
    public static String getMD5(byte[] input) {
        return bytesToHexString(MD5(input));
    }

    /**
     * 是否安装了包名为pkgName的包
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean isInstalledPkg(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(pkgName,
                    PackageManager.GET_ACTIVITIES);
            if (info != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 显示软键盘
     * @param context
     * @param edt
     */
    public static void showKeyBord(final Context context,final EditText edt) {
        edt.requestFocus();
        Timer timer = new Timer(); // 设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // 弹出软键盘的代码
                if (edt.getParent() != null) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edt, InputMethodManager.RESULT_SHOWN);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        }, 200);
    }

    /**
     * 隐藏软键盘
     * @param context
     * @param edt
     */
    public static void hideKeyBord(Context context, EditText edt) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
    }

    /**
     * 判断x、y是否在view控件范围
     * @param view
     * @param x
     * @param y
     * @return
     */
    public static boolean inRangeOfView(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        if (x < viewX
                || x > (viewX + view.getWidth())
                || y < viewY
                || y > (viewY + view.getHeight())) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前进程名字
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     *  设置view点击和显示的默认样式，这个是总方法，
     * 显示，点击时候的状态，主要就是两个色值
     * 圆角：传入每个角的圆角大小，四个圆角的顺序为左上，右上，右下，左下,并且四个角都为dp转px
     * 边宽：如果传入的边框不大于0 这不显示边框，边框同样有两个颜色,如果边框没有，那么颜色也用不到
     *
     * @param view
     * @param stroke 边框宽度 默认必须传人dp转px
     * @param stroke_color_1 边框默认颜色
     * @param stroke_color_2 边框选中的颜色
     * @param bg_color_1  背景默认颜色
     * @param bg_color_2  背景点击状态的颜色
     * @param radius_l_t 左上
     * @param radius_r_t 右上
     * @param radius_r_b 右下
     * @param radius_l_b 左下
     */
    public static void setViewSelectorBg(View view, int stroke,
                                         int stroke_color_1, int stroke_color_2,
                                         int bg_color_1, int bg_color_2,
                                         float radius_l_t, float radius_r_t,
                                         float radius_r_b, float radius_l_b) {
        //1、2两个参数表示左上角，3、4表示右上角，5、6表示右下角，7、8表示左下角
        float radius[] = new float[]{radius_l_t, radius_l_t, radius_r_t, radius_r_t, radius_r_b, radius_r_b, radius_l_b, radius_l_b};
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setColor(bg_color_1);
        normalDrawable.setCornerRadii(radius);
        if (stroke > 0) {
            normalDrawable.setStroke(stroke, stroke_color_1);
        }
        GradientDrawable selectedDrawable = new GradientDrawable();
        selectedDrawable.setColor(bg_color_2);
        selectedDrawable.setCornerRadii(radius);
        if (stroke > 0) {
            selectedDrawable.setStroke(stroke, stroke_color_2);
        }
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_focused, -android.R.attr.state_selected, -android.R.attr.state_pressed}, normalDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_focused, android.R.attr.state_selected, -android.R.attr.state_pressed}, selectedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_focused, -android.R.attr.state_pressed}, selectedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, selectedDrawable);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(stateListDrawable);
        } else {
            view.setBackgroundDrawable(stateListDrawable);
        }
    }

    /**
     * 获取软件apk文件末尾的自定义信息
     * @param ctx
     * @param apkPath
     * @return
     */
    public static String readApkExtInfo(Context ctx,String apkPath){
        if(ctx==null){
            return "";
        }
        byte[] bytes = null;
        try {
            RandomAccessFile accessFile = new RandomAccessFile(new File(apkPath), "r");
            long index = accessFile.length();

            bytes = new byte[2];
            index = index - bytes.length;
            accessFile.seek(index);
            accessFile.readFully(bytes);

            int contentLength = stream2Short(bytes, 0);

            bytes = new byte[contentLength];
            index = index - bytes.length;
            accessFile.seek(index);
            accessFile.readFully(bytes);

            return new String(bytes, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 向apk中写入一段信息,
     * 该方法通常在服务器端设置，zipFile.getComment()需要java1.7
     * android4.4之前是不支持Java7 的
     * @param file
     * @param comment
     */
    public static void writeApkExtInfo(File file, String comment) {
        ZipFile zipFile = null;
        ByteArrayOutputStream outputStream = null;
        RandomAccessFile accessFile = null;
        try {
            zipFile = new ZipFile(file);
            String zipComment = zipFile.getComment();
            if (!TextUtils.isEmpty(zipComment)) {
                return;
            }
            byte[] byteComment = comment.getBytes();
            outputStream = new ByteArrayOutputStream();
            outputStream.write(byteComment);
            outputStream.write(short2Stream((short) byteComment.length));
            byte[] data = outputStream.toByteArray();
            accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(file.length() - 2);
            accessFile.write(short2Stream((short) data.length));
            accessFile.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (accessFile != null) {
                    accessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * short转换成字节数组（小端序）
     * @return
     */
    private static short stream2Short(byte[] stream, int offset) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(stream[offset]);
        buffer.put(stream[offset + 1]);
        return buffer.getShort(0);
    }
    /**
     * 字节数组转换成short（小端序）
     * @return
     */
    private static byte[] short2Stream(short data) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(data);
        buffer.flip();
        return buffer.array();
    }

    /**
     * 将一张纯色图处理成mColor颜色
     * @param mBitmap
     * @param mColor
     * @return
     */
    public static Bitmap getColorBitmap(Bitmap mBitmap, int mColor) {
        Bitmap mColorBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas mCanvas = new Canvas(mColorBitmap);
        Paint mPaint = new Paint();

        mPaint.setColor(mColor);
        //从原位图中提取只包含alpha的位图
        Bitmap alphaBitmap = mBitmap.extractAlpha();
        //在画布上（mAlphaBitmap）绘制alpha位图
        mCanvas.drawBitmap(alphaBitmap, 0, 0, mPaint);

        return mColorBitmap;
    }

}
