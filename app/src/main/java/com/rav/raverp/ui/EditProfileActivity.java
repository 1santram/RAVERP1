package com.rav.raverp.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.gson.Gson;
import com.rav.raverp.MyApplication;
import com.rav.raverp.R;
import com.rav.raverp.data.interfaces.ArrowBackPressed;
import com.rav.raverp.data.interfaces.DialogActionCallback;
import com.rav.raverp.data.interfaces.ImageCompressTaskListener;
import com.rav.raverp.data.interfaces.StoragePermissionListener;
import com.rav.raverp.data.local.prefs.PrefsHelper;
import com.rav.raverp.data.model.api.ApiResponse;
import com.rav.raverp.data.model.api.EditEmailModel;
import com.rav.raverp.data.model.api.EditMobileModel;
import com.rav.raverp.data.model.api.GetProfile;
import com.rav.raverp.data.model.api.GetProject;
import com.rav.raverp.data.model.api.Login;
import com.rav.raverp.data.model.local.ChangePassword;
import com.rav.raverp.databinding.ActivityEditProfileBinding;
import com.rav.raverp.databinding.DialogEditEmailIdProfileBinding;
import com.rav.raverp.databinding.DialogEditMobileNoProfileBinding;
import com.rav.raverp.databinding.DialogPlotFilterBinding;
import com.rav.raverp.network.ApiClient;
import com.rav.raverp.network.ApiClientlocal;
import com.rav.raverp.network.ApiHelper;
import com.rav.raverp.utils.AppConstants;
import com.rav.raverp.utils.CommonUtils;
import com.rav.raverp.utils.NetworkUtils;
import com.rav.raverp.utils.ScreenUtils;
import com.rav.raverp.utils.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class EditProfileActivity extends BaseActivity implements ArrowBackPressed{

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    private Context mContext = EditProfileActivity.this;
    private ActivityEditProfileBinding binding;
    private ApiHelper apiHelper;
    private boolean isDialogHided;

    private boolean isGetProfile, isUpdateProfile;
    private Dialog filterDialog;
    private String profilePicPath;
    private Login login;
    private GetProfile getProfile;
    String EditMobileChange,EditEmailChange;
    String msg = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = putContentView(R.layout.activity_edit_profile);
        binding.setEditProfileActivity(this);
        setToolbarTitle("Profile");
        showBackArrow();
        setArrowBackPressed(this);

        ScreenUtils.setupUI(binding.parentLayout, EditProfileActivity.this);

         login = MyApplication.getLoginModel();

         binding.setLogin(login);
        binding.setGetProfile(getProfile);




    apiHelper = ApiClientlocal.getClient().create(ApiHelper.class);

        binding.editemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialogEmail();
            }
        });


        binding.editmobileno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialogMobile();

            }
        });

           GetProfileApi();

    }

    private void GetProfileApi() {

        String loginid=login.getStrLoginID();
        Integer role=login.getIntRoleID();
        Call<ApiResponse<List<GetProfile>>> GetProfileCall = apiHelper.getProfile(loginid,role);
        GetProfileCall.enqueue(new Callback<ApiResponse<List<GetProfile>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<GetProfile>>> call,
                                   @NonNull Response<ApiResponse<List<GetProfile>>> response) {

                if (response.isSuccessful()) {

                    if (response != null) {
                        if (response.body().getResponse().equalsIgnoreCase("Success")) {
                            List<GetProfile> login = response.body().getBody();


                            String email = login.get(0).getStrEmail();
                            binding.setemail.setText(email.toString());
                            String mobile = login.get(0).getStrPhone();
                            binding.setmobile.setText(mobile.toString());
                            String name = login.get(0).getStrDisplayName();
                            String profile = login.get(0).getStrProfilePic();
                            Glide.with(binding.profileImageView.getContext()).load("http://192.168.29.136" + profile)
                                    .placeholder(R.drawable.account)
                                    .into(binding.profileImageView);





                        } else {


                        }
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<GetProfile>>> call,
                                  @NonNull Throwable t) {
                if (!call.isCanceled()) {

                    ViewUtils.showToast(t.getLocalizedMessage());
                }
                t.printStackTrace();
            }
        });
    }




    private void showFilterDialogEmail() {
            filterDialog = new Dialog(this);
            final DialogEditEmailIdProfileBinding binding = DataBindingUtil.inflate(LayoutInflater.from((this)),
                    R.layout.dialog_edit_email_id_profile, null, false);
            filterDialog.setContentView(binding.getRoot());
            filterDialog.setCancelable(true);
            filterDialog.setCanceledOnTouchOutside(true);
            binding.setGetProfile(getProfile);


            ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
            InsetDrawable inset = new InsetDrawable(back, 70);
            Window window = filterDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(inset);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
            }
            filterDialog.show();
            binding.btncancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterDialog.dismiss();
                }
            });

            binding.imgcross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterDialog.dismiss();
                }
            });

            binding.btnsubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditEmailChange=  binding.editemailnochange.getText().toString();

                    submitFormEmailId();
                }
            });




        }

    private void GetEmailChange() {

        String id=login.getStrLoginID();
        Integer role=login.getIntRoleID();
        showProgress(true);
        Call<EditEmailModel>getEditEmailCall = apiHelper.getEditEmail(id,EditEmailChange,role);
        getEditEmailCall.enqueue(new Callback<EditEmailModel>() {
            @Override
            public void onResponse(@NonNull Call<EditEmailModel> call,
                                   @NonNull Response<EditEmailModel> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    if (response.body().getResponse().equalsIgnoreCase("Success")) {



                        ViewUtils.showSuccessDialog(mContext, response.body().getMessage(),
                                new DialogActionCallback() {
                                    @Override
                                    public void okAction() {


                                        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });

                    }else{

                    }

                }

            }




            @Override
            public void onFailure(@NonNull Call<EditEmailModel> call,
                                  @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    ViewUtils.showToast(t.getLocalizedMessage());
                }
                t.printStackTrace();
            }
        });


    }

    private void showFilterDialogMobile() {
        filterDialog = new Dialog(this);
        final DialogEditMobileNoProfileBinding binding = DataBindingUtil.inflate(LayoutInflater.from((this)),
                R.layout.dialog_edit_mobile_no_profile, null, false);
        filterDialog.setContentView(binding.getRoot());
        filterDialog.setCancelable(true);
        filterDialog.setCanceledOnTouchOutside(true);
        binding.setGetProfile(getProfile);



        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 70);
        Window window = filterDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(inset);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        filterDialog.show();


        binding.btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.dismiss();
            }
        });

        binding.imgcross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               filterDialog.dismiss();
            }
        });

        binding.btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditMobileChange=  binding.editmobilenochange.getText().toString();
                submitFormMobile();
               // GetMobileChange();
            }
        });


    }

    private void submitFormMobile() {
        if (!validateMobileNo()) {
            return;
        }


       checkNetwork();

    }

    private boolean validateMobileNo() {
        if (!CommonUtils.isValidMobile(EditMobileChange)){
            msg = getString(R.string.Please_Enter_Valid_Mobile_No);
            return false;
        }
        return true;
    }




    private void submitFormEmailId() {
        if (!validateEmailId()) {
            return;
        }

        GetEmailChange();

    }

    private boolean validateEmailId() {
      if (CommonUtils.isValidEmail(EditMobileChange)){
          msg = getString(R.string.Please_Enter_Valid_Email_ID);
          return false;
      } else {

      }
        return true;
    }

    public void checkNetwork() {
        if (NetworkUtils.isNetworkConnected()) {
         GetMobileChange();
        } else {
            ViewUtils.showOfflineDialog(mContext, new DialogActionCallback() {
                @Override
                public void okAction() {
                    checkNetwork();
                }
            });
        }
    }

    private void GetMobileChange() {

        String id=login.getStrLoginID();

        Integer role=login.getIntRoleID();
        showProgress(true);
        Call<EditMobileModel>getEditEmailCall = apiHelper.getEditMobile(id,EditMobileChange,role);
        getEditEmailCall.enqueue(new Callback<EditMobileModel>() {
            @Override
            public void onResponse(@NonNull Call<EditMobileModel> call,
                                   @NonNull Response<EditMobileModel> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    if (response.body().getResponse().equalsIgnoreCase("Success")) {

                        ViewUtils.showSuccessDialog(mContext, response.body().getMessage(),
                                new DialogActionCallback() {
                                    @Override
                                    public void okAction() {


                                        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });

                    }else{

                    }

                }

            }




            @Override
            public void onFailure(@NonNull Call<EditMobileModel> call,
                                  @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    showProgress(false);
                    ViewUtils.showToast(t.getLocalizedMessage());
                }
                t.printStackTrace();
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void arrowBackPressed() {
        onBackPressed();
    }



}


