package com.lee.etirps.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by Anchu on 2/11/2018.
 */


public class PostMessageDialogFragment extends DialogFragment {


    public interface PostMessageDialogListener{
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    PostMessageDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mListener = (PostMessageDialogListener) context;
        } catch(ClassCastException ex){
            Log.e("PostMessageDialogFrg", ex.getMessage());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_create, null)).setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogPositiveClick(PostMessageDialogFragment.this);
            }
        })      .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogNegativeClick(PostMessageDialogFragment.this);
            }
        }).setTitle("Drop a Fragment");

        return builder.create();
    }

}