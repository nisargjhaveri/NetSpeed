package com.nisargjhaveri.netspeed;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainIndicatorFragment extends Fragment {
    IndicatorServiceConnector mServiceConnector;

    TextView speedValueTextView;
    TextView speedUnitTextView;

    IndicatorServiceConnector.ServiceCallback mServiceCallback = new IndicatorServiceConnector.ServiceCallback() {
        @Override
        public void updateSpeed(final Speed speed) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (speedValueTextView != null) speedValueTextView.setText(speed.total.speedValue);
                    if (speedUnitTextView != null) speedUnitTextView.setText(speed.total.speedUnit);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceConnector = new IndicatorServiceConnector(getContext(), mServiceCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_indicator, container, false);

        speedValueTextView = view.findViewById(R.id.speedValueText);
        speedUnitTextView = view.findViewById(R.id.speedUnitText);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mServiceConnector.bindService();
    }

    public void onStop() {
        mServiceConnector.unbindService();

        super.onStop();
    }
}
