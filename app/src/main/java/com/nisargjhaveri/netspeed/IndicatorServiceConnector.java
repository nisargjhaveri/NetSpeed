package com.nisargjhaveri.netspeed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

final class IndicatorServiceConnector {
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT= 2;
    static final int MSG_UPDATE_SPEED = 3;

    public interface ServiceCallback {
        void updateSpeed(Speed speed);
    }

    private Messenger mServiceMessenger = null;
    private Messenger mClientMessenger = null;
    private Handler mClientHandler = null;

    private boolean mBound = false;

    private Context mContext;
    private ServiceCallback mServiceCallback;

    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;
            switch (msg.what) {
                case MSG_UPDATE_SPEED:
                    mServiceCallback.updateSpeed(new Speed(mContext, (Bundle)msg.obj));
                    break;
                default:
                    handled = false;
            }
            return !handled;
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            HandlerThread handlerThread = new HandlerThread("IPChandlerThread");
            handlerThread.start();
            mClientHandler = new Handler(handlerThread.getLooper(), mHandlerCallback);

            mClientMessenger = new Messenger(mClientHandler);

            mServiceMessenger = new Messenger(service);

            try {
                // Register us with service
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = mClientMessenger;
                mServiceMessenger.send(msg);
                mBound = true;
            } catch (RemoteException e) {
                disconnectService();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // Service dead
            disconnectService();
        }
    };

    private void disconnectService() {
        mClientHandler.getLooper().quitSafely();
        mClientHandler = null;
        mClientMessenger = null;

        mServiceMessenger = null;
        mBound = false;
    }

    IndicatorServiceConnector(Context context, ServiceCallback serviceCallback) {
        mContext = context;
        mServiceCallback = serviceCallback;
    }

    boolean bindService() {
        return mContext
                .getApplicationContext()
                .bindService(
                        new Intent(mContext, IndicatorService.class),
                        mServiceConnection,
                        Context.BIND_AUTO_CREATE
                );
    }

    void unbindService() {
        if (mBound) {
            try {
                // Unregister us with service
                Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                msg.replyTo = mClientMessenger;
                mServiceMessenger.send(msg);

                mContext.getApplicationContext().unbindService(mServiceConnection);
            } catch (RemoteException | IllegalArgumentException e) {
                // Service dead
            }

            disconnectService();
        }
    }
}
