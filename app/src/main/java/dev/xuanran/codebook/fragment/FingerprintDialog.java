package dev.xuanran.codebook.fragment;

import android.app.Dialog;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import dev.xuanran.codebook.R;

public class FingerprintDialog extends DialogFragment {

    private FingerprintManager fingerprintManager;
    private FingerprintAuthenticationCallback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager = (FingerprintManager) requireContext().getSystemService(Context.FINGERPRINT_SERVICE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fingerprint, null);
        builder.setView(view);
        builder.setTitle("指纹验证");

        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(/* Here you need to pass your crypto object for encryption/decryption */);
        CancellationSignal cancellationSignal = new CancellationSignal();

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onFingerprintAuthenticationSucceeded();
                dismiss();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onFingerprintAuthenticationFailed();
            }
        }, null);

        return builder.create();
    }

    public void setFingerprintAuthenticationCallback(FingerprintAuthenticationCallback callback) {
        this.callback = callback;
    }

    public interface FingerprintAuthenticationCallback {
        void onFingerprintAuthenticationSucceeded();

        void onFingerprintAuthenticationFailed();
    }
}
