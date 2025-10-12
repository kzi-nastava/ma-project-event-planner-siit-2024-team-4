package com.example.eventplanner.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.LogInActivity;
import com.example.eventplanner.network.ApiClient;
import com.example.eventplanner.network.service.AuthService;
import com.example.eventplanner.network.MultipartHelper;
import com.example.eventplanner.dto.RegistrationRequest;
import com.example.eventplanner.dto.RegistrationResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SPPRegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SPPRegistrationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SPPRegistrationFragment() {
        // Required empty public constructor
    }

    private ImageView profileImageView;
    private static final int PICK_IMAGES_REQUEST = 2;
    private final List<Bitmap> selectedBitmaps = new ArrayList<>();


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SPPRegistrationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SPPRegistrationFragment newInstance(String param1, String param2) {
        SPPRegistrationFragment fragment = new SPPRegistrationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spp_registration, container, false);

        Button registerBtn = view.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(v -> {
            EditText email = view.findViewById(R.id.enterEmailText);
            EditText name = view.findViewById(R.id.enterCompanyNameText);
            EditText desc = view.findViewById(R.id.enterDescriptionText);
            EditText address = view.findViewById(R.id.enterAddressText);
            EditText phone = view.findViewById(R.id.enterPhoneNumberText);
            EditText password = view.findViewById(R.id.enterPasswordText);
            EditText confirmPassword = view.findViewById(R.id.confirmPasswordText);

            if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            RegistrationRequest request = new RegistrationRequest(
                    "SPP",
                    email.getText().toString(),
                    password.getText().toString(),
                    address.getText().toString(),
                    phone.getText().toString(),
                    null, // ako nemaš slike još
                    name.getText().toString(),
                    desc.getText().toString(),
                    ""
            );

            sendRegistrationRequest(request);
        });


        profileImageView = view.findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            selectedBitmaps.clear();

            try {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                        selectedBitmaps.add(bitmap);
                    }
                    profileImageView.setImageBitmap(selectedBitmaps.get(0)); // prikaži prvu
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    selectedBitmaps.add(bitmap);
                    profileImageView.setImageBitmap(bitmap);
                }
            } catch (Exception ignored) {}
        }
    }

    private void sendRegistrationRequest(RegistrationRequest request) {
        AuthService authService = ApiClient.getClient().create(AuthService.class);

        RequestBody dtoBody = RequestBody.create(
                new com.google.gson.Gson().toJson(request),
                okhttp3.MediaType.parse("application/json")
        );

        List<MultipartBody.Part> files = MultipartHelper.createMultipartList(selectedBitmaps);
        Call<RegistrationResponse> call = authService.register(dtoBody, files);

        call.enqueue(new retrofit2.Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, retrofit2.Response<RegistrationResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Activation link sent to your email.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getActivity(), LogInActivity.class));
                } else {
                    Toast.makeText(getActivity(), "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}