package com.thalesgroup.gemalto.idcloud.auth.sample.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.thales.dis.mobile.idcloud.auth.exception.IdCloudClientException;
import com.thales.dis.mobile.idcloud.auth.ui.UiCallbacks;
import com.thales.dis.mobile.idcloud.authui.callback.SampleCommonUiCallback;
import com.thales.dis.mobile.idcloud.authui.callback.SampleSecurePinUiCallback;
import com.thalesgroup.gemalto.idcloud.auth.sample.Configuration;
import com.thalesgroup.gemalto.idcloud.auth.sample.Progress;
import com.thalesgroup.gemalto.idcloud.auth.sample.R;
import com.thalesgroup.gemalto.idcloud.auth.sample.SecureLogArchive;
import com.thalesgroup.gemalto.idcloud.auth.sample.idcloudclient.Unenroll;
import com.thalesgroup.gemalto.idcloud.auth.sample.idcloudclient.Authenticate;

import java.io.File;


public class AuthenticateHomeFragment extends Fragment {

    private Unenroll unenrollObj;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView textView = (TextView) view.findViewById(R.id.textView_authenticate);
        Button button = (Button) view.findViewById(R.id.button_authenticate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Progress.sdkLock.tryAcquire()) return;
                executeAuthenticate(new OnExecuteFinishListener() {
                    @Override
                    public void onSuccess() {
                        Progress.sdkLock.release();
                        showToast(getString(R.string.authenticate_alert_message));
                    }
                    @Override
                    public void onError(IdCloudClientException e) {
                        Progress.sdkLock.release();
                        if (e.getError().getCode() == IdCloudClientException.ErrorCode.NO_PENDING_EVENTS.getCode()) {
                            showToast(e.getLocalizedMessage());
                        } else {
                            showAlertDialog(getString(R.string.alert_error_title),e.getLocalizedMessage());
                        }

                    }
                });
            }
        });

        ImageButton btn_share =(ImageButton)view.findViewById(R.id.imageButton_shareInAuthenticate);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Prepare secure log zip folder.
                File zipFile = SecureLogArchive.createSecureLogZip(getContext());

                //Sending secureLog through zip
                if (Build.VERSION.SDK_INT >= 24) {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, SecureLogArchive.getEmailTitle(getContext()));
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.secureLog_email_content));
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zipFile));
                startActivity(Intent.createChooser(sendIntent, getString(R.string.securelog_chooser_title)));

            }
        });

        Button addButton = (Button) view.findViewById(R.id.button_unenroll);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Progress.sdkLock.tryAcquire()) return;
                //Execute the add Unenroll use-case.
                executeUnenroll(new OnExecuteFinishListener() {
                    @Override
                    public void onSuccess() {
                        Progress.sdkLock.release();
                        showToast(getString(R.string.unenroll_alert_message));
                        Intent intent = new Intent(getActivity(), EnrollActivity.class);
                        getActivity().startActivity(intent);
                    }
                    @Override
                    public void onError(IdCloudClientException e) {
                        Progress.sdkLock.release();
                        showAlertDialog(getString(R.string.alert_error_title),e.getLocalizedMessage());
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void executeAuthenticate(final AuthenticateHomeFragment.OnExecuteFinishListener listener) {
        // Initialize an instance of the Authenticate use-case, providing
        // (1) the pre-configured URL
        Authenticate authenticateObj = new Authenticate(getActivity(), Configuration.url);
        authenticateObj.execute(listener);
    }

    private void executeUnenroll(final AuthenticateHomeFragment.OnExecuteFinishListener listener) {
        // Initialize an instance of the Unenroll use-case, providing
        // (1) the pre-configured URL

        unenrollObj = new Unenroll(getActivity(), Configuration.url);
        unenrollObj.execute(listener);
    }

    public interface OnExecuteFinishListener {
        void onSuccess();
        void onError(IdCloudClientException e);
    }

    protected void showAlertDialog(final String title, final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AuthenticateHomeFragment.this.getContext())
                        .setTitle(title)
                        .setMessage(message);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
    }
    protected void showToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(AuthenticateHomeFragment.this.getContext(), message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}

